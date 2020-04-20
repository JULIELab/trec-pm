#!/bin/bash
#SBATCH --mem 50g
#SBATCH --cpus-per-task 12
#SBATCH -J topicmodel

java -jar ~/Coding/git/julielab-topic-modeling/julielab-topic-modeling/target/julielab-topic-modeling-*jar-with-dependencies.jar $*
