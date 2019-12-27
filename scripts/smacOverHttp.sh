#!/bin/bash
port=$1
instance=$2
instanceInfo=$3
cutoffTime=$4
cutoffLength=$5
seed=$6

argc=$#
argv=("$@")

curldata="instance=$instance&instance_info=none&cutoff_time=$cutoffTime&cutoff_length=$cutoffLength&seed=$seed"
for (( j=6; j<argc; j++ )); do
    arg=${argv[j]}
    if [[ $((j%2)) = 0 ]]; then
      argWoDash="${arg:1:${#arg}}"
      value=${argv[j+1]}
      curldata=$curldata"&"$argWoDash=$value
    fi
done
score=`curl -s -XPOST http://localhost:$port/get_configuration_score -d "$curldata"`
echo "Result for SMAC: SUCCESS, 0, 0, $score, 0"