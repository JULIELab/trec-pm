#!/bin/bash
#SBATCH --cpus-per-task 5
#SBATCH --mem 10G
#SBATCH -J termboostexp

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedTermBoostExperimenterDefaultBoosting -Dexec.args="$1 $2 $3"
