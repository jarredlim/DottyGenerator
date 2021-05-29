from dottygen.generator.file_writer import RecurseTypeGenerator
import shutil, os
from dottygen.utils.folder_deleter import delete_all_folder_content

def generate_case_study(case_study):

   recurse_generator = RecurseTypeGenerator()
   recurse_generator.setup()
   sandbox_path = os.path.abspath(os.path.join("case_studies", "sandbox", "src", "main", "scala"))
   new_sandbox_path = os.path.abspath(os.path.join("case_studies", "sandbox","src", "main", "scala", case_study))
   example_path = os.path.abspath(os.path.join("case_studies", "examples", case_study))

   delete_all_folder_content(sandbox_path)
   shutil.copytree(example_path, new_sandbox_path)

   if case_study == "BankMicroservice":
       map = [("Client",8),("Client", 1), ("SvrVer", 4), ("SvrVer", 0), ("SvrAct", 6), ("SvrAct", 0), ("Svr", 8), ("Svr", 1)]
   else:
       map = [("S",4), ("A", 3), ("B",2)]

   for role, state_id in map:
       recurse_generator.add_recursion(state_id, role)
