#!/bin/bash

WHAT=$1

mvn exec:java -Dexec.mainClass=at.medunigraz.imi.bst.trec.SigirPubmedExperimenterBoostOptimizer -Dexec.args="$1"