import unittest
import sys

from benchmark.TestMap.factory import build_test_suite
from benchmark.TestMap.factory import parse_config


def run_test_map() :

    tests = parse_config()
    suite = build_test_suite(tests)

    runner = unittest.TextTestRunner(verbosity=2)
    runner.run(suite)