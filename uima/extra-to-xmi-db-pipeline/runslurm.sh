#!/bin/bash
#SBATCH --mem 10g
#SBATCH --cpus-per-task 2 
#SBATCH -J ExtraXmiImport 

java -jar -Xmx6g ~/bin/jcore-pipeline-runner-bas* runnerconfig.xml

