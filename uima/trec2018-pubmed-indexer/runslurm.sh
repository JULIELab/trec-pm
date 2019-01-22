#!/bin/bash
#SBATCH --mem 20g
#SBATCH --cpus-per-task 1
#SBATCH -J trecpubindex 

java -jar -Xmx15g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
