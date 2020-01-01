#!/bin/bash
#SBATCH --mem 15g
#SBATCH --cpus-per-task 1
#SBATCH -J CTSMAC
#SBATCH --nodelist h1
# If a SMAC session already exists, add the path to its state directory here to continue
existingsession=""
restoreparam=""
if [[ ! -z "$existingsession" ]]; then
    restoreparam="--restore-scenario $existingsession"
fi
smac-v2.10.03-master-778/smac --validation false --scenario-file config/smac/allparams_ct/scenario.txt $restoreparam
