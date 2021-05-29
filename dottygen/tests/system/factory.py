import os
import subprocess
import typing
import unittest

from dottygen.cli import main as run_codegen
from dottygen.tests.system.utils import TEST_DIR
from dottygen.utils import logger

from argparse import ArgumentParser

def _build_test_case(*,
                     filename: str,
                     protocol: str,
                     unop) -> typing.Type[unittest.TestCase]:

    output_file = f"test.scala"
    parent_output = os.path.abspath(os.path.join('effpi_sandbox',output_file))

    class CodeGenerationTest(unittest.TestCase):

        def setUp(self):

            if os.path.exists(parent_output):
                os.remove(parent_output)

        def test_code_generation(self):
            flags = [os.path.join(TEST_DIR, 'examples', filename), protocol ]
            if unop:
                flags += ["--unop"]

            phase = 'Run codegen'
            exit_code = run_codegen(flags)
            if exit_code != 0:
                logger.FAIL(phase)
            else:
                logger.SUCCESS(phase)

            self.assertEqual(exit_code, 0)

            phase = 'Check generated Effpi code'
            completion = subprocess.run(f"sbt 'tests/runMain effpi_sandbox.{protocol}.Main'", shell=True, stdout=subprocess.PIPE,
                                        stderr=subprocess.PIPE)

            exit_code = completion.returncode
            if exit_code != 0:
                logger.FAIL(phase)

                if completion.stdout:
                    logger.ERROR('stdout', completion.stdout)

                if completion.stderr:
                    logger.ERROR('stderr', completion.stderr)
            else:
                logger.SUCCESS(phase)
                # os.remove(parent_type_output)
                # os.remove(parent_function_output)

            self.assertEqual(exit_code, 0)
            print()

    test_name = f'{filename}: {protocol}>'
    CodeGenerationTest.__name__ = test_name
    CodeGenerationTest.__qualname__ = test_name
    return CodeGenerationTest


def parse_arguments(args: typing.List[str]) -> typing.Dict:
    """Prepare command line argument parser and return the parsed arguments
    from the specified 'args'."""

    parser = ArgumentParser()

    parser.add_argument('--unop', help='unoptimised merging', action='store_true')

    parsed_args = parser.parse_args(args)
    return vars(parsed_args)

def build_test_suite(tests, args: typing.List[str]) -> unittest.TestSuite:

    parsed_args = parse_arguments(args)
    unop = parsed_args['unop']

    suite = unittest.TestSuite()
    for test in tests:
        for protocol in test.protocols:
            TestCase = _build_test_case(filename=test.filename,
                                        protocol=protocol.identifier, unop=unop)

            suite.addTests(unittest.makeSuite(TestCase))

    return suite