from abc import abstractmethod

from dottygen.generator.types import Type
from dottygen.generator.utils import first_char_lower

class Channel(Type):


    def __init__(self, channel_name: str, labels, continuation="", sender="", receiver = ""):
        self._channel_name = channel_name
        self.labels = labels
        self.continuation = continuation
        self.sender = sender
        self.receiver = receiver

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
        label_name = ""
        for i in range(len(self.labels)):
            label_name += self.labels[i].get_name()
            if i != len(self.labels) - 1:
                label_name += "|"
        return label_name

    def get_channel_name(self):
        return self._channel_name

    def get_continuation(self):
        return [self.continuation]



class OutChannel(Channel):

    def get_type(self) -> str:
        return f"Out[{self._channel_name},{self.get_labels_name()}] >>: {self.continuation.get_type()}"

    def get_channel_type(self):
        return f"OutChannel[{self.get_labels_name()}]"

    def get_function_body(self, role) -> str:
        return f'print("{role}:Sending {self.get_labels_name()} through channel {first_char_lower(self.get_channel_name())}\\n") \n send({first_char_lower(self.get_channel_name())},{self.labels[0].get_random_payload_value()}) >> {{\n {self.continuation.get_function_body(role)} }}'


class TypeMatchChannel(Channel):

    def get_type(self) -> str:
        return self.get_channel_name()

    def get_function_body(self, role) -> str:
        return first_char_lower(self.get_channel_name())

    def get_channel_type(self):
        return f"{self.get_labels_name()}"


class InChannel(Channel):

    def add_lamda_param(self, param):
        self.param = param

    def get_type(self) -> str:
        labels_name = self.get_labels_name()
        if len(self.labels) == 1:
            return f"In[{self._channel_name}, {labels_name}, {labels_name} => \n{self.continuation.get_type()}]"
        else :
            return f"In[{self._channel_name}, {labels_name}, ({self.param}:{labels_name}) => {self.continuation.get_type()}]"

    def get_channel_type(self):
        return f"InChannel[{self.get_labels_name()}]"

    def get_function_body(self, role) -> str:
        return f'receive({first_char_lower(self._channel_name)}) {{ \n ({first_char_lower(self.param)}:{self.get_labels_name()}) => \n print("{role}: Receive type {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}\\n") \n {self.continuation.get_function_body(role)} \n}}'






