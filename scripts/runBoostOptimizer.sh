#!/bin/bash
#SBATCH --mem 10G
#SBATCH --cpus-per-task 4
WHAT=$1

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedExperimenterBoostOptimizer -Dexec.args="$1"
