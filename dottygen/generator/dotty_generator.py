import typing
import time
from dottygen.generator.file_writer import FunctionWriter
from dottygen.generator.selection import Selection
from dottygen.generator.base import Termination, Label
from dottygen.generator.function import Function
from dottygen.generator.recursion import Goto, Loop
from dottygen.generator.branch import FunctionCall, TypeMatch, TypeMatchParam
from dottygen.generator.channels import InChannel, OutChannel, InErrChannel

from dottygen.automata.efsm import EFSM
from dottygen.utils.type_declaration_parser import DataType

class DottyGenerator:

    _efsm: EFSM
    _protocol: str
    _role: str
    _other_roles: typing.Iterable[str]

    def __init__(self, efsm : EFSM, protocol : str, role: str, other_roles: typing.Iterable[str], recurse_generator, isWebsite, host):
        self._efsm = efsm
        self._protocol = protocol
        self._role = role
        self._other_roles = other_roles
        self._function_list = []
        self._labels = set()
        self._function_count = 1
        self._recurse_generator = recurse_generator
        self._isWebsite = isWebsite
        self._host = host

    def _build_helper(self, state, visited):
        efsm = self._efsm
        if efsm.is_terminal_state(state):
            return Termination(state.is_unreachable),[]
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
                                  sender=self._role, receiver=actions[0].role, send_err=state.has_send_error)
            else:
                type = Selection(channel_name)
                for action in actions:
                    continuation, channels = self._build_helper(action.succ, visited)
                    self._insert_channel(channel_list, channels)
                    out_channel = OutChannel(channel_name, [self._get_label(action)], continuation=continuation, sender=self._role, receiver=
                                             actions[0].role, send_err=state.has_send_error)
                    type.add_continuation(out_channel)
            self._insert_channel(channel_list, [type], True)

        elif efsm.is_receive_state(state):
            labels = [self._get_label(action) for action in actions]#
            param_name = "x"
            if(len(actions) == 1):
                continuation, channels = self._build_helper(actions[0].succ, visited)
            else:
                is_all_terminal = True
                for action in actions:
                    if not efsm.is_terminal_state(action.succ) or action.succ.is_unreachable:
                        is_all_terminal = False
                if is_all_terminal:
                    continuation, channels = Termination(), []
                else:
                    new_function_name, param_name = self._generate_type_match_function()
                    match_channel = TypeMatchParam(param_name, labels)
                    function_body, channels = self._type_function(actions, match_channel, visited)
                    channels.insert(0, match_channel)
                    continuation = FunctionCall(new_function_name, list(channels))
                    self._function_list.append(Function(new_function_name, function_body, list(channels),False))
                    channels.remove(match_channel)
            self._insert_channel(channel_list, channels)
            if efsm.is_error_detection_state(state):
                err_continuation, channels = self._build_helper(state.error_detection.succ, visited)
                self._insert_channel(channel_list, channels)
                type = InErrChannel(channel_name, labels, param_name, continuation=continuation,err_continuation=err_continuation, sender=actions[0].role, receiver=self._role)
            else:
                type = InChannel(channel_name, labels, param_name, continuation=continuation, sender=actions[0].role, receiver=self._role)
            self._insert_channel(channel_list, [type], True)

        if visited[state.id] > 0:
            self._recurse_generator.add_recursion(state.id, self._role)
            type = Loop(state.id, self._role, type)
        del visited[state.id]

        return type, channel_list

    def _insert_channel(self,channel_list, channels, insertFirst=False):
        for channel in channels:
            if channel.get_channel_name() not in [channel.get_channel_name() for channel in channel_list]:
                if insertFirst:
                    channel_list.insert(0, channel)
                else:
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
        return self._role +"_" + str(self._function_count), f"X_{self._function_count}"

    def generate_type(self):
        type = ""
        for function in self._function_list:
            type  += function.get_type()
        return type

    def generate_functions(self):
        function_writer = FunctionWriter(self._role)
        for function in self._function_list:
            function.get_function_body(1, function_writer, self._isWebsite, self._host)
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




