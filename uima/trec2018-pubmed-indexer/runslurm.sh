#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 20 
#SBATCH -J trecpubindex 

java -jar -Xmx1g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
