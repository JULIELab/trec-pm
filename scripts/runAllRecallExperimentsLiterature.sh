#!/bin/bash

mvn compile
# For the best_fields, the slop does not do anything, but a argument value is expected
sbatch --exclude=h5,h6 scripts/runRecallExperimentsLiterature.sh best_fields OR 10
sbatch --exclude=h5,h6  scripts/runRecallExperimentsLiterature.sh best_fields AND 10

# Here, the boolean operator has no effect
sbatch --exclude=h5,h6  scripts/runRecallExperimentsLiterature.sh phrase OR 10
#sbatch --exclude=h5,h6  scripts/runRecallExperimentsLiterature.sh phrase OR 5
#sbatch --exclude=h5,h6  scripts/runRecallExperimentsLiterature.sh phrase OR 3
#sbatch --exclude=h5,h6  scripts/runRecallExperimentsLiterature.sh phrase OR 2