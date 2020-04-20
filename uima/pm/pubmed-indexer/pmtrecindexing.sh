#!/bin/bash
#SBATCH --cpus-per-task 1
#SBTACH --mem 5g
# This is the script to run to start the indexing of documents in the database
# into ElasticSearch.

srun java -Xmx2g -Dlogback.configurationFile=/home/faessler/Coding/git/trec2018/uima/trec2018-pubmed-indexer/config/logback.xml -jar ~/bin/jcore-pipeline-runner-*.jar /home/faessler/Coding/git/trec2018/uima/trec2018-pubmed-indexer/runconfig.xml 
