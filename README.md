# TREC-PM (Precision Medicine)

A repository containing support code and resources initially developed at the [Institute for Medical Informatics, Statistics and Documentation at the Medical University of Graz (Austria)](https://www.medunigraz.at/imi/en/) for participation at the [2017 TREC Precision Medicine Track](http://trec-cds.appspot.com/2017.html). For further information on this track and the final results please check the official [TREC-PM 2017 overview paper](https://trec.nist.gov/pubs/trec26/papers/Overview-PM.pdf). Team name: **imi_mug**

It was then further improved for participation at the [2018 TREC Precision Medicine Track](http://trec-cds.appspot.com/2018.html). Improvements include: support for subtemplates and the possibility to use disjunctive queries (_dis\_max_) allowing e.g. synonyms and hypernyms to have different weights. Team name: **hpi-dhc**.

Specific to this branch is the experimental code used for the SIGIR publication

Erik Faessler, Michel Oleynik, and Udo Hahn. 2020. What Makes a Top-Performing Precision Medicine Search Engine? Tracing Main System Features in a Systematic Way. _In Proceedings of the 43rd International ACM SIGIR Conference on Research and Development in Information Retrieval (SIGIR ’20), July 25–30, 2020, Virtual Event, China._ ACM, New York, NY, USA, 10 pages. https://doi.org/10.1145/3397271.3401048


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

### 2019
* [julie_mug TREC 2019 presentation slides](https://github.com/JULIELab/trec-pm/blob/master/docs/2019/presentation.pdf)
* [julie_mug TREC 2019 Poster](https://github.com/JULIELab/trec-pm/blob/sigir20/docs/2019/poster.pdf)
* [TREC 2019 proceedings](https://trec.nist.gov/pubs/trec28/trec2019.html).

## Code Dependencies

- JDK 11+ (won't compile with JDK8)
- maven
- make (for `trec_eval` tool)
- gcc (for `trec_eval` tool)
- perl (for `sample_eval` tool)
- Elasticsearch 5.4.0+
- python3 (to parse UMLS, get fasttext embeddings)

## How to Create the Resources for the Experiments

### UMLS (for disease query expansion)

You require the `MRCONSO.RRF` and `MRSTY.RRF` files which can be obtained from the official UMLS downloads.
For the SIGIR2020 paper we used the UMLS 2019A release.
Then, execute the following scripts:
*   `<repository_root>/scripts/createUmlsPreferredTerms.py`
*   `<repository_root>/scripts/createUmlsRelations.py`
*   `<repository_root>/scripts/createUmlsSemanticMapping.txt`
*   `<repository_root>/scripts/createUmlsTermSynsets.py`
store their output into the `<repository_root>/resources` directory and compress them using `gzip`.
The Framework classes making use of the UMLS synsets will expect
the files at this location.

For example, to create the `umlsSynsets.txt.gz` file, do:
- Download https://download.nlm.nih.gov/umls/kss/2019AA/umls-2019AA-mrconso.zip
- `unzip umls-2019AA-mrconso.zip`
- `python3 scripts/createUmlsTermSynsets.py MRCONSO.RRF ENG > resources/umlsSynsets.txt`
- `wc -c umlsSynsets.txt` = 338449057
- `gzip resources/umlsSynsets.txt`

### How to Create the Document Database and the ElasticSearch Index

NOTE: The indices used in the SIGIR2020 publication `What Makes a Top-Performing Precision Medicine Search Engine? Tracing Main System Features in a Systematic Way` can be obtained as ElasticSearch 5.4
 snapshots from Zenodo at https://doi.org/10.5281/zenodo.3854458.
 
The databases can be re-created using the the components in the `uima` subdirectory.
All UIMA pipelines have been created and run by the [JCoRe Pipeline Components](https://github.com/JULIELab/jcore-pipeline-modules) in version `0.4.0`. Note that all pipelines require their libraries in the `lib/` directory which does not exist at first. It is automatically created and populated by opening the pipeline with the `JCoRe Pipeline Builder CLI` under the above link. Opening the pipeline should be enough. If this does not create and populate the `lib/` directory, try opening and saving the pipeline.

1.  Install `ElasticSearch 5.4` and `Postgres >= 9.6`. Used for the experiments was `Postgres 9.6.13`.
2.  Change into the `uima` directory on the command line and execute `./gradlew install-uima-components`. This must successfully run through in order to complete the following steps. Note that Gradle is only used for scripting, the projects are all built with Maven. Thus, check the Maven output for success or failure messages. Gradle may report success despite Maven failing.
3.  Run the `pm-to-xmi-db-pipeline` and the `ct-to-xmi-db-pipeline` with the `JCoRE Pipeline Runner`. Before you actually run those, check the `pipelinerunner.xml` configuration files in both projects for the number of threads being used. Adapt them to the capabilities of your system, if necessary.
4.  Configure the `preprocessing` and `preprocessing_ct` pipelines with the `JCoRe Pipeline Builder` to activate nearly all (explained in a second) components. Some are deactivated in this release. Note that there are some components specific to `BANNER` gene tagging and `FLAIR` gene tagging. Use the `BANNER` components, Flair hasn't been used in our submitted runs. You might also leave the `LingScope` and `MutationFinder` components deactivated because those haven't been used either. Configure the `uima/costosys.xml` file to point to your Postgres database. Run the components. They will write the annotation data into the Postgres database. We used multiple machines for this, employing the [SLURM](https://slurm.schedmd.com/documentation.html) scheduler (not required). All in all we had 96 CPU cores available. Processing time was in the hours, much less than a day, for PubMed. The processing will accordingly take longer or shorter depending on the resources at your disposal.
5.  Configure the `pubmed-indexer` and `ct-indexer` projects to work with your ElasticSearch index using the `JCoRe Pipeline Builder`. Execute `mvn package` in both pipeline directories to build the indexing code, which is packaged as a `jar` and automatically put into the `lib` directory of the pipelines. Run the components.

If all steps have been performed successfully, the indices should now be present in your ElasticSearch instance. Then run the `at.medunigraz.imi.bst.trec.LiteratureArticlesExperimenter´ and `at.medunigraz.imi.bst.trec.ClinicalTrialsExperimenter` classes for the experiments carried out for TREC.

#### Additional Indexing for the SIGIR2020 Experiments

To run the SIGIR2020 experiments, each index requires 10 copies (see the SMAC section below for an explanation on this). Copy the index via reindexing in the following manner:

    for i in {1..9}; do
    curl -XPOST http://localhost:9200/_reindex -d "
    {
      \"source\": {
        \"index\": \"trecpm19_ct\"
      },
      \"dest\": {
        \"index\": \"trecpm19_ct_bm25_copy$i\"
      }
    }"
    done
 
 Configure the `trec-pm.properties` file to point to the correct index base name. The is the name of the respective index as shown above without the `_copy$i` suffix.



### Running SMAC for Parameter Optimization

NOTE: The SMAC running scripts in this repository expect the SMAC code to be uncompressed in a directory named `smac-v2.10.03-master-778` which is the directory contained in the latest release of SMAC. If another release should happen, the paths of the `scripts/runBaSplit.sh` and `scripts/runCtSplit.sh` scripts would have to be adapted.

For the [SMAC](https://www.automl.org/automated-algorithm-design/algorithm-configuration/smac/) parameter optimization, the topics of the TREC-PM challenges 2017, 2018 and 2019 were combined for biomedical abstracts (BA) and clinical trials (CT) respectively. The only exception were the CT2017 topics because for those no sample gold standard is available and thus the computation of the `infNDCG` measure is not possible.

The resulting topic sets for BA and CT were then split into 10 stratified cross validation splits, respectively. The stratification happened on the disease and gene topic fields to distribute diseases and genes as evenly as possible. The respective code is called in `EvaluateConfigurationRoute` line 44:
`List<List<Topic>> partitioning = goldStandard.createPropertyBalancedQueryPartitioning(numSplits, Arrays.asList(Topic::getDisease, Topic::getGeneField));`

To save time, all 20 SMAC optimization processes were run concurrently. To save on Java startup times and to be able to read and write into the same cache files for concurrent runs, we wrote a small web server based on Spark. This server class is `de.julielab.ir.paramopt.HttpParamOptServer`. One can send a specific hyper parameter configuration to the server, and it will respond with the `infNDCG` measure achieved with this parameter set.

Since the optimized parameters include the BM25 hyper parameters `b` and `k1`, we required one index for each run: The similarity hyper parameters can only be set on the index level in ElasticSearch. Also, the index must be closed for the change.
Consequently, we copied the BA and CT indices 10 times, respectively, and run 20 independent SMAC processes, each on another index.

The copying of the indices amounts to a total of 40 indices. This is because in the years 2017 and 2018 the same data was used in TREC-PM. For 2019, the datasets were updated. Thus, we have 2 indices for BA and 2 for CT. Both need to be copied 10 times for concurrent changes to the BM25 hyper parameters. Index copies were created using the reindexing feature of ElasticSearch as shown in the previous section.

Correct access to the different index copies within SMAC runs was achieved by a setting in the `scenario.txt` file of the SMAC runs (located in the subdirectories of `config/smac`). The algorithm call looks like this:

    algo = bash scripts/smacOverHttpWrapper.sh 32100 pm _copy0
    
 Here, we call a shell scripts with 3 parameters: The evaluation server HTTP port, the task type (pm=PubMed; we use pm synonymously to ba), and an _index suffix_. Those parameters are then sent to the HTTP evaluation server. The server queries a different copy of the respective index (ba or ct) according to the index suffix.
 
 To finally start the optimization, the HTTP evaluation server was started with the `scripts/runSmacHttpServer.sh` script. Then, the `runSmacBaOptimizationOverHttp.sh` and `runSmacCtOptimizationOverHttp.sh` scripts were called to start the SMAC runs on all topic splits concurrently as [SLURM](https://slurm.schedmd.com/documentation.html) jobs.
 
 ### Running the Ablation Study
 
 The ablation is run by calling the `scripts/runSigir20AblationExperiments.sh` script. The `de.julielab.ir.experiments.ablation.sigir20.Sigir20AblationExperiments` reads the best performing SMAC runs from the `smac-output` directory (the original SMAC output files are contained in the Zenodo package). Then, predefined sets of hyper parameters are reset to their default settings or completely deactivated as defined in the classes `de.julielab.ir.experiments.ablation.sigir20.Sigir20TopDownAblationBAParameters` and `de.julielab.ir.experiments.ablation.sigir20.Sigir20TopDownAblationCTParameters`. Resulting ablation scores and a preliminary LaTeX table for those are written to `sigir20-ablation-results`.

# Important Java System Properties in this Framework

There are few settings that are configured via Java System properties. Such settings do not count as
regular configuration settings but change basic behaviour of the system, often used for tests.

* `at.medunigraz.imi.bst.retrieval.subtemplates.folder` - sets the folder where the subtemplates are expected (default: `/subtemplates/`)
* `de.julielab.java.utilities.cache.enabled` - if set to `false`, the caching library is deactivated. The caching code is still there but the `CacheAccess` objects always return `null` when retrieving cached objects.

## Q&A
**Q: Do I really need to store all the documents into the database? Wouldn't it be quicker just to index everything directly from the source data?**

*A: Directly indexing from the source data is very well possible by combining the respective parts of the three steps (reading, preprocessing, indexing). Note however, that the LtR feature generation makes use of the document stored in the database. Thus, LtR wouldn't work this way.*


[![Codacy Badge](https://api.codacy.com/project/badge/Grade/2b63c0e0c69140318323c9eb1cd19f32)](https://www.codacy.com/app/michelole/trec-pm)
[![Build Status](https://travis-ci.com/JULIELab/trec-pm.svg?branch=master)](https://travis-ci.com/JULIELab/trec-pm)
[![Coverage Status](https://coveralls.io/repos/github/michelole/trec-pm/badge.svg?branch=master)](https://coveralls.io/github/michelole/trec-pm?branch=master)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
