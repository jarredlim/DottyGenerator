from dataclasses import dataclass
import json
import os
from pathlib import Path
import typing

TEST_DIR = os.path.dirname(__file__)
_CONFIG_FILE = os.path.join(TEST_DIR, 'config.json')


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

    test_set = {}
    f = Path(_CONFIG_FILE)
    test_config = json.loads(f.read_text())
    # test_set['type_compile'] = [TestFile.from_dict(test) for test in test_config['type_compile_tests']]
    test_set['map_test'] = [TestFile.from_dict(test) for test in test_config['match_test']]
    return test_set

