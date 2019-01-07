#!/bin/bash

whats="disease gene fields posneg additional extra pmgs"

mvn clean compile

#srun scripts/runBoostOptimizer.sh extra

for i in $whats; do
    echo "Starting boost optimization of $i"
    sbatch scripts/runBoostOptimizer.sh $i
done