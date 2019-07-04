# TREC-PM (Precision Medicine)

A repository containing support code and resources initially developed at the [Institute for Medical Informatics, Statistics and Documentation at the Medical University of Graz (Austria)](https://www.medunigraz.at/imi/en/) for participation at the [2017 TREC Precision Medicine Track](http://trec-cds.appspot.com/2017.html). For further information on this track and the final results please check the official [TREC-PM 2017 overview paper](https://trec.nist.gov/pubs/trec26/papers/Overview-PM.pdf). Team name: **imi_mug**

It was then further improved for participation at the [2018 TREC Precision Medicine Track](http://trec-cds.appspot.com/2018.html). Improvements include: support for subtemplates and the possibility to use disjunctive queries (_dis\_max_) allowing e.g. synonyms and hypernyms to have different weights. Team name: **hpi-dhc**.

## Citing

If you use `imi_mug`'s original data or code in your work, please cite their [TREC 2017 proceedings paper](https://trec.nist.gov/pubs/trec26/papers/imi_mug-PM.pdf):

*TREC 2017 Precision Medicine - Medical University of Graz. Pablo López-García, Michel Oleynik, Zdenko Kasáč and Stefan Schulz. Text REtrieval Conference, Gaithersburg, MD. 2017. Available at https://trec.nist.gov/pubs/trec26/papers/imi_mug-PM.pdf.*

If you use any of the improvements mentioned above, please also cite our [TREC 2018 proceedings paper](https://trec.nist.gov/pubs/trec27/papers/hpi-dhc-PM.pdf):

*HPI-DHC at TREC 2018 Precision Medicine Track. Michel Oleynik, Erik Faessler, Ariane Morassi Sasso, et. al. Text REtrieval Conference, Gaithersburg, MD. 2018. Available at https://trec.nist.gov/pubs/trec27/papers/hpi-dhc-PM.pdf.*

## Other resources

### 2017
* [imi_mug TREC 2017 presentation slides](https://github.com/bst-mug/trec2017/blob/master/docs/presentation.pdf)
* [imi_mug TREC 2017 Poster](https://github.com/bst-mug/trec2017/blob/master/docs/poster.pdf)
* [TREC 2017 proceedings](https://trec.nist.gov/pubs/trec26/trec2017.html).


### 2018
* [hpi_dhc TREC 2018 presentation slides](https://github.com/hpi-dhc/trec-pm/blob/master/docs/2018/presentation.pdf)
* [hpi_dhc TREC 2018 Poster](https://github.com/hpi-dhc/trec-pm/blob/master/docs/2018/poster.pdf)
* [hpi_dhc TREC 2018 Data Artifacts](https://figshare.com/projects/TREC_PM_2018_Data_hpi-dhc_/56882)
* [TREC 2018 proceedings](https://trec.nist.gov/pubs/trec27/trec2018.html).

## Code Dependencies

- JDK 11+
- maven
- make (for `trec_eval` tool)
- gcc (for `trec_eval` tool)
- perl (for `sample_eval` tool)
- Elasticsearch 5.4.0+

## How to Create the Resources for the Experiments

### UMLS

You require the `MRCONSO.RRF` which can be obtained from the official UMLS downloads.
Then, adapt the paths in the `scripts/createUmlsTermSynsets.py` script to read from your `MRCONSO.RRF` file and
create the `resources/umlsSynsets.txt` file. Framework classes making use of the UMLS synsets will expect
the file at this location. 

## Some Examples on How to Run Experiments

```
# All executions should be run where the pom file is, usually the root of the project

# How to run the pubmed experimenter
# Necessary to define the year and type of gold-standard (for evaluation)

mvn clean install
mvn exec:java -Dexec.mainClass="at.medunigraz.imi.bst.trec.PubmedExperimenter"

# How to run the clinical trials experimenter
# Necessary to define the year and type of gold-standard (for evaluation)

mvn clean install
mvn exec:java -Dexec.mainClass="at.medunigraz.imi.bst.trec.ClinicalTrialsExperimenter"

# How to run the KeywordExperimenter
# Necessary to define the year and type of gold-standard (for evaluation)
# For positive booster, in the keyword template leave boost = 1
# For negative booster, in the keyword template leave boost = -1
# Also, in the KeywordExperimenter the keywordsSource needs to be specified

mvn clean install
mvn exec:java -Dexec.mainClass="at.medunigraz.imi.bst.trec.KeywordExperimenter" > out.txt &
cat out.txt | grep -e "\(^[0-9\.]*\)\(\;.*\)\(with.*\)\(\\[.*\\]\)\(.*\)" | sed -r "s/"\(^[0-9\.]*\)\(\;.*\)\(with.*\)\(\\[.*\\]\)\(.*\)"/\1 \2 \4/" > results.txt
```

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2b63c0e0c69140318323c9eb1cd19f32)](https://www.codacy.com/app/michelole/trec-pm)
[![Build Status](https://travis-ci.com/JULIELab/trec-pm.svg?branch=master)](https://travis-ci.com/JULIELab/trec-pm)
[![Coverage Status](https://coveralls.io/repos/github/michelole/trec-pm/badge.svg?branch=master)](https://coveralls.io/github/michelole/trec-pm?branch=master)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
