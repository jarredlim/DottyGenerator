from dottygen.utils.folder_deleter import delete_all_folder_content
import shutil, os

basic_template_path =  os.path.abspath(os.path.join('dottygen','generator', "templates", "basic.scala"))

main_template_path =  os.path.abspath(os.path.join('dottygen','generator', "templates", "main.scala"))
type_template_path =  os.path.abspath(os.path.join('dottygen','generator', "templates", "type.scala"))
implementation_template_path =  os.path.abspath(os.path.join('dottygen','generator', "templates", "implementation.scala"))
case_class_template_path =  os.path.abspath(os.path.join('dottygen','generator', "templates", "case_class.scala"))

class OutputGenerator:

    def __init__(self):
        self._functions = {}
        self._effpi_types = {}

    def add_function(self, role, function):
        self._functions[role] = function

    def add_type(self, role, type):
        self._effpi_types[role] = type

    def single_output(self, output_folder, case_classes, channels_assign, protocol):
         delete_all_folder_content(output_folder)
         output_file = os.path.join(output_folder, f'{protocol}.scala')
         shutil.copyfile(basic_template_path,output_file)
         with open(output_file) as f:
             newText = f.read().replace('CASE_CLASSES', case_classes).\
                       replace("EFFPI_TYPES", "".join(self._effpi_types.values())).\
                       replace("FUNCTIONS", "".join(self._functions.values())).\
                       replace("CHANNEL_ASSIGN", channels_assign).replace("PROTOCOL", protocol)

         with open(output_file, "w") as f:
             f.write(newText)

    def batch_output(self, output_folder, case_classes, channels_assign, protocol, roles):
        delete_all_folder_content(output_folder)
        import_implementations = ""
        import_types = ""

        for role in roles:
            os.mkdir(os.path.join(output_folder, role))
            type_file = os.path.join(output_folder, role, f'types.scala')
            shutil.copyfile(type_template_path, type_file)
            with open(type_file) as f:
                newText = f.read().replace('ROLE', role).replace('TYPES', self._effpi_types[role])
            with open(type_file, "w") as f:
                f.write(newText)

            implementation_file = os.path.join(output_folder, role, f'implementation.scala')
            shutil.copyfile(implementation_template_path, implementation_file)
            with open(implementation_file) as f:
                newText = f.read().replace('ROLE', role).replace('IMPLEMENTATIONS', self._functions[role])
            with open(implementation_file, "w") as f:
                f.write(newText)

            import_types += f"import effpi_sandbox.{role}.types._\n"
            import_implementations += f"import effpi_sandbox.{role}.implementation._\n"

        output_file = os.path.join(output_folder, f'{protocol}.scala')
        shutil.copyfile(main_template_path, output_file)
        with open(output_file) as f:
            newText = f.read().replace('PROTOCOL', protocol).replace('IMPORT_TYPES', import_types).\
                replace('IMPORT_IMPLEMENTATIONS', import_implementations).replace('CHANNEL_ASSIGN', channels_assign)
        with open(output_file, "w") as f:
            f.write(newText)

        output_file = os.path.join(output_folder, 'case_class.scala')
        shutil.copyfile(case_class_template_path, output_file)
        with open(output_file) as f:
            newText = f.read().replace('CASE_CLASSES', case_classes)
        with open(output_file, "w") as f:
            f.write(newText)

