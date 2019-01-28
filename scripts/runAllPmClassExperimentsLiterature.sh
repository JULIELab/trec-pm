#!/bin/bash

mvn compile
# For the best_fields, the slop does not do anything, but a argument value is expected
sbatch --exclude=h5,h6 scripts/runPmClassExperimentsLiterature.sh best_fields OR 2017
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh best_fields AND 2017

# Here, the boolean operator has no effect
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh phrase OR 2017


# For the best_fields, the slop does not do anything, but a argument value is expected
sbatch --exclude=h5,h6 scripts/runPmClassExperimentsLiterature.sh best_fields OR 2018
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh best_fields AND 2018

# Here, the boolean operator has no effect
sbatch --exclude=h5,h6  scripts/runPmClassExperimentsLiterature.sh phrase OR 2018