#!/bin/bash
#SBATCH --mem 20g
#SBATCH --cpus-per-task 2 
#SBATCH -J PmXmiImport 

java -jar -Xmx6g ~/bin/jcore-pipeline-runner-bas* pipelinerunner.xml

