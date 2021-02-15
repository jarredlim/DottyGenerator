import unittest

from dottygen.tests.system.factory import build_test_suite
from dottygen.tests.system.utils import parse_config


if __name__ == "__main__":

    tests = parse_config()
    suite = build_test_suite(tests)

    runner = unittest.TextTestRunner(verbosity=2)
    runner.run(suite)