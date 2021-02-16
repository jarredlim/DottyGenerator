from dottygen.generator.types import Type
from dottygen.generator.utils import first_char_lower

class Selection(Type):

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

    def get_type(self) -> str:
        output = ""
        for i in range(len(self._output_channels)):
            output += f"({self._output_channels[i].get_type()})"
            if i != len(self._output_channels) - 1:
                output += "|"
        return output

    def get_function_body(self, indentation, function_writer):
        function_writer.write_line("val r = scala.util.Random", indentation)
        function_writer.write_line(f'val decision = r.nextInt({len(self._output_channels)})', indentation)
        function_writer.add_print(f'Making selection through channel {first_char_lower(self._channel_name)}', indentation)
        for i in range(len(self._output_channels)):
            if i == 0:
                function_writer.write_line(f"if(decision == 0){{", indentation)
            elif i == len(self._output_channels) - 1:
                function_writer.write_line(f"else{{", indentation)
            else:
                function_writer.write_line(f"else if(decision == {i}){{\n", indentation)
            self._output_channels[i].get_function_body(indentation + 1, function_writer)
            function_writer.write_line(f"}}", indentation)


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

     def get_function_body(self, indentation, function_writer):
         function_writer.add_print("Terminating...", indentation)
         function_writer.write_line('nil', indentation)

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

    def get_function_body(self, indentation, file_writer):
        file_writer.add_print(f'print("go to loop Rec{self._role}{self._state_id}', indentation)
        file_writer.write_line(f'loop(Rec{self._role}{self._state_id})', indentation)

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

    def get_function_body(self, indentation, file_writer):
        file_writer.write_line(f'rec(Rec{self._role}{self._state_id}){{', indentation)
        file_writer.add_print(f'print("entering loop Rec{self._role}{self._state_id}")', indentation + 1)
        self._body.get_function_body(indentation + 1, file_writer)
        file_writer.write_line(f'}}', indentation)
        # return f'rec(Rec{self._role}{self._state_id}){{\n print("{role}: entering loop Rec{self._role}{self._state_id}\\n") ' \
        #        f"\n {self._body.get_function_body(role)}\n}}"

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

    def get_function_body(self, indentation, file_writer):
        file_writer.write_line(f"{first_char_lower(self._function_name)}({first_char_lower(self.param)}{self._get_channel_names(True)})", indentation)


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

    def get_function_body(self,indentation, file_writer):
        file_writer.write_line(f"{first_char_lower(self._match_channel.get_channel_name())} match {{", indentation)
        for i in range(len(self._labels)):
             file_writer.write_line(f'case {first_char_lower(self._match_channel.get_channel_name())} : {self._labels[i].get_name()} => {{', indentation + 1)
             file_writer.add_print(f'Received type {self._labels[i].get_name()} from {first_char_lower(self._match_channel.get_channel_name())}', indentation+2)
             self._continuations[i].get_function_body(indentation + 2, file_writer)
             file_writer.write_line(f"}}", indentation+1)
        file_writer.write_line("}", indentation)

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

    def get_function_body(self,indentation, function_writer):
        function_writer.write_line(f"def {first_char_lower(self._function_name)}(", indentation)
        channel_type = ""
        for i in range(len(self._channels)):
            channel_name = first_char_lower(self._channels[i].get_channel_name())
            channel_type += f"{channel_name}.type"
            comma = "," if i != len(self._channels) - 1 else ""
            function_writer.write_line(f"{channel_name}: {self._channels[i].get_channel_type()}{comma}", indentation + 1)
            channel_type += comma
        if not self._is_main:
            function_writer.write_line(f"):{self._function_name}[{channel_type}] =", indentation)
            self._body.get_function_body(indentation + 1, function_writer)
        else:
            function_writer.write_line(f"):{self._function_name}[{channel_type}] ={{", indentation)
            self._body.get_function_body(indentation+1, function_writer)
            function_writer.write_line(f"}}", indentation)



    def get_name(self):
        return self._function_name




