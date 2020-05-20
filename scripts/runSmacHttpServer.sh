#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 1
#SBATCH -J PMOPTSVR
#SBATCH --nodelist h3
# Parameter $1: true or false for excluding 2019 data or not
java -Xmx25g -cp .:"target/lib/*":target/classes de.julielab.ir.paramopt.HttpParamOptServer $1
