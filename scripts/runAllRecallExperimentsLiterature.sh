#!/bin/bash

mvn compile
# For the best_fields, the slop does not do anything, but a argument value is expected
sbatch scripts/runRecallExperimentsLiterature.sh best_fields OR false 10
sbatch scripts/runRecallExperimentsLiterature.sh best_fields OR true 10
sbatch scripts/runRecallExperimentsLiterature.sh best_fields AND false 10
sbatch scripts/runRecallExperimentsLiterature.sh best_fields AND true 10

# Here, the boolean operator has no effect
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR false 10
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR true 10
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR false 5
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR true 5
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR false 3
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR true 3
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR false 2
sbatch scripts/runRecallExperimentsLiterature.sh phrase OR true 2