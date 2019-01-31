#!/bin/bash
#whats="genedis fields posneg additional extra pmclass mutation drug"
whats="pmclass"

mvn compile

for i in $whats; do
    echo "Starting boost optimization of $i"
    sbatch scripts/runBoostOptimizer.sh $i 2017
    sbatch scripts/runBoostOptimizer.sh $i 2018
done
