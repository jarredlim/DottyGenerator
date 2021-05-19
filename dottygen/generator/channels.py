from abc import abstractmethod

from dottygen.generator.base import CommunicationBase
from dottygen.generator.utils import first_char_lower, get_labels_name

class ChannelInstance(CommunicationBase):


    def __init__(self, channel_name: str, labels, continuation="", sender="", receiver = ""):
        self._channel_name = channel_name
        self._labels = labels
        self.continuation = continuation
        self._sender = sender
        self._receiver = receiver

    @abstractmethod
    def get_channel_type(self):
        pass

    def get_sender(self):
        return self._sender

    def get_receiver(self):
        return self._receiver

    def get_labels(self):
        return self._labels

    def get_labels_name(self):
        return get_labels_name(self._labels)

    def __eq__(self, other):
        return self._channel_name == other.get_channel_name

    def __hash__(self):
        return hash(self._channel_name)

    def get_channel_name(self):
        return self._channel_name

    def get_continuation(self):
        return [self.continuation]



class OutChannel(ChannelInstance):

    def __init__(self, channel_name: str, labels, continuation="", sender="", receiver="", send_err=False):
        super().__init__(channel_name, labels, continuation, sender, receiver)
        self._send_err = send_err

    def get_type(self) -> str:
        return f"Out[{self._channel_name},{self.get_labels_name()}] >>: {self.continuation.get_type()}"

    def get_channel_type(self):
        return f"OutChannel[{self.get_labels_name()}]"

    def get_function_body(self, indentation, function_writer, isWebsite, nestedChoice=False):
        assert (len(self._labels) == 1)
        if isWebsite and not nestedChoice:
            function_writer.add_print(f'Waiting to send {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}',
                                      indentation)
            function_writer.write_line(
                f'displayMessage += \"Expecting to send {self._labels[0].get_name()}({self._labels[0].get_payload_string()}) through channel {first_char_lower(self._channel_name)}<br/>\"',
                indentation)
            function_writer.write_line(f'possibleMessage = Map({self._labels[0].get_payload_list_string()})', indentation)
            function_writer.write_line("canRelease = true", indentation)
            function_writer.write_line("semaphore.acquire", indentation)

        function_writer.add_print(f'Sending {self.get_labels_name()} through channel {first_char_lower(self.get_channel_name())}',
                indentation)
        if self._send_err:
            function_writer.write_line(
                f'if(false){{throw RuntimeException("Some exception")}}',
                indentation)
        if isWebsite:
            function_writer.write_line(f'send({first_char_lower(self.get_channel_name())},{self._labels[0].get_payload_assign_string()}) >> {{',
                                       indentation)
        else:
            function_writer.write_line(f'send({first_char_lower(self.get_channel_name())},{self._labels[0].get_random_payload_value()}) >> {{', indentation)
        self.continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}}', indentation)


class InChannel(ChannelInstance):

    def __init__(self, channel_name: str, labels,param, continuation="", sender="", receiver=""):
        super().__init__(channel_name, labels, continuation, sender, receiver)
        self.param = param

    def get_type(self) -> str:
        labels_name = self.get_labels_name()
        return f"In[{self._channel_name}, {labels_name}, ({self.param}:{labels_name}) => {self.continuation.get_type()}]"

    def get_channel_type(self):
        return f"InChannel[{self.get_labels_name()}]"

    def get_function_body(self, indentation, function_writer, isWebsite):
        function_writer.write_line(f'receive({first_char_lower(self._channel_name)}) {{', indentation)
        function_writer.write_line(f'({first_char_lower(self.param)}:{self.get_labels_name()}) =>', indentation+1)
        if isWebsite:
            from dottygen.generator.branch import FunctionCall
            if isinstance(self.continuation, FunctionCall):
                function_writer.write_line(f"displayMessage += s\"Receiving type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
            else:
                function_writer.write_line(
                    f"displayMessage += s\"Received {self._labels[0].get_name()}({self._labels[0].get_payload_receive(first_char_lower(self.param))})"
                    f"through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
        function_writer.add_print(f'Receive type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}', indentation+1)
        self.continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}}', indentation)


class InErrChannel(ChannelInstance):

    def __init__(self, channel_name: str, labels, param, continuation="",err_continuation="", sender="", receiver=""):
        super().__init__(channel_name, labels, continuation, sender, receiver)
        self.param = param
        self._error_continuation = err_continuation

    def get_type(self) -> str:
        labels_name = self.get_labels_name()
        return f"InErr[{self._channel_name}, {labels_name}, ({self.param}:{labels_name}) => {self.continuation.get_type()}, (err:Throwable) => {self._error_continuation.get_type()}]"

    def get_channel_type(self):
        return f"InChannel[{self.get_labels_name()}]"

    def get_function_body(self, indentation, function_writer, isWebsite):
        function_writer.write_line(f'receiveErr({first_char_lower(self._channel_name)}) ({{', indentation)
        function_writer.write_line(f'({first_char_lower(self.param)}:{self.get_labels_name()}) =>', indentation+1)
        if isWebsite:
            from dottygen.generator.branch import FunctionCall
            if isinstance(self.continuation, FunctionCall):
                function_writer.write_line(f"displayMessage += s\"Receiving type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
            else:
                function_writer.write_line(
                    f"displayMessage += s\"Received {self._labels[0].get_name()}({self._labels[0].get_payload_receive(first_char_lower(self.param))})"
                    f"through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
        function_writer.add_print(f'Received type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}', indentation+1)
        self.continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}},', indentation)
        function_writer.write_line(f'{{(err : Throwable) =>', indentation)
        function_writer.add_print(f'Receive {self.get_labels_name()} through channel {first_char_lower(self._channel_name)} TIMEOUT, activating new option', indentation + 1)
        self._error_continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}}, Duration("5 seconds"))', indentation)

