from dottygen.generator.types import Type
from dottygen.generator.utils import first_char_lower

class Output(Type):

    def __init__(self, channel_name):
        self._output_channels = []
        self._channel_name = channel_name

    def get_channel_name(self):
        return self._channel_name

    def add_continuation(self, out_channel):
        self._output_channels.append(out_channel)

    def get_continuation(self):
        return [channel.continuation for channel in self._output_channels]

    def get_output_channels(self):
        return self._output_channels

    @property
    def is_selection(self):
        return len(self._output_channels) > 1

    def get_type(self) -> str:
        if not self.is_selection:
            return f"{self._output_channels[0].get_type()}"
        output = ""
        for i in range(len(self._output_channels)):
            output += f"({self._output_channels[i].get_type()})"
            if i != len(self._output_channels) - 1:
                output += "|"
        return output

    def get_function_body(self, role) -> str:
        if not self.is_selection:
            return f"{self._output_channels[0].get_function_body(role)}"
        output = f"val r = scala.util.Random\n val decision = r.nextInt({len(self._output_channels)})\n" \
                 f'print("{role}:Making selection through channel {first_char_lower(self._channel_name)}\\n")\n'
        for i in range(len(self._output_channels)):
            if i == 0:
                output += f"if(decision == 0){{\n"
            elif i == len(self._output_channels) - 1:
                output += f"else{{\n"
            else:
                output += f"else if(decision == {i}){{\n"
            output += f"{self._output_channels[i].get_function_body(role)} \n }}\n"
        return output


    def get_labels_name(self):
        label_name = ""
        for i in range(len(self._output_channels)):
            label_name += self._output_channels[i].get_labels_name()
            if i != len(self._output_channels) - 1:
                label_name += "|"
        return label_name

    def get_channel_type(self):
        return f"OutChannel[{self.get_labels_name()}]"

class Termination(Type):

     def get_type(self) -> str:
         return "PNil"

     def get_continuation(self):
         return []

     def get_function_body(self, role) -> str:
         return f'print("{role}: Terminating....\\n") \n nil'

class Goto(Type):

    def __init__(self, state_id, role):
        self._state_id = state_id
        self._role = role

    def get_loop_state_id(self):
        return self._state_id

    def get_continuation(self):
        return []

    def get_type(self) -> str:
        return f"Loop[Rec{self._role}{self._state_id}]"

    def get_function_body(self, role) -> str:
        return f'print("{role}: go to loop Rec{self._role}{self._state_id}\\n")' \
               f'\n loop(Rec{self._role}{self._state_id})'

class Loop(Type):
    def __init__(self, state_id, role, body):
        self._state_id = state_id
        self._body = body
        self._role = role

    def get_loop_state_id(self):
        return self._state_id

    def get_continuation(self):
        return [self._body]

    def get_type(self) -> str:
        return f"Rec[Rec{self._role}{self._state_id}, {self._body.get_type()}]"

    def get_function_body(self,role) -> str:
        return f'rec(Rec{self._role}{self._state_id}){{\n print("{role}: entering loop Rec{self._role}{self._state_id}\\n") ' \
               f"\n {self._body.get_function_body(role)}\n}}"

class FunctionLambda(Type):

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

    def get_function_body(self, role) -> str:
        return f"{first_char_lower(self._function_name)}({first_char_lower(self.param)}{self._get_channel_names(True)})"


class TypeMatch(Type):

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

    def get_function_body(self, role) -> str:
        body = f"{first_char_lower(self._match_channel.get_channel_name())} match {{ \n"
        for i in range(len(self._labels)):
             body += f'case {first_char_lower(self._match_channel.get_channel_name())} : {self._labels[i].get_name()} => {{ \n print("{role}: Received type {self._labels[i].get_name()} ' \
                     f'from {first_char_lower(self._match_channel.get_channel_name())}\\n")' \
                     f"\n{self._continuations[i].get_function_body(role)} \n }} \n"
        body += "}"
        return body

class  Function(Type):
    def __init__(self,function_name, body, channels, is_main):
        self._channels = channels
        self._body = body
        self._function_name = function_name
        self._is_main = is_main

    def is_main(self):
        return self._is_main

    def get_continuation(self):
        return [self._body]

    def get_type(self) -> str:
        type = f"type {self._function_name}[ \n"
        for i in range(len(self._channels)):
            type += f"{self._channels[i].get_channel_name()} <: {self._channels[i].get_channel_type()}"
            if i != len(self._channels) - 1:
                type += ",\n"
        if not self._is_main:
            type += f"] <: Process = \n {self._body.get_type()} \n\n"
        else:
            type += f"] = \n {self._body.get_type()} \n\n"
        return type

    def get_function_body(self, role) -> str:
        function = ""
        function += f"def {first_char_lower(self._function_name)}( \n"
        channel_type = ""
        for i in range(len(self._channels)):
            channel_name = first_char_lower(self._channels[i].get_channel_name())
            function += f"{channel_name}: {self._channels[i].get_channel_type()}"
            channel_type += f"{channel_name}.type"
            if i != len(self._channels) - 1:
                        function += ",\n"
                        channel_type += ","
        if not self._is_main:
            function += f"):{self._function_name}[{channel_type}] =  {self._body.get_function_body(role)} \n\n"
        else:
          function += f"):{self._function_name}[{channel_type}] ={{ \n {self._body.get_function_body(role)} \n}} \n\n"

        return function


    def get_name(self):
        return self._function_name




