from pathlib import Path
import shutil, os

class FileWriter:

    def append_to_file(self, output_file, output):
        f = Path(output_file)
        with f.open('a') as fp:
            fp.write(output)

class RecurseTypeGenerator:

    def __init__(self):
        self._recurse_file = f"RecurseExtend.scala"
        self._recurse_template = os.path.abspath(os.path.join("dottygen", "generator", "templates", self._recurse_file))
        self._recurse_output = os.path.abspath(os.path.join("effpi", "src", "main","scala", self._recurse_file))
        self._file_writer = FileWriter()

    def setup(self):
       if os.path.exists(self._recurse_output):
            os.remove(self._recurse_output)

       shutil.copyfile(self._recurse_template, self._recurse_output)

    def add_recursion(self, state_id, role):
        new_recursion = f'sealed abstract class Rec{role}{state_id}[A]() extends RecVar[A]("{role}{state_id}")\ncase object Rec{role}{state_id} extends Rec{role}{state_id}[Unit]\n\n'
        self._file_writer.append_to_file(self._recurse_output, new_recursion)


class FunctionWriter:

    def __init__(self, role):
        self._output = ""
        self._role = role

    def write_line(self, line, indentation, newline=True):
        for i in range(indentation):
            line = "   " + line
        if newline:
         line += "\n"
        self._output += line

    def add_print(self, line, indentation):
        self.write_line(f'print("{self._role}:{line}\\n")', indentation)

    def add_empty_line(self, lines):
        for i in range(lines):
            self._output += "\n"

    def get_output(self):
        return self._output