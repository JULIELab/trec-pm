"""
Creates the UMLS term synsets used for query term expansion.
Example call:
    python createUmlsTermSynsets.py /data/data_resources/UMLS/UMLS2019/UMLS2019AA/MRCONSO.RRF ENG > ../resources/umlsSynsets.txt
"""

from collections import defaultdict
from sys import argv

def getSynsets(mrconso, language):
	cui2synsets = defaultdict(list)
	for line in open(mrconso,"r"):
		line = line.split(r"|")
		cui = line[0]
		term_language = line[1]
		term = line[14]
		suppressed = line[16]
		if language == term_language and suppressed == "N":
			cui2synsets[cui].append(term)
	for synset in cui2synsets.values():
		yield synset

def main():
	mrconso = argv[1]
	language = argv[2]
	for synset in getSynsets(mrconso, language):
		print("---".join(synset))

if __name__ == "__main__":
	main()
