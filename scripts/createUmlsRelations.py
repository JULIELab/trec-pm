"""
"""

from collections import defaultdict
from sys import argv

def getRelations(mrrel):
	# PAR ~ parent
	# RB  ~ broader
	eligibleRelations = set(["PAR", "RB"])
	for line in open(mrrel,"r"):
		line = line.split(r"|") 
		cui1 = line[0]
		cui2 = line[4]
		rel  = line[3]
		term_language = line[1]
		suppressed = line[14]
		if rel in eligibleRelations and suppressed == "N":
			yield "\t".join([cui1, rel, cui2])

def main():
	mrrel = argv[1]
	for rel in getRelations(mrrel):
		print(rel)

if __name__ == "__main__":
	main()
