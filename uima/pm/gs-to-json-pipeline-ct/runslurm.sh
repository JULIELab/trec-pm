#!/bin/bash
#SBATCH --mem 7g
#SBATCH --cpus-per-task 1 
#SBATCH -J gstojsonct

java -jar -Xmx6g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
