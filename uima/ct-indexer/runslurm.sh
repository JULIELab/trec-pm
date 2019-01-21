#!/bin/bash
#SBATCH --mem 20g
#SBATCH --cpus-per-task 10 
#SBATCH -J CTIndexing 

java -jar -Xmx15g ~/bin/jcore-pipeline-runner-bas* pipelinerunner.xml

