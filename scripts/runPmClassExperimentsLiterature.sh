#!/bin/bash
#SBATCH --cpus-per-task 2
#SBATCH --mem 40G
#SBATCH -J pmclassexp

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedExperimenterPmClass  -Dexec.args="$1 $2 $3"
