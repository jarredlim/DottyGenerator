import unittest
import sys

from benchmark.TestMap.run_test import run_test_map
from benchmark.apigeneration.generate import generate_api_test

if __name__ == "__main__" :

  if sys.argv[1] == 'test_map':
     run_test_map()

  elif sys.argv[1] == 'test_generation':
      generate_api_test(sys.argv[2])