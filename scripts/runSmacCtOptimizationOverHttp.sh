#!/bin/bash
#SBATCH -n 10 
#SBATCH --nodelist h4
for i in {0..9}; do
	echo "Starting parameter optimization for clinical trials, split $i"
	srun -o slurm-ct-split$i.log -n 1 scripts/runCtSplit.sh $i &
done
echo "Waiting for the jobs to finish."
wait
