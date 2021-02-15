from argparse import ArgumentParser
import typing

from dottygen.automata import parser as automata_parser
from dottygen.utils import logger, scribble, type_declaration_parser, role_parser
from dottygen.generator import DottyGenerator
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


def _generate_case_classes(labels):
    case_class = ""
    for label in labels:
        case_class += f"case class {label.get_name()}({label.get_payload_string()})\n"
    case_class += "\n"
    return case_class


def main(args: typing.List[str]) -> int:
    """Main entry point, return exit code."""

    parsed_args = parse_arguments(args)

    # role = parsed_args['role']
    protocol = parsed_args['protocol']
    output_file = parsed_args['output']
    env = parsed_args['env']
    scribble_file = parsed_args['filename']
    types = []
    functions = []
    labels = set()
    all_roles = role_parser.parse(parsed_args['filename'], parsed_args['protocol'])
    recurse_generator = RecurseTypeGenerator()
    recurse_generator.setup()

    for role in all_roles:
        try:
            message = f'Role {role} : Getting protocol from {scribble_file}'
            with type_declaration_parser.parse(scribble_file) as custom_types:
                exit_code, output = scribble.get_graph(scribble_file, protocol, role)
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
            efsm = automata_parser.from_data(output)
            logger.SUCCESS(phase)
        except ValueError as error:
            logger.FAIL(phase)
            logger.ERROR(error)
            return 1

        phase = f'Role {role} : Generating Type from EFSM'
        try:
            other_roles = all_roles - set(role)
            generator = DottyGenerator(efsm=efsm, protocol=protocol, role=role, other_roles=other_roles, recurse_generator=recurse_generator)
            type, function, label = generator.build(output_file)
            types.append(type)
            functions.append(function)
            labels = labels.union(label)
            logger.SUCCESS(phase)
        except (OSError, ValueError) as error:
            logger.FAIL(phase)
            logger.ERROR(error)
            return 1

    phase = f'Writing functions and types into file'
    try:
        case_classes = _generate_case_classes(labels)
        fileWriter = FileWriter()
        function_string = "".join(functions) if env != "test" else ""
        fileWriter.write_to_basic_template(output_file, case_classes, "".join(types), function_string)
    except (OSError, ValueError) as error:
        logger.FAIL(phase)
        logger.ERROR(error)
        return 1

    return 0