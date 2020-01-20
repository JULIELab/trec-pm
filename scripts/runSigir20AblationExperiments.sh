#!/bin/bash
#SBATCH --mem 10g
#SBATCH --cpus-per-task 1
#SBATCH -J ABLATION
#SBATCH --nodelist h4
java -cp .:"target/lib/*":target/classes de.julielab.ir.experiments.ablation.sigir20.Sigir20AblationExperiments $*
