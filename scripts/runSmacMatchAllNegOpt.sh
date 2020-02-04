#!/bin/bash
#SBATCH --cpus-per-task 1
#SBATCH --mem 5g
#SBATCH -J NegOpt

smac-v2.10.03-master-778/smac --scenario-file config/smac/matchallnegboost/scenario.txt --validation false
