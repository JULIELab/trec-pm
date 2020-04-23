#!/bin/bash
# To be executed by gradle in the uima/ subdirectory of the repository!
components="jcore-ct-reader jcore-pmclassifier-ae jcore-trecpm-extraabstracts-reader gs-to-json-pipeline"
for i in $components; do
    echo "Calling python setup-scripts/createMetaDescriptors.py -c -i -r manual -v 1.0 -u false $i;"
	python setup-scripts/createMetaDescriptors.py -c -i -r manual -v 1.0 -u false $i;
done
# This step actually installs the components. But only those that are mentioned in the <modules> section
# of the POM! So to add a component for installation, add it there as a module.
mvn clean install -DskipTests=true
echo "Installing the parent pom in this directory"
