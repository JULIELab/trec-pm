#!/bin/bash
#SBATCH --mem 20g
#SBATCH --cpus-per-task 4
#SBATCH -J CACHE
#SBATCH --nodelist h1
# The parameter specifies the size of the thread pool. For each request one processing thread is
# required. The threads are held in a thread pool. No more than the given number of requests can be
# processed in parallel.
java -cp "target/lib/*":target/classes de.julielab.ir.TrecCacheServer 100
