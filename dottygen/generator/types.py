from abc import ABC, abstractmethod
from random import randrange
import random, string

class Type(ABC):

    @abstractmethod
    def get_type(self) -> str:
        raise NotImplementedError('Action.add_to_efsm')

    @abstractmethod
    def get_function_body(self, indentation, file_writer):
        raise NotImplementedError('Action.add_to_efsm')

    @abstractmethod
    def get_continuation(self):
        raise NotImplementedError('Action.add_to_efsm')

class Label():

    def __init__(self, name, payload):
        self._name = name
        self._payload = payload

    def get_name(self):
        return self._name

    def __eq__(self, other):
       return self._name == other.get_name()

    def __hash__(self):
        return hash(self._name)

    def get_payload(self):
        return self._payload

    def _convert_payload_type(self, type):
        if "string" in type:
            return "String"
        elif "date" in type:
            return "Date"
        elif "number" in type:
            return "Int"
        return type

    def get_random_payload_value(self):
        output = f"{self._name}("
        for i in range(len(self._payload)):
            if "number" in self._payload[i] or "Int" in self._payload[i]:
                output += str(randrange(100))
            elif "string" in self._payload[i] or "String" in self._payload[i]:
                output_string = ''.join(random.choice(string.ascii_lowercase) for _ in range(10))
                output += f'"{output_string}"'
            elif "date" in self._payload[i] or "Date" in self._payload[i]:
                output += f'new Date()'
            if i != len(self._payload) - 1:
                output += ","
        output += ")"
        return output

    def get_payload_string(self):
        payload_string = ""
        for i in range(len(self._payload)):
            if ":" in self._payload[i]:
                payload_var = ""
                index = self._payload[i].index(":") + 1
                payload_type = self._payload[i][:index] + self._convert_payload_type(self._payload[i][index :])
            else:
                payload_var =  f"x{i+1}:"
                payload_type = self._convert_payload_type(self._payload[i])
            payload_string += f"{payload_var}{payload_type}"
            if i != len(self._payload) - 1:
                payload_string += ","
        return payload_string

