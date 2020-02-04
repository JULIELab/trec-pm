#!/bin/bash
#SBATCH -n 10 
#SBATCH --nodelist h4
for i in {0..9}; do
	echo "Starting parameter optimization for biomedical abstracts, split $i"
	srun -o slurm-ba-split$i.log -n 1 scripts/runBaSplit.sh $i &
done
echo "Waiting for the jobs to finish."
wait
