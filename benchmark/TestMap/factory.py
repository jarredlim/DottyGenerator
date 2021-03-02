import unittest
from .test_class import get_test_class

from dataclasses import dataclass
import json
import os
from pathlib import Path
import typing

TEST_DIR = os.path.dirname(__file__)
_CONFIG_FILE = os.path.join(TEST_DIR, 'config.json')

def build_test_suite(tests) -> unittest.TestSuite:
    suite = unittest.TestSuite()
    for test in tests:
        for protocol in test.protocols:
            TestCase = get_test_class(filename=test.filename,
                                        protocol=protocol.identifier)

            suite.addTests(unittest.makeSuite(TestCase))

    return suite

@dataclass
class TestProtocol:
    identifier: str

    @classmethod
    def from_dict(cls, data: typing.Dict) -> 'TestProtocol':
        identifier = data['identifier']
        return cls(identifier)


@dataclass
class TestFile:
    filename: str
    protocols: typing.List[TestProtocol]

    @classmethod
    def from_dict(cls, data: typing.Dict) -> 'TestFile':
        filename = data['filename']
        protocols = [TestProtocol.from_dict(protocol)
                     for protocol in data['protocols']]
        return cls(filename, protocols)

def parse_config():
    f = Path(_CONFIG_FILE)
    test_config = json.loads(f.read_text())
    test_set= [TestFile.from_dict(test) for test in test_config['match_test']]
    return test_set