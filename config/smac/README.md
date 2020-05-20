# Parameter Optimization with SMAC

This directory contains the SMAC configurations that were used to find optimal hyperparameters for each ten fold cross validation-split for the biomedical abstracts (ba) and clinical trials (ct) topic sets.

Both sets are comprised of the 2017, 2018 and 2019 topics of the respective tasks with the exception of CT 2017. This is because in 2017, no sampled judgements were created for the CT task which prevents the computation of the `infNDCG` measure for those topics.

The configuration are virtually the same with the exception of the `instances.txt` files. There, the different cross validation splits are references.