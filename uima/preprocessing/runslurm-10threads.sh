#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 10 
#SBATCH -J trecpreproc 

# This library causes a conflict
if [ -f lib/commons-cli-1.2.jar ]; then
	rm lib/commons-cli-1.2.jar
fi
java -jar -Xmx2g ~/bin/jcore-pipeline-runner-base*.jar pipelinerunner.xml
