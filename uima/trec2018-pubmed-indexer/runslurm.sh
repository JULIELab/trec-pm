#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 1
#SBATCH -J trecpubindex 

java -jar -Xmx35g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
