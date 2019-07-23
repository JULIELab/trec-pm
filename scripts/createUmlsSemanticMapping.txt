#!/bin/bash

if [ -z "$1" ]; then
	echo "Please provide the path to the MRSTY.RRF file which is part of the full UMLS Metathesaurus download."
	exit 1
fi

OUTPUT=resources/umlsSemanticTypes.txt.gz
if [[ "`pwd`" == *scripts ]]; then
	OUTPUT="../$OUTPUT"
fi
echo "Writing UMLS concept semantic type mapping to $OUTPUT"
cut -d"|" -f1,2,4 $1 | tr '|' '\t' | gzip > $OUTPUT
