from abc import abstractmethod

from dottygen.generator.base import CommunicationBase
from dottygen.generator.utils import first_char_lower, get_labels_name

class Channel(CommunicationBase):


    def __init__(self, channel_name: str, labels, continuation="", sender="", receiver = "", is_nested=False, nested_type=None):
        self._channel_name = channel_name
        self.labels = labels
        self.continuation = continuation
        self.sender = sender
        self.receiver = receiver
        self.is_nested = is_nested
        self._nested_type = nested_type

    @abstractmethod
    def get_channel_type(self):
        pass

    def get_sender(self):
        return self.sender

    def get_receiver(self):
        return self.receiver

    def get_labels(self):
        return self.labels

    def get_labels_name(self):
        return get_labels_name(self.labels)

    def __eq__(self, other):
        return self._channel_name == other.get_channel_name

    def __hash__(self):
        return hash(self._channel_name)

    def get_channel_name(self):
        return self._channel_name

    def get_continuation(self):
        return [self.continuation]



class OutChannel(Channel):

    def get_type(self) -> str:
        if self.is_nested:
          return f"Out[{self.get_channel_type()},{self.get_labels_name()}] >>: {self.continuation.get_type()}"

        return f"Out[{self._channel_name},{self.get_labels_name()}] >>: {self.continuation.get_type()}"

    def get_channel_type(self):
        if self.is_nested:
            return f"OutChannel[{self._nested_type}]"
        return f"OutChannel[{self.get_labels_name()}]"

    def get_function_body(self, indentation, function_writer, isWebsite, nestedChoice=False):
        if isWebsite and not nestedChoice:
            function_writer.add_print(f'Waiting to send {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}',
                                      indentation)
            function_writer.write_line(
                f'displayMessage += \"Expecting to send {self.labels[0].get_name()}({self.labels[0].get_payload_string()}) through channel {first_char_lower(self._channel_name)}<br/>\"',
                indentation)
            function_writer.write_line(f'possibleMessage = Map({self.labels[0].get_payload_list_string()})', indentation)
            function_writer.write_line("canRelease = true", indentation)
            function_writer.write_line("semaphore.acquire", indentation)

        function_writer.add_print(f'Sending {self.get_labels_name()} through channel {first_char_lower(self.get_channel_name())}',
                indentation)
        if isWebsite:
            function_writer.write_line(f'send({first_char_lower(self.get_channel_name())},{self.labels[0].get_payload_assign_string()}) >> {{',
                    indentation)
        else:
            function_writer.write_line(f'send({first_char_lower(self.get_channel_name())},{self.labels[0].get_random_payload_value()}) >> {{', indentation)
        self.continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}}', indentation)


class InChannel(Channel):

    def add_lamda_param(self, param):
        self.param = param

    def get_type(self) -> str:
        labels_name = self.get_labels_name()
        if self.is_nested:
            return f"In[{self.get_channel_type()}, {labels_name}, ({self.param}:{labels_name}) => {self.continuation.get_type()}]"
        else:
            return f"In[{self._channel_name}, {labels_name}, ({self.param}:{labels_name}) => {self.continuation.get_type()}]"

    def get_channel_type(self):
        return f"InChannel[{self.get_labels_name()}]"

    def get_function_body(self, indentation, function_writer, isWebsite):
        function_writer.write_line(f'receive({first_char_lower(self._channel_name)}) {{', indentation)
        function_writer.write_line(f'({first_char_lower(self.param)}:{self.get_labels_name()}) =>', indentation+1)
        if isWebsite:
            from dottygen.generator.branch import FunctionLambda
            if isinstance(self.continuation, FunctionLambda):
                function_writer.write_line(f"displayMessage += s\"Receiving type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
            else:
                function_writer.write_line(
                    f"displayMessage += s\"Received {self.labels[0].get_name()}({self.labels[0].get_payload_receive(first_char_lower(self.param))})"
                    f"through channel {first_char_lower(self._channel_name)}<br/>\"", indentation+1)
        function_writer.add_print(f'Receive type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}', indentation+1)
        self.continuation.get_function_body(indentation + 1, function_writer, isWebsite)
        function_writer.write_line(f'}}', indentation)






