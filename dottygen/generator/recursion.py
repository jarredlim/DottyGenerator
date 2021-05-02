from dottygen.generator.base import CommunicationBase

class Loop(CommunicationBase):
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

    def get_function_body(self, indentation, file_writer, isWebsite):
        file_writer.write_line(f'rec(Rec{self._role}{self._state_id}){{', indentation)
        file_writer.add_print(f'entering loop Rec{self._role}{self._state_id}', indentation + 1)
        self._body.get_function_body(indentation + 1, file_writer, isWebsite)
        file_writer.write_line(f'}}', indentation)

class Goto(CommunicationBase):

    def __init__(self, state_id, role):
        self._state_id = state_id
        self._role = role

    def get_loop_state_id(self):
        return self._state_id

    def get_continuation(self):
        return []

    def get_type(self) -> str:
        return f"Loop[Rec{self._role}{self._state_id}]"

    def get_function_body(self, indentation, file_writer, isWebsite):
        file_writer.add_print(f'go to loop Rec{self._role}{self._state_id}', indentation)
        file_writer.write_line(f'loop(Rec{self._role}{self._state_id})', indentation)