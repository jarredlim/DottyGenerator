import os
import shutil
import unittest

from dottygen.automata import parser as automata_parser
from dottygen.utils import scribble, logger, role_parser, type_declaration_parser
from dottygen.generator import DottyGenerator
from dottygen.generator.file_writer import RecurseTypeGenerator
from dottygen.generator.selection import Selection
from dottygen.generator.branch import FunctionCall, TypeMatch
from dottygen.generator.recursion import Loop, Goto
from dottygen.generator.function import Function
from dottygen.generator.base import Termination, Label
from dottygen.generator.channels import InChannel, OutChannel

def get_test_class(filename, protocol):

    filename = os.path.abspath(os.path.join('benchmark', 'TestMap','examples', filename))

    class CodeGenerationTest(unittest.TestCase):

        def setUp(self):
            pass

        def get_scribble_ouput(self, role):
            message = f'Role {role} : Getting protocol {protocol} from {filename}'
            with type_declaration_parser.parse(filename) as custom_types:
                exit_code, output = scribble.get_graph(filename, protocol, role)
                if exit_code != 0:
                    logger.FAIL(message)
                    logger.ERROR(output)
                else:
                    logger.SUCCESS(message)

                self.assertEqual(exit_code, 0)
                return output

        def get_efsm(self, output, role):
            phase = f'Role {role} : Generating EFSM from Scribble output'
            try:
                efsm = automata_parser.from_data(output)
                logger.SUCCESS(phase)
                return efsm
            except ValueError as error:
                logger.FAIL(phase)
                logger.ERROR(error)
                self.assertTrue(False)

        def get_all_roles(self):
            return role_parser.parse(filename, protocol)


    class MapTest(CodeGenerationTest):

        def _check_send(self, state, body, role):
             self.assertIsInstance(body, OutChannel)
             self.assertEqual(body.get_sender(), role)
             actions = list(state.actions)
             self.assertEqual(body.get_receiver(), actions[0].role)
             labels = body.get_labels()
             self._check_labels_send_receive(labels,actions)
             self.assertEqual(len(body.get_continuation()), 1)
             return [(actions[0].succ, body.get_continuation()[0])]

        def _check_labels_send_receive(self, labels, actions):
            self.assertEqual(len(labels), 1)
            label = labels[0]
            self.assertIsInstance(label, Label)
            self.assertEqual(actions[0].label, label.get_name())
            for i in range(len(actions[0].payloads)):
                self.assertEqual(actions[0].payloads[i], label.get_payload()[i])

        def _check_receive(self, state, body, role):
            self.assertIsInstance(body, InChannel)
            self.assertEqual(body.get_receiver(), role)
            actions = list(state.actions)
            self.assertEqual(body.get_sender(), actions[0].role)
            labels = body.get_labels()
            self._check_labels_send_receive(labels,actions)
            self.assertEqual(len(body.get_continuation()), 1)
            return [(actions[0].succ, body.get_continuation()[0])]

        def _check_selection(self, state, body, role):
            self.assertIsInstance(body, Selection)
            output_channels = body.get_output_channels()
            actions = list(state.actions)
            self.assertEqual(len(output_channels), len(actions))
            continuations = []
            for i in range(len(actions)):
                output_channel = output_channels[i]
                self.assertIsInstance(output_channel, OutChannel)
                self.assertEqual(output_channel.get_sender(), role)
                self.assertEqual(output_channel.get_receiver(), actions[i].role)
                labels = output_channel.get_labels()
                self._check_labels_send_receive(labels, [actions[i]])
                self.assertEqual(len(output_channel.get_continuation()), 1)
                continuations.append((actions[i].succ, output_channel.get_continuation()[0]))
            return continuations

        def _check_branch(self, state, body, role):
            self.assertIsInstance(body, InChannel)
            actions = list(state.actions)
            labels = body.get_labels()
            self.assertEqual(len(actions), len(labels))
            for i in range(len(labels)):
                self.assertEqual(body.get_sender(), actions[i].role)
                self.assertEqual(body.get_receiver(), role)
                self._check_labels_send_receive([labels[i]],[actions[i]])
            continuations = body.get_continuation()
            self.assertEqual(len(continuations), 1)
            next_function = self._get_function(continuations[0])
            continuations = next_function.get_continuation()
            self.assertEqual(len(continuations), 1)
            function_body = continuations[0]
            self.assertIsInstance(function_body, TypeMatch)
            labels = function_body.get_labels()
            continuations = function_body.get_continuation()
            self.assertEqual(len(labels), len(continuations))
            self.assertEqual(len(labels),len(actions))
            results = []
            for i in range(len(labels)):
                self._check_labels_send_receive([labels[i]],[actions[i]])
                results.append((actions[i].succ, continuations[i]))
            return results

        def _get_function(self, function_lambda):
            self.assertIsInstance(function_lambda, FunctionCall)
            self.assertEqual(len(function_lambda.get_continuation()), 0)
            next_function_name = function_lambda.get_function_name()
            function_count = 0
            for function in self.function_list:
                if function.get_name() == next_function_name:
                    next_function = function
                    function_count += 1
            self.assertEqual(function_count, 1)
            return next_function

        def _check_termination(self,body):
            self.assertIsInstance(body, Termination)
            self.assertEqual(len(body.get_continuation()), 0)
            return []

        def _check_loop(self,state,visited, body):
            self.assertIsInstance(body, Loop)
            visited[state.id] = body.get_loop_state_id()
            new_body = body.get_continuation()
            self.assertEqual(len(new_body), 1)
            return new_body[0]

        def _check_go_to(self, body, visited, state):
             self.assertIsInstance(body, Goto)
             self.assertNotEqual(visited[state.id], -1)
             self.assertEqual(visited[state.id], body.get_loop_state_id())
             self.assertEqual(len(body.get_continuation()), 0)
             return []


        def _get_description(self, actions):
            label_payload = [f"{action.label}({','.join(action.payloads)})" for action in actions]
            return ','.join(label_payload)

        def perform_check(self,efsm, state, body, visited, loop_set, role):
            phase = f"Testing State {state.id} of role {role}: "
            try:
                is_visited = state.id in visited.keys()
                self.assertFalse(state.id not in loop_set and is_visited)
                if state.id in loop_set and not is_visited:
                    phase += f"Test Loop"
                    body = self._check_loop(state, visited, body)

                if efsm.is_terminal_state(state):
                   phase += f"Test Termination"
                   continuations =  self._check_termination(body)

                if state.id in loop_set and is_visited:
                    phase += f"Test Go to"
                    self._check_go_to(body, visited, state)
                    logger.SUCCESS(phase)
                    return

                if efsm.is_send_state(state) and len(list(state.actions)) == 1:
                   phase += f"Test Message Sending of {self._get_description(list(state.actions))} to {list(state.actions)[0].role}"
                   continuations = self._check_send(state, body, role)

                if efsm.is_receive_state(state) and len(list(state.actions)) == 1:
                    phase += f"Test Message Receiving of {self._get_description(list(state.actions))} from {list(state.actions)[0].role}"
                    continuations = self._check_receive(state, body, role)

                if efsm.is_send_state(state) and len(list(state.actions)) > 1:
                   phase += f"Test Selection of {self._get_description(list(state.actions))} to {list(state.actions)[0].role}"
                   continuations = self._check_selection(state, body, role)

                if efsm.is_receive_state(state) and len(list(state.actions)) > 1:
                    phase += f"Test Branch of {self._get_description(list(state.actions))} from {list(state.actions)[0].role}"
                    continuations = self._check_branch(state, body, role)

            except AssertionError as e:
                logger.FAIL(phase)
                raise

            logger.SUCCESS(phase)
            if state.id not in loop_set:
                visited[state.id] = -1
            for next_state, next_body in continuations:
                self.perform_check(efsm, next_state,next_body, visited, loop_set, role)
            del visited[state.id]


        def _get_loop_state(self, efsm, state, visited, loop_set):
            if efsm.is_terminal_state(state):
                return
            if state.id in visited:
                loop_set.add(state.id)
                return
            visited.add(state.id)
            for action in list(state.actions):
                self._get_loop_state(efsm, action.succ, visited, loop_set)


        def test_code_generation(self):
            for role in role_parser.parse(filename,protocol):
                efsm = self.get_efsm(self.get_scribble_ouput(role), role)
                other_roles = self.get_all_roles() - set(role)
                generator = DottyGenerator(efsm=efsm, protocol=protocol, role=role, other_roles=other_roles,
                                           recurse_generator=RecurseTypeGenerator())
                self.function_list = generator.get_function_list()
                i = 0
                for function in self.function_list:
                    self.assertIsInstance(function, Function)
                    if function.is_main():
                        i+=1
                        main_body = function.get_continuation()
                if i == 0:
                    logger.ERROR(f"Role {role} :No main function found")
                elif i > 1:
                    logger.ERROR(f"Role {role} :More than one main function found")
                self.assertEqual(i,1)
                self.assertEqual(len(main_body), 1)
                loop_set = set()
                self._get_loop_state(efsm, efsm[efsm.initial_state.id],set(), loop_set)
                self.perform_check(efsm, efsm[efsm.initial_state.id], main_body[0], {},loop_set, role)
            print()

    test_name = f'{filename}: {protocol}>'
    MapTest.__name__ = test_name
    MapTest.__qualname__ = test_name
    return MapTest







