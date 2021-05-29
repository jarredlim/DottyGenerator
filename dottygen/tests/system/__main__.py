import unittest
import sys

from dottygen.tests.system.factory import build_test_suite
from dottygen.tests.system.utils import parse_config


if __name__ == "__main__":

    tests = parse_config()
    suite = build_test_suite(tests, sys.argv[1:])

    runner = unittest.TextTestRunner(verbosity=2)
    runner.run(suite)