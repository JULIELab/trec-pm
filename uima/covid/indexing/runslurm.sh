#!/bin/bash
#SBATCH --mem 10G
#SBATCH --cpus-per-task 24
#SBATCH -J CD19_IDX
java -Xmx1G -jar ~/bin/jcore-pipeline-runner-base* run.xml

