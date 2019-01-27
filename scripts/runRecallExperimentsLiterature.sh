#!/bin/bash
#SBATCH --cpus-per-task 5
#SBATCH --mem 10G
#SBATCH -J recallexp

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedRecallExperimenterDefaultBoosting -Dexec.args="$1 $2 $3 $4"
