#!/bin/bash
port=$1
# PM or CT (PubMed for biomedical articles task, ClinicalTrials for clinical trials task), used in the server endpoint
corpustype=$2
instance=$3
instanceInfo=$4
cutoffTime=$5
cutoffLength=$6
seed=$7

argc=$#
argv=("$@")

curldata="instance=$instance&instance_info=none&cutoff_time=$cutoffTime&cutoff_length=$cutoffLength&seed=$seed"
for (( j=7; j<argc; j++ )); do
    arg=${argv[j]}
    if [[ $((j%2)) = 1 ]]; then
      argWoDash="${arg:1:${#arg}}"
      value=${argv[j+1]}
      curldata=$curldata"&"$argWoDash=$value
    fi
done
score=`curl -s -XPOST http://localhost:$port/get_configuration_score_$corpustype -d "$curldata"`
echo "Result for SMAC: SUCCESS, 0, 0, $score, 0"