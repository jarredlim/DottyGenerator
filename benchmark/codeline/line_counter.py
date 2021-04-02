class LineCounter:

    _type_line = 0
    _function_line = 0

    def add_case_class(self, case_class):
        self._type_line += len(case_class)

    def add_types(self, types):
        for type in types:
            self._type_line += type.count('\n')

    def add_functions(self, functions):
        for func in functions:
            self._function_line += func.count('\n')

    def get_type_line(self):
        return self._type_line

    def get_function_line(self):
        return self._function_line
