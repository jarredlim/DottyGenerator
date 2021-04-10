from dottygen.cli import generate
from .line_counter import LineCounter
from benchmark.apigeneration.counter import Counter
import random
import os
import shutil
import matplotlib.pyplot as plt

parent_output_dir = os.path.abspath(os.path.join('benchmark', 'apigeneration', 'scr-sandbox'))
output_file = os.path.join(parent_output_dir, 'test.scr')
two_participant_template_path = os.path.abspath(os.path.join('benchmark', 'apigeneration', 'scr-templates', 'BasicTwoParticipants.scr'))
two_participants = ["Client", "Svr"]
payloads = ["number", "string"]

def _init_sandbox_():
    if os.path.exists(parent_output_dir) and os.path.isdir(parent_output_dir):
        shutil.rmtree(parent_output_dir)
    os.mkdir(parent_output_dir)

def multiple_loops_two_participant_protocol(label,payload_string1, payload_string2, participant, j, protocol):

            return f"rec Loop{j} {{ " \
                       f" choice at {two_participants[participant]} {{ \n HELLO{label[0]}({payload_string1}) from {two_participants[participant]} to {two_participants[1- participant]}; \n \n continue Loop{j}; \n }} or {{ \n HELLO{label[1]}({payload_string2}) from {two_participants[participant]} to {two_participants[1- participant]}; \n {protocol} \n }} }}"

def nested_choices_two_participant_protocol(label, payload_string1, payload_string2, participant, protocol):

           return f"choice at {two_participants[participant]} {{ \n HELLO{label[0]}({payload_string1}) from {two_participants[participant]} to {two_participants[1 - participant]}; \n {protocol} \n }}\n" \
                        f"or {{ \n HELLO{label[1]}({payload_string2}) from {two_participants[participant]} to {two_participants[1 - participant]}; }} \n"





def multiple_send_receive_two_participant_protocols(label, payload_string, participant):

      return f"HELLO{label}({payload_string}) from {two_participants[participant]} to {two_participants[1-participant]}; \n"




def generate_code_line(test_name):
    x = []
    function_line = []
    type_line = []

    max_range = 10 if test_name == "loop" else 100

    for i in range(1, max_range):
        protocol = ""
        counter = LineCounter()
        for j in range(1, i):

            participant = random.randint(0, 1)
            label = random.sample(range(1, 100), 2)
            payload = random.sample(range(1, 10), 2)
            payload_string1 = ""
            payload_string2 = ""

            for k in range(payload[0]):
                curr_payload = random.randint(0, 1)
                payload_string1 += payloads[curr_payload]
                if k != payload[0] - 1:
                    payload_string1 += ", "

            for k in range(payload[1]):
                curr_payload = random.randint(0, 1)
                payload_string2 += payloads[curr_payload]
                if k != payload[1] - 1:
                    payload_string2 += ", "

            if test_name == "send_receive":
              protocol += multiple_send_receive_two_participant_protocols(label[0], payload_string1, participant)
            elif test_name == "choices":
                protocol = nested_choices_two_participant_protocol(label, payload_string1, payload_string2, participant, protocol)
            elif test_name == "loop":
                protocol = multiple_loops_two_participant_protocol(label,payload_string1, payload_string2, participant, j, protocol)


        _init_sandbox_()
        shutil.copyfile(two_participant_template_path, output_file)
        with open(output_file) as f:
            newText = f.read().replace('PROTOCOLS', protocol)

        with open(output_file, "w") as f:
            f.write(newText)

        generate("dev", "/home/dev/effpi_sandbox/dotty_types.scala", "Test", output_file, Counter(), counter)
        x.append(i)
        function_line.append(counter.get_function_line())
        type_line.append(counter.get_type_line())

    plot_graph(x, function_line, type_line, test_name)


def plot_graph(x, function_line, type_line, test_name):

    if test_name == "loop":
        test_type = "Loops"
    elif test_name =="choices":
        test_type = " Choices"
    else:
        test_type = "Send/Receives"

    plt.plot(x, function_line, label="Lines of Function")
    plt.plot(x, type_line, label="Lines of Type")
    plt.xlabel(f'Number of {test_type}')
    plt.ylabel('Time taken (s)')
    plt.title(f'Multiple {test_type} with Two Participants')
    plt.legend()
    plt.savefig(f"benchmark/codeline/graphs/test_{test_name}.png")
