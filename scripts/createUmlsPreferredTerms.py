"""
Creates the UMLS term synsets used for query term expansion.
Example call:
    python createUmlsPreferredTerms.py /data/data_resources/UMLS/UMLS2019/2019AA/META/MRCONSO.RRF ENG > ../resources/umlsPreferredTerms.txt
"""

from collections import Counter
from collections import defaultdict
from sys import argv


def getPrefTerms(mrconso, language):
	cui2synsets = defaultdict(list)
	for line in open(mrconso,"r"):
		line = line.split(r"|")
		cui = line[0]
		term_language = line[1]
		term = line[14]
		suppressed = line[16]
		termstatus = line[2]
		if language == term_language and suppressed == "N" and termstatus == "P":
			cui2synsets[cui].append(term)
	for item in cui2synsets.items():
		yield item

def main():
	mrconso = argv[1]
	language = argv[2]
	for prefTermEntry in getPrefTerms(mrconso, language):
		cui = prefTermEntry[0]
		prefTerms = prefTermEntry[1]
		prefTermCounts = Counter(prefTerms)
		prefTerm = prefTermCounts.most_common(1)[0][0]
		print(cui + "\t" + prefTerm)

if __name__ == "__main__":
	main()
