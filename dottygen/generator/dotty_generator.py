import typing
import time
from dottygen.generator.file_writer import FunctionWriter
from dottygen.generator.choices import Selection, FunctionLambda, TypeMatch, Function, Termination, Goto, Loop
from dottygen.generator.types import Label
from dottygen.generator.channels import InChannel, OutChannel, TypeMatchChannel

from dottygen.automata.efsm import EFSM
from dottygen.utils.type_declaration_parser import DataType

class DottyGenerator:

    _efsm: EFSM
    _protocol: str
    _role: str
    _other_roles: typing.Iterable[str]
    _channel_count: typing.Dict[str, int]

    def __init__(self, efsm : EFSM, protocol : str, role: str, other_roles: typing.Iterable[str], recurse_generator):
        self._efsm = efsm
        self._protocol = protocol
        self._role = role
        self._other_roles = other_roles
        self._channel_count = {}
        self._function_list = []
        self._labels = set()
        self._function_count = 1
        self._recurse_generator = recurse_generator

    def _build_helper(self, state, visited):
        efsm = self._efsm
        if efsm.is_terminal_state(state):
            return Termination(),[]
        actions = list(state.actions)
        channel_list = []

        if state.id in visited:
            visited[state.id] += 1
            return Goto(state.id, self._role), []

        visited[state.id] = 0

        assert(state.has_channel_name)
        channel_name = state.channel_name

        if efsm.is_send_state(state):
            if(len(actions) == 1):
                continuation, channels = self._build_helper(actions[0].succ, visited)
                self._insert_channel(channel_list, channels)
                type = OutChannel(channel_name, [self._get_label(actions[0])], continuation=continuation,
                                         sender=self._role, receiver=actions[0].role)
            else:
                type = Selection(channel_name)
                for action in actions:
                    continuation, channels = self._build_helper(action.succ, visited)
                    self._insert_channel(channel_list, channels)
                    out_channel = OutChannel(channel_name, [self._get_label(action)], continuation=continuation, sender=self._role, receiver=
                                             actions[0].role)
                    type.add_continuation(out_channel)
            channel_list.insert(0, type)

        elif efsm.is_receive_state(state):
            labels = [self._get_label(action) for action in actions]
            if(len(actions) == 1):
                continuation, channels = self._build_helper(actions[0].succ, visited)
            else:
                new_function_name, match_channel_name = self._generate_type_match_function()
                match_channel = TypeMatchChannel(match_channel_name, labels)
                function_body, channels = self._type_function(actions, match_channel, visited)
                continuation = FunctionLambda(new_function_name, list(channels), "x")
                channels.insert(0, match_channel)
                self._function_list.append(Function(new_function_name, function_body, list(channels),False))
                channels.remove(match_channel)
            self._insert_channel(channel_list, channels)
            type = InChannel(channel_name, labels, continuation=continuation, sender=actions[0].role, receiver=self._role)
            type.add_lamda_param("x")
            channel_list.insert(0, type)

        if visited[state.id] > 0:
            self._recurse_generator.add_recursion(state.id, self._role)
            type = Loop(state.id, self._role, type)
        del visited[state.id]

        return type, channel_list

    def _insert_channel(self,channel_list, channels):
        for channel in channels:
            if channel.get_channel_name() not in [channel.get_channel_name() for channel in channel_list]:
                channel_list.append(channel)


    def _type_function(self, actions, match_channel, visited):
          channel_list = []
          continuations = []
          labels = []
          for action in actions:
              continuation, channels = self._build_helper(action.succ, visited)
              self._insert_channel(channel_list, channels)
              labels.append(self._get_label(action))
              continuations.append(continuation)
          return TypeMatch(labels, continuations, match_channel), channel_list

    def _get_label(self, action, add_to_list=True):
        label = Label(action.label, action.payloads)
        if add_to_list:
           self._labels.add(label)
        return label


    def _generate_type_match_function(self):
        self._function_count += 1
        return self._role + str(self._function_count), f"X{self._function_count}"

    def _get_channel_name(self, src:str, dest: str) -> str:
        if src < dest:
            channel_name = f"{src}_{dest}"
        else :
            channel_name = f"{dest}_{src}"
        channel_count = self._channel_count.get(channel_name, 0)
        channel_count+=1
        self._channel_count[channel_name] = channel_count
        return f"C_{channel_name}_{channel_count}"

    def generate_type(self):
        type = ""
        for function in self._function_list:
            type  += function.get_type()
        return type

    def generate_functions(self):
        function_writer = FunctionWriter(self._role)
        for function in self._function_list:
            function.get_function_body(1, function_writer)
            function_writer.add_empty_line(2)
        return function_writer.get_output()

    def _build_body(self):
        efsm = self._efsm
        current_state = efsm[efsm.initial_state.id]
        function_body, channels = self._build_helper(current_state, {})
        self._function_list.append(Function(self._role, function_body, list(channels), True))
        return list(channels)

    def get_function_list(self):
        self._build_body()
        return self._function_list

    def build(self, counter):

        start_time = time.time()
        channels = self._build_body()
        end_time = time.time()
        counter.add_class_time(end_time - start_time)

        start_time = time.time()
        type = self.generate_type()
        end_time = time.time()
        counter.add_type_time(end_time - start_time)

        start_time = time.time()
        function = self.generate_functions()
        end_time = time.time()
        counter.add_function_time(end_time - start_time)


        return type, function, self._labels, channels




