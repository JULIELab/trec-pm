# TREC-PM (Precision Medicine)

A repository containing support code and resources initially developed at the [Institute for Medical Informatics, Statistics and Documentation at the Medical University of Graz (Austria)](https://www.medunigraz.at/imi/en/) for participation at the [2017 TREC Precision Medicine Track](http://trec-cds.appspot.com/2017.html). For further information on this track and the final results please check the official [TREC-PM 2017 overview paper](https://trec.nist.gov/pubs/trec26/papers/Overview-PM.pdf). Team name: **imi_mug**

It was then further improved for participation at the [2018 TREC Precision Medicine Track](http://trec-cds.appspot.com/2018.html). Improvements include: support for subtemplates and the possibility to use disjunctive queries (_dis\_max_) allowing e.g. synonyms and hypernyms to have different weights. Team name: **hpi-dhc**.

## Citing

If you use `imi_mug`'s original data or code in your work, please cite their [TREC 2017 proceedings paper](https://trec.nist.gov/pubs/trec26/papers/imi_mug-PM.pdf):

*TREC 2017 Precision Medicine - Medical University of Graz. Pablo López-García, Michel Oleynik, Zdenko Kasáč and Stefan Schulz. Text REtrieval Conference, Gaithersburg, MD. 2017. Available at https://trec.nist.gov/pubs/trec26/papers/imi_mug-PM.pdf.*

If you use any of the improvements mentioned above, please also cite our [TREC 2018 proceedings paper](https://trec.nist.gov/pubs/trec27/papers/hpi-dhc-PM.pdf):

*HPI-DHC at TREC 2018 Precision Medicine Track. Michel Oleynik, Erik Faessler, Ariane Morassi Sasso, et. al. Text REtrieval Conference, Gaithersburg, MD. 2018. Available at https://trec.nist.gov/pubs/trec27/papers/hpi-dhc-PM.pdf.*

## Elastic Search JSON Templating Engine

For easier query formulation, this project contains a custom engine to fill JSON templates with contents from the search
topics. The idea is to fix the query structure (e.g. `{"query":{"match":{"title":"${disease}"}}}`) and to dynamically
 add the value of a specific topic (the TREC query) at the specified position. In the previous example, `${disease
 }` will access the field `disease`) of the provided topic.
 
There are currently two templating engines contained in this project. A legacy one and a newer that should replace
 the legacy approach in the future.
  
The classes realizing the legacy approach are
* `at.medunigraz.imi.bst.retrieval.MapQueryDecorator`
* `at.medunigraz.imi.bst.retrieval.TemplateQueryDecorator`
* `at.medunigraz.imi.bst.retrieval.SubTemplateQueryDecorator`

The new approach is encoded in
* `at.medunigraz.imi.bst.retrieval.JsonMapQueryDecorator`
* `at.medunigraz.imi.bst.retrieval.JsonTemplateQueryDecorator`

These "decorators" are applied to a given topic and a file containing a template. They will then replace the
_template expressions_ with the referenced values from the topic. As a template expression we denote a special
syntax that should set apart the fixed JSON contents from a topic value injection directive.
  
The syntax of the template expressions for the newer approach are explained in the following. The legacy approach
will not be documented here. In case it is needed we refer to the code (tests and existing experimental code) to
demonstrate its usage.
 
We distinguish between value injecting template expressions and template injecting expressions. The first kind
is the one that refers to actual topic values to be inserted into the template. The latter kind refers to expressions
that load a sub template, resolve its template expressions (which is a recursive action) and than replace the
original template expression with the expression-resolved subtemplate.

Note that all template expressions discussed here are case-insensitive with respect to the expression keywords, e.g.
modifiers. The field names and template paths must be written with correct case since they directly refer to Java
object fields and file paths.
 
### Value Injecting Template Expressions 
Template expressions of this type access values in the topic for actual insertion into the template that contains the
expression. All expressions are JSON strings or part of a JSON string. The quotes surrounding the template will be
removed if necessary, i.e. for non-string values of the referenced topic field. If one quote is missing, it will be
assumed that the expression part of longer string and the quoting will be left untouched.
 
The following template expressions are offered for value injection:

|  expression    |   description   |
|:--------------|:-------------:|
|   `"${topicField}"      ` |   Inserts the value of the topic object field `topicField`. If that value is an array or a collection, a modifier is required, see below.    |
|   `"${topicField[i]}"    ` |   Requires that `topicField` is a collection or an array and `i >= 0` is an explicitly given constant (e.g. `3`). Inserts the `ith` value of `topicField`. It is not a requirement that the underlying data structures offers random access. If not, the data structure will be iterated to reach the `ith` value.    |
|   `"${topicField[]}"    ` |   Requires that the template containing this expression was referenced by an iterative template-injecting expression in another template. Requires that `topicField` is a collection or an array. Inserts the `ith` value of `topicField` where `i` is an index that is dynamically passed by the iterative parent expression.    |
|   `"${topicField[][j][]}"    ` |   Requires that the template containing this expression is at the end of a two-level iterative template-injecting expression chain from two other templates (this, this template would be the third). Requires that `topicField` is a collection or an array and `j >= 0` is an explicitly given constant (e.g. `3`). Inserts the value of `topicField` at position `[i][j][k]` where `i` and `k` are indices dynamically passed by the iterative parent expressions.   |
|   `"${$ELEMENT}"        ` |   Requires that the template containing this expression was referenced by an iterative template-injecting expression in another template. Inserts the value referenced in the current iteration of the direct parent expression. Cannot have implicit index specifications (`[]`) as this is the current iteration element itself. Can, however, have explicit index specifications (e.g. `[2]`) if the value is a collection or array. |

In addition to those expressions there exists a set of template modifiers. Those modifiers are only valid within
value-injecting template expressions. They influence the exact way a referenced topic value is rendered into the
JSON template. The modifiers are directly prepended to the name of the topic field or the `$ELEMENT` reference. The
following table gives an overview over the existing modifiers.

|  modified expression    |   description   |
|:-------------:|:-------------:|
|   `"${CONCAT topicField}" ` |   If the `topicField` value is a collection or array, its contents will be concatenated into a single whitespace-delimited string. This also works with multi-dimensional data structures.    |
|   `"${JSONARRAY topicField}" ` |   The value if `topicField` will be rendered as a (possibly nested) JSON array, including the brackets. This works also with multi-dimensional data structures.    |
|   `"${FLAT JSONARRAY topicField}" ` |   The value if `topicField` will be flattened into one single array and rendered as a JSON array, including the brackets. This works also with multi-dimensional data structures.    |
|   `"${QUOTE topicField}"` |   Rarely needed. Forces the injected value to be surrounded by quotes. Should be handled automatically.    |
|   `"${NOQUOTE topicField}"` |   Rarely needed. Prohibits the injected value to be surrounded by quotes. Should be handled automatically.    |

### Template Injecting Template Expressions

The expressions discussed here have in common that they reference the name of subtemplate. A subtemplate is insofar
different from a "normal" template that is resides in a specific resources folder which is configuration in the
configuration.

The possible options are

|  expression    |   description   |
|:--------------|:-------------:|
|  `"${INSERT templatePath.json}"` | Inserts the given subtemplate after injecting topic values, if any are referenced in the subtemplate. |
| `["${FOR INDEX IN topicField REPEAT templatePath.json}"]` | Requires that `topicField` is a collection or array. For each value in `topicField`, the subtemplate at `templatePath.json` will be subject to template expression resolution with respect to the index and value of the current iteration. The value of the current iteration can be accessed in the subtemplate via `topicField[]` or `$ELEMENT`. For nested applications of this expression, the subtemplate can specify multiple indices, e.g `topicField[][]`. |
|  `"[${FOR INDEX IN topicField[] REPEAT templatePath.json}]"` | Requires that `topicField` is at least two-dimensional. Recursive FOR INDEX IN application. |

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
- python3 (to parse UMLS, get fasttext embeddings)

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
