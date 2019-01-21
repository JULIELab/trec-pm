#!/bin/bash
#SBATCH --mem 50g
#SBATCH --cpus-per-task 5 
#SBATCH -J gstojson

java -jar -Xmx32g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
