#!/bin/bash
#SBATCH --mem 40g
#SBATCH --cpus-per-task 1
#SBATCH -J PMOPTSVR
#SBATCH --nodelist h4
java -Xmx25g -cp .:"target/lib/*":target/classes de.julielab.ir.paramopt.HttpParamOptServer
