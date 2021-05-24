import unittest
import sys

from case_studies.generate import generate_case_study

if __name__ == "__main__" :

  if sys.argv[1] == 'BankMicroservice':
     generate_case_study("BankMicroservice")

  elif sys.argv[1] == 'TwoBuyerNegotiate':
      generate_case_study("TwoBuyerNegotiate")

