#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 20 
#SBATCH -J CtPrePr 

# This library causes a conflict
if [ -f lib/commons-cli-1.2.jar ]; then
	rm lib/commons-cli-1.2.jar
fi
$JAVA_HOME/bin/java -jar ~/bin/jcore-pipeline-runner-base* pipelinerunner-2threads.xml
