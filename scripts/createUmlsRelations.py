"""
"""

from collections import defaultdict
from sys import argv

def getRelations(mrrel):
	# PAR ~ parent
	# RB  ~ broader
	eligibleRelations = set(["PAR", "RB"])
	for line in open(mrrel,"r"):
		line    = line.split(r"|")
		cui1    = line[0]
		idType1 = line[2]
		cui2    = line[4]
		idType2 = line[6]
		rel     = line[3]
		term_language = line[1]
		suppressed = line[14]
		# Filter for CUI id references because those are the concept relations.
		if rel in eligibleRelations and suppressed == "N" and idType1 == "CUI" and idType2 == "CUI":
			yield "\t".join([cui1, rel, cui2])

def main():
	mrrel = argv[1]
	for rel in getRelations(mrrel):
		print(rel)

if __name__ == "__main__":
	main()
