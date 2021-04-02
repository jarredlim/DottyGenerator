import unittest
import sys

from benchmark.TestMap.run_test import run_test_map
from benchmark.apigeneration.generate import generate_api_test
from benchmark.codeline.generate import generate_code_line
from benchmark.timecomparison.generate2 import plot_bar_chart

if __name__ == "__main__" :

  if sys.argv[1] == 'test_map':
     run_test_map()

  elif sys.argv[1] == 'test_generation':
      generate_api_test(sys.argv[2])

  elif sys.argv[1] == 'test_code_line':
      generate_code_line(sys.argv[2])

  elif sys.argv[1] == 'test_generation_time':
      plot_bar_chart()
