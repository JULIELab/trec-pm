#!/bin/bash
#SBATCH --mem 40G
#SBATCH --cpus-per-task 4
#SBATCH -J boostopt

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedExperimenterBoostOptimizer -Dexec.args="$1 $2"
