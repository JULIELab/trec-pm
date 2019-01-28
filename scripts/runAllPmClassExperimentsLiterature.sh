#!/bin/bash

mvn compile
# For the best_fields, the slop does not do anything, but a argument value is expected
sbatch --exclude=h5,h6 scripts/runPmClassExperimentsLiterature.sh best_fields OR
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh best_fields AND

# Here, the boolean operator has no effect
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh phrase OR
