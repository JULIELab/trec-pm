#!/bin/bash
#SBATCH --mem 7g
#SBATCH --cpus-per-task 1 
#SBATCH -J trecprocessing

java -jar -Xmx6g -Dlogback.configurationFile=/home/faessler/Coding/git/trec2018/uima/gs-to-json-pipeline/config/logback.xml ~/bin/jcore-pipeline-runner-*.jar pipelinerunner.xml
