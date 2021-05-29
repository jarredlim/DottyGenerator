from dottygen.generator.base import CommunicationBase
from dottygen.generator.utils import first_char_lower, first_char_upper


class  Function(CommunicationBase):
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

    def get_function_body(self,indentation, function_writer, isWebsite, host=""):
        private = "" if self._is_main else "private "
        function_writer.write_line(f"{private}def {first_char_lower(self._function_name)}(", indentation)
        channel_type = ""
        for i in range(len(self._channels)):
            channel_name = first_char_lower(self._channels[i].get_channel_name())
            channel_type += f"{channel_name}.type"
            comma = "," if i != len(self._channels) - 1 else ""
            function_writer.write_line(f"{channel_name}: {self._channels[i].get_channel_type()}{comma}", indentation + 1)
            channel_type += comma
        if not self._is_main:
            function_writer.write_line(f"):{self._function_name}[{channel_type}] =", indentation)
            self._body.get_function_body(indentation + 1, function_writer, isWebsite)
        else:
            function_writer.write_line(f"):{self._function_name}[{channel_type}] ={{", indentation)
            if isWebsite:
                function_writer.add_print(f"Start running on host http://localhost:{host}", indentation + 1)
                function_writer.write_line(f"server.createContext(\"/\", new {first_char_upper(self._function_name)}RootHandler())", indentation + 1)
                function_writer.write_line("server.start()", indentation + 1)
            self._body.get_function_body(indentation+1, function_writer, isWebsite)
            function_writer.write_line(f"}}", indentation)

    def get_name(self):
        return self._function_name