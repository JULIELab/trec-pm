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

- JDK 11+ (won't compile with JDK8)
- maven
- make (for `trec_eval` tool)
- gcc (for `trec_eval` tool)
- perl (for `sample_eval` tool)
- Elasticsearch 5.4.0+
- python3 (to parse UMLS)

## How to Create the Resources for the Experiments

### UMLS

You require the `MRCONSO.RRF` which can be obtained from the official UMLS downloads.
Then, adapt the paths in the `scripts/createUmlsTermSynsets.py` script to read from your `MRCONSO.RRF` file and
create the `resources/umlsSynsets.txt` file. Framework classes making use of the UMLS synsets will expect
the file at this location.

- Download https://download.nlm.nih.gov/umls/kss/2019AA/umls-2019AA-mrconso.zip
- `unzip umls-2019AA-mrconso.zip`
- `python3 scripts/createUmlsTermSynsets.py MRCONSO.RRF ENG > resources/umlsSynsets.txt`
- `wc -c umlsSynsets.txt` = 338449057
- `gzip resources/umlsSynsets.txt`

### FastText Embeddings for LtR

`FastText` embeddings are used to create document embeddings for LtR features. Note that their performance impact seemed to be minor in experiments on the TREC-PM 17/18 data and probably can be left out without great performance penalties. However, this can't be said for sure before evaluation on the 2019 gold standard.
The emebeddings can be recreated by:
1. Run the BANNER gene tagger from [jcore-projects](https://github.com/JULIELab/jcore-projects/tree/master/jcore-jnet-ae-biomedical-english), version>=2.4 on the Medline/PubMed 2019 baseline.
2. Extract the document text from those document with at least one tagged gene in them. This should be around 8 million documents. The text is the title plus abstract text (e.g. by using the [JCoRe PubMed reader](https://github.com/JULIELab/jcore-projects/tree/master/jcore-pubmed-reader) and the [JCoRe To TXT consumer](https://github.com/JULIELab/jcore-base/tree/master/jcore-txt-consumer) in the `DOCUMENT` mode). No postprocessing (which should be done for better models but hasn't been done on the used embeddings).
3. Create `FastText` word embeddings with a dimension of 300. We used the `.bin` output for LtR features.

## Some Examples on How to Run Experiments

```
# All executions should be run where the pom file is, usually the root of the project

# How to run the pubmed experimenter
# Necessary to define the year and type of gold-standard (for evaluation)

mvn clean install
mvn exec:java -Dexec.mainClass="at.medunigraz.imi.bst.trec.LiteratureArticlesExperimenter"

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
# How to Create the Document Database and the ElasticSearch Index

The databases can be re-created using the the components in the `uima` subdirectory.
All UIMA pipelines have been created and run by the [JCoRe Pipeline Components](https://github.com/JULIELab/jcore-pipeline-modules) in version `0.4.0`. Note that all pipelines require their libraries in the `lib/` directory which does not exist at first. It is automatically created and populated by opening the pipeline with the `JCoRe Pipeline Builder CLI` under the above link. Opening the pipeline should be enough. If this das not create and populate the `lib/` directory, try opening and saving the pipeline.

1.  Install `ElasticSearch 5.4` and `Postgres >= 9.6`. Used for the experiments was `Postgres 9.6.13`.
2.  Change into the `uima` directory on the command line and execute `./gradlew install-uima-components`. this must successfully run through in order to complete the following steps. Note that Gradle is only used for scripting, the projects are all build with Maven. Thus, check the Maven output for success or failure messages. Gradle may report success despite Maven failing.
3.  Run the `pm-to-xmi-db-pipeline` and the `ct-to-xmi-db-pipeline` with the `JCoRE Pipeline Runner`. Before you actually run those, check the `pipelinerunner.xml` configuration files in both projects for the number threads being used. Adapt them to the capabilities of your system, if necessary.
4.  Configure the `preprocessing` and `preprocessing_ct` with the `JCoRe Pipeline Builder` to active nearly all (explained in a second) components. Some are deactivated in this release. Note that there are some components specific to `BANNER` gene tagging and `FLAIR` gene tagging. Use the `BANNER` components, Flair hasn't been used in our submitted runs. You might also leave the `LingScope` and `MutationFinder` components off because those haven't been used either. Configure the `uima/costosys.xml` file in all pipelines to point to your Postgres database. Run the components. They will write the annotation data into the Postgres database. We used multiple machines for this, employing the SLURM scheduler (not required). All in all we had 96 CPU cores available. Processing time was in the hours, much less than a day for PubMed. The processing will accordingly take longer or shorter depending on the resources at your disposal.
5.  Configure the `pubmed-indexer` and `ct-indexer` projects to work with your ElasticSearch index using the `JCoRe Pipeline Builder`. Execute `mvn package` in both pipeline directories to build the indexing code, which is packaged as a `jar` and automatically put into the `lib` directory of the pipelines. Run the components.

If all steps have been performed successfully, the indices should now be present in your ElasticSearch instance. To run the experiments, also configure the `<repository root>/config/costosys.xml`  file to point to your database. Then run the `at.medunigraz.imi.bst.trec.LiteratureArticlesExperimenter´ and `at.medunigraz.imi.bst.trec.ClinicalTrialsExperimenter` classes.

## Q&A
**Q: Do I really need to store all the documents into the database? Wouldn't it be quicker just to index everything directly from the source data?**

*A: Directly indexing from the source data is very well possible by combining the respective parts of the three steps (reading, preprocessing, indexing). Note however, that the LtR feature generation makes use of the document stored in the database. Thus, LtR wouldn't work this way.*

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2b63c0e0c69140318323c9eb1cd19f32)](https://www.codacy.com/app/michelole/trec-pm)
[![Build Status](https://travis-ci.com/JULIELab/trec-pm.svg?branch=master)](https://travis-ci.com/JULIELab/trec-pm)
[![Coverage Status](https://coveralls.io/repos/github/michelole/trec-pm/badge.svg?branch=master)](https://coveralls.io/github/michelole/trec-pm?branch=master)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
