from dottygen.generator.utils import get_labels_name, first_char_lower


class CaseClassGenerator():

    def __init__(self, labels):
        self._labels = labels

    def generate(self):
        case_class = ""
        for label in self._labels:
                case_class += f"case class {label.get_name()}({label.get_payload_string()})\n"
        case_class += "\n"
        return case_class

class ChannelGenerator():

    def __init__(self, channel_list, channel_map):
        self._channel_list = channel_list
        self._channel_map = channel_map

    def _get_map_index(self, channel, channel_map):
        for i in range(len(channel_map)):
            if channel.get_channel_name() in channel_map[i]:
                return i
        return -1

    def _get_channel(self, channel_name, channel_list):
        for _, channels in channel_list:
            for channel in channels:
                if channel.get_channel_name() == channel_name:
                    return channel
        return True

    def generate(self):
        channel_names = []
        channel_types = []
        for i in range(len(self._channel_map)):
            channel_set = self._channel_map[i]
            channel_labels = set()
            for channel_name in channel_set:
                 channel = self._get_channel(channel_name, self._channel_list)
                 for labels in channel.get_labels():
                     channel_labels.add(labels)
            channel_names.append(f"c{i+1}")
            channel_types.append(f"Channel[{get_labels_name(list(channel_labels))}]()")

        participant_funcs = []
        for role, channels in self._channel_list:
            participant_channels = []
            for channel in channels:
                channel_index = self._get_map_index(channel, self._channel_map)
                assert not channel_index == -1
                participant_channels.append(channel_names[channel_index])
            participant_funcs.append(f"{first_char_lower(role)}({', '.join(participant_channels)})")

        return f"val({', '.join(channel_names)}) = ({', '.join(channel_types)}) \n" \
               f"eval(par({', '.join(participant_funcs)}))\n"

