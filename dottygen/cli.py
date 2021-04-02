from argparse import ArgumentParser
import typing
import time

from benchmark.apigeneration.counter import Counter
from benchmark.codeline.line_counter import LineCounter
from dottygen.automata import parser as automata_parser
from dottygen.utils import logger, scribble, type_declaration_parser, role_parser
from dottygen.generator import DottyGenerator
from dottygen.generator.merger import Merger
from dottygen.generator.channel_generator import CaseClassGenerator, ChannelGenerator
from dottygen.generator.file_writer import FileWriter, RecurseTypeGenerator

def parse_arguments(args: typing.List[str]) -> typing.Dict:
    """Prepare command line argument parser and return the parsed arguments
    from the specified 'args'."""

    parser = ArgumentParser()

    parser.add_argument('filename',
                        type=str, help='Path to Scribble protocol')

    parser.add_argument('protocol',
                        type=str, help='Name of protocol')

    parser.add_argument('output',
                         type=str, help='Output directory for generation')

    parser.add_argument('env',
                        type=str, help='test or dev')

    parsed_args = parser.parse_args(args)
    return vars(parsed_args)


def main(args: typing.List[str]) -> int:
    """Main entry point, return exit code."""

    parsed_args = parse_arguments(args)

    # role = parsed_args['role']
    protocol = parsed_args['protocol']
    output_file = parsed_args['output']
    env = parsed_args['env']
    scribble_file = parsed_args['filename']
    return generate(env, output_file, protocol, scribble_file)


def generate(env, output_file, protocol, scribble_file, counter=Counter(), line_counter=LineCounter()):
    types = []
    functions = []
    labels = set()
    channel_list = []
    efsms = {}
    all_roles = role_parser.parse(scribble_file, protocol)
    recurse_generator = RecurseTypeGenerator()
    recurse_generator.setup()
    for role in all_roles:
        counter.set_role(role)
        try:
            message = f'Role {role} : Getting protocol from {scribble_file}'
            with type_declaration_parser.parse(scribble_file) as custom_types:
                start_time = time.time()
                exit_code, output = scribble.get_graph(scribble_file, protocol, role)
                end_time = time.time()
                counter.add_nuscr_time(end_time - start_time)
                if exit_code != 0:
                    logger.FAIL(message)
                    logger.ERROR(output)
                    return exit_code
                logger.SUCCESS(message)
        except (OSError, ValueError) as error:
            logger.ERROR(error)
            return 1

        phase = f'Role {role} : Parse endpoint IR from Scribble output'
        try:
            start_time = time.time()
            efsm = automata_parser.from_data(output)
            end_time = time.time()
            counter.add_efsm_time(end_time - start_time)
            efsms[role] = efsm
            logger.SUCCESS(phase)
        except ValueError as error:
            logger.FAIL(phase)
            logger.ERROR(error)
            return 1

    start_time = time.time()
    merger = Merger(efsms)
    channel_map = merger.merge()
    end_time = time.time()
    counter.add_merge_time(end_time-start_time)

    for role in all_roles:
        counter.set_role(role)
        efsm = efsms[role]
        phase = f'Role {role} : Generating Type from EFSM'
        try:
            other_roles = all_roles - set(role)
            generator = DottyGenerator(efsm=efsm, protocol=protocol, role=role, other_roles=other_roles,
                                       recurse_generator=recurse_generator)
            type, function, label, channels = generator.build(counter)
            types.append(type)
            functions.append(function)
            channel_list.append((role, channels))
            labels = labels.union(label)
            logger.SUCCESS(phase)
        except (OSError, ValueError) as error:
            logger.FAIL(phase)
            logger.ERROR(error)
            return 1
    phase = f'Writing functions and types into file'
    try:
        # print(counter.get_merge_time())
        # print(counter.get_class_time())
        # print(counter.get_efsm_time())
        # print(counter.get_type_time())
        # print(counter.get_function_time())
        # print(counter.get_nuscr_time())
        #print(counter.get_merge_time() + counter.get_class_time() + counter.get_efsm_time() + counter.get_type_time() + counter.get_function_time() + counter.get_nuscr_time())
        line_counter.add_case_class(labels)
        case_classes = CaseClassGenerator(labels).generate()
        channels_assign = ChannelGenerator(channel_list, channel_map).generate()
        fileWriter = FileWriter()
        function_string, channels_assign = ("".join(functions), channels_assign) if env != "test" else ("", "")
        line_counter.add_types(types)
        line_counter.add_functions(functions)
        fileWriter.write_to_basic_template(output_file, case_classes, "".join(types), function_string, channels_assign)
    except (OSError, ValueError) as error:
        logger.FAIL(phase)
        logger.ERROR(error)
        return 1
    return 0