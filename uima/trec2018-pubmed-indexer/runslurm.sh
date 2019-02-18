#!/bin/bash
#SBATCH --mem 60g
#SBATCH --cpus-per-task 10 
#SBATCH -J trecpubindex 

java -jar -Xmx1g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
