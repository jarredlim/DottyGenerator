from dottygen.generator.base import CommunicationBase
from dottygen.generator.utils import first_char_lower
from dottygen.generator.channels import Channel

class TypeMatchChannel(Channel):

    def get_type(self) -> str:
        return self.get_channel_name()

    def get_function_body(self,indentation, function_writer, isWebsite):
        function_writer.writeline(first_char_lower(self.get_channel_name()), indentation)

    def get_channel_type(self):
        return f"{self.get_labels_name()}"

class TypeMatch(CommunicationBase):

    def __init__(self, labels, continuations, match_channel):
        self._match_channel = match_channel
        self._labels = labels
        self._continuations = continuations

    def get_type(self) -> str:
        type = f"{self._match_channel.get_channel_name()} match {{ \n"
        for i in range(len(self._labels)):
             type += f"case {self._labels[i].get_name()} => {self._continuations[i].get_type()} \n"
        type += "}"
        return type

    def get_labels(self):
        return self._labels

    def get_continuation(self):
        return self._continuations

    def get_function_body(self,indentation, file_writer, isWebsite):
        file_writer.write_line(f"{first_char_lower(self._match_channel.get_channel_name())} match {{", indentation)
        for i in range(len(self._labels)):
             file_writer.write_line(f'case {first_char_lower(self._match_channel.get_channel_name())} : {self._labels[i].get_name()} => {{', indentation + 1)
             if isWebsite:
                 file_writer.write_line(f"displayMessage += s\"Received {self._labels[i].get_name()}({self._labels[i].get_payload_receive(first_char_lower(self._match_channel.get_channel_name()))})<br/>\"", indentation+2)
             file_writer.add_print(f'Actual type Received from {first_char_lower(self._match_channel.get_channel_name())}: {self._labels[i].get_name()}', indentation+2)
             self._continuations[i].get_function_body(indentation + 2, file_writer, isWebsite)
             file_writer.write_line(f"}}", indentation+1)
        file_writer.write_line("}", indentation)

class FunctionLambda(CommunicationBase):

    def __init__(self, function_name, channels, param):
        self._function_name = function_name
        self._channels = channels
        self.param = param

    def get_function_name(self):
        return self._function_name

    def get_continuation(self):
        return []

    def _get_channel_names(self, is_function=False):
        channel_names = ""
        for channel in self._channels:
            channel_name = channel.get_channel_name()
            if is_function:
                channel_name = first_char_lower(channel_name)
            channel_names += f",{channel_name}"
        return channel_names

    def get_type(self) -> str:
        return f"{self._function_name}[{self.param}.type{self._get_channel_names()}]"

    def get_function_body(self, indentation, file_writer, isWebsite):
        file_writer.write_line(f"{first_char_lower(self._function_name)}({first_char_lower(self.param)}{self._get_channel_names(True)})", indentation)