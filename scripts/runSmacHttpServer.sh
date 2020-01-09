#!/bin/bash
#SBATCH --mem 20g
#SBATCH --cpus-per-task 1
#SBATCH -J PMOPTSVR
#SBATCH --nodelist h4
java -cp .:"target/lib/*":target/classes de.julielab.ir.paramopt.HttpParamOptServer 32100
