#!/bin/bash
smac-v2.10.03-master-778/smac --validation false --scenario-file config/smac/allparams_ct_split$1/scenario.txt --restore-scenario smac-output/allparams_ct_split$1/state-run3 --rungroup allparams_ct_split$1
