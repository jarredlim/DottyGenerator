from dottygen.generator.base import CommunicationBase
from dottygen.generator.utils import first_char_lower, first_char_upper

class Selection(CommunicationBase):

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
        return f"({output})"

    def get_function_body(self, indentation, function_writer, isWebsite):
        labels = self.get_labels()
        if isWebsite:
            function_writer.add_print(f'Waiting selection {self.get_labels_name()} through channel {first_char_lower(self._channel_name)}',
                                      indentation)
            function_writer.write_line(
                f'displayMessage += \"Expecting to send {self._get_expected_string()} through channel {first_char_lower(self._channel_name)}<br/>\"',
                indentation)
            function_writer.write_line(f'possibleMessage = Map({",".join(self._get_map_string())})', indentation)
            function_writer.write_line("canRelease = true", indentation)
            function_writer.write_line("semaphore.acquire", indentation)
        else:
            function_writer.write_line("val r = scala.util.Random(System.currentTimeMillis())", indentation)
            function_writer.write_line(f'val decision = r.nextInt({len(self._output_channels)})', indentation)
            function_writer.add_print(f'Making selection through channel {first_char_lower(self._channel_name)}', indentation)
        for i in range(len(self._output_channels)):
            if i == 0:
                if isWebsite:
                    function_writer.write_line(f"if( choice == \"{labels[i].get_name()}\"){{", indentation)
                else:
                    function_writer.write_line(f"if(decision == 0){{", indentation)
            elif i == len(self._output_channels) - 1:
                function_writer.write_line(f"else{{", indentation)
            else:
                if isWebsite:
                    function_writer.write_line(f"else if( choice == \"{labels[i].get_name()}\"){{\n", indentation)
                else:
                    function_writer.write_line(f"else if(decision == {i}){{\n", indentation)
            self._output_channels[i].get_function_body(indentation + 1, function_writer, isWebsite, isWebsite)
            function_writer.write_line(f"}}", indentation)

    def get_labels_name(self):
        label_name = ""
        for i in range(len(self._output_channels)):
            label_name += self._output_channels[i].get_labels_name()
            if i != len(self._output_channels) - 1:
                label_name += "|"
        return label_name

    def get_labels(self):
        labels = []
        for i in range(len(self._output_channels)):
            labels += self._output_channels[i].get_labels()
        return labels

    def _get_expected_string(self):
        labels = self.get_labels()
        label_strings = [f"{label.get_name()}({label.get_payload_string()})" for label in labels]
        return " or ".join(label_strings)

    def _get_map_string(self):
        label_string = []
        for output_channel in self._output_channels:
            label_string += [label.get_payload_list_string() for label in output_channel.get_labels()]
        return label_string

    def get_channel_type(self):
        return f"OutChannel[{self.get_labels_name()}]"





