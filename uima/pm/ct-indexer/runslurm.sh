#!/bin/bash
#SBATCH --mem 10g
#SBATCH --cpus-per-task 10 
#SBATCH -J CTIndexing 

java -jar -Xmx1g ~/bin/jcore-pipeline-runner-bas* pipelinerunner.xml

