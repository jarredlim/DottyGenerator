from dottygen.generator.types import Type

class NestedCaseClass():

    def __init__(self, continuation, name, label):
        self._continuation = continuation
        self._generated_class = name
        self._label = label

    def generate_class(self):
        return f"case class {self._label.get_name()}({self._label.get_payload_string()}) extends {self._generated_class} {{\n" \
               f"  type RetTy = {self._continuation.get_type()}\n" \
               f"}}\n\n"

    def __eq__(self, other):
        return self._label.get_name() == other.get_class_name()

    def __hash__(self):
        return hash(self._label.get_name())

    def get_class_name(self):
        return self._label.get_name()

class NestedFunctionLambda(Type):
    def __init__(self, param):
        self.param = param

    def get_type(self) -> str:
        return f"{self.param}.RetTy"

    def get_continuation(self):
        return []

    def get_function_body(self, indentation, file_writer):
        pass

def generate_sealed_class(type):
    return f"sealed abstract class {type} extends HasRetTy {{\n" \
           f"type RetTy <: Process \n}}\n\n"

