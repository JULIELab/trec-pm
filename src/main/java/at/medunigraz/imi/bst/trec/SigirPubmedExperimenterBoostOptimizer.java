package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

public class SigirPubmedExperimenterBoostOptimizer extends SuperSigirPubmedExperimenter {
    public static void main(String[] args) {

        Set<String> validParams = new LinkedHashSet<>();
        validParams.add("disease");
        validParams.add("gene");
        validParams.add("fields");
        validParams.add("posneg");
        validParams.add("additional");
        validParams.add("extra");
        validParams.add("pmgs");
        validParams.add("pmclass");

        if (args.length != 1 || !validParams.contains(args[0])) {
            System.err.println("Usage: " + SigirPubmedExperimenterBoostOptimizer.class.getSimpleName() + " <what>");
            System.err.println("Where <what> is one of " + validParams);
            System.exit(1);
        }

        String what = args[0];

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        Map<String, String> templateProperties = SigirParameters.TREC_2018_HPIPUBNONE;


        DecimalFormat df = new DecimalFormat("0.0");
        if (what.equals("disease")) {
            for (double disb = .4; disb < 2.4; disb += .4) {
                for (double ptb = .2; ptb < 1.2; ptb += .4) {
                    for (double synb = .2; synb < 2; synb += .4) {
                        templateProperties.put("disease_boost", String.valueOf(disb));
                        templateProperties.put("disease_prefterm_boost", String.valueOf(ptb));
                        templateProperties.put("disease_syn_boost", String.valueOf(synb));
                        String suffix = "--dis" + df.format(disb) + "-pt" + df.format(ptb) + "-syn" + df.format(synb);
                        runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                    }
                }
            }
        } else if (what.equals("gene")) {
            for (double genb = .4; genb < 2.4; genb += .4) {
                for (double descb = .2; descb < 1.8; descb += .4) {
                    for (double synb = .2; synb < 1.8; synb += .4) {
                        templateProperties.put("gene_boost", String.valueOf(genb));
                        templateProperties.put("gene_desc_boost", String.valueOf(descb));
                        templateProperties.put("gene_syn_boost", String.valueOf(synb));
                        String suffix = "--gen" + df.format(genb) + "-desc" + df.format(descb) + "-syn" + df.format(synb);
                        runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                    }
                }
            }
        } else if (what.equals("fields")) {
            for (double titb = 1; titb < 1.6; titb += .2) {
                for (double abstrb = 1; abstrb < 1.6; abstrb += .2) {
                    for (double kwb = 1; kwb < 1.6; kwb += .2) {
                        for (double meshb = 1; meshb < 1.6; meshb += .2) {
                            for (double genesb = 1; genesb < 1.1; genesb += .4) {
                                templateProperties.put("title_boost", "^" + titb);
                                templateProperties.put("abstract_boost", "^" + abstrb);
                                templateProperties.put("keyword_boost", "^" + kwb);
                                templateProperties.put("meshTags_boost", "^" + meshb);
                                templateProperties.put("genes_field_boost", "^" + genesb);
                                String suffix = "--tit" + df.format(titb) + "-ab" + df.format(abstrb) + "-kw" + df.format(kwb) + "-msh" + df.format(meshb) + "-gen" + df.format(genesb);
                                runExperiments(templateProperties, goldStandard, target, year, what, suffix);

                            }
                        }
                    }
                }
            }
        } else if (what.equals("posneg")) {
            for (double posb = .7; posb < 1.2; posb += .1) {
                for (double negb = -3; negb < .2; negb += .4) {
                    templateProperties.put("pos_words_boost", String.valueOf(posb));
                    templateProperties.put("neg_words_boost", String.valueOf(negb));
                    String suffix = "--pos" + df.format(posb) + "-neg" + df.format(negb);
                    runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                }
            }
        } else if (what.equals("additional")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double cancerb = .4; cancerb < 2; cancerb += .4) {
                for (double chemob = .4; chemob < 2; chemob += .4) {
                    for (double dnab = .4; dnab < 2; dnab += .4) {
                        for (double nonmelb = -1; nonmelb < .8; nonmelb += .4) {
                            Map<String, String> paramcombination = templateProperties;
                            paramcombination.put("cancer_boost", String.valueOf(cancerb));
                            paramcombination.put("chemo_boost", String.valueOf(chemob));
                            paramcombination.put("dna_boost", String.valueOf(dnab));
                            paramcombination.put("non_mel_boost", String.valueOf(dnab));
                            String suffix = "--canc" + df.format(cancerb) + "-chem" + df.format(chemob) + "-dna" + df.format(dnab) + "-nonmel" + df.format(nonmelb);
                            parameters.add(paramcombination);
                            suffixes.add(suffix);
                        }
                    }
                }
            }
            IntStream.range(0, parameters.size()).parallel().forEach(i -> {
                Map<String, String> parameterset = parameters.get(i);
                String suffix = suffixes.get(i);
                Map<String, String> runparameters = templateProperties;
                runparameters.putAll(parameterset);
                runExperiments(parameterset, goldStandard, target, year, what, suffix);
            });

        } else if (what.equals("extra")) {
            for (double extrab = .4; extrab < 2; extrab += .4) {
                templateProperties.put("extra_boost", String.valueOf(extrab));
                String suffix = "--extra" + df.format(extrab);
                runExperiments(templateProperties, goldStandard, target, year, what, suffix);
            }
        } else if (what.equals("pmgs")) {
            for (double pmgsb = -1; pmgsb < .8; pmgsb += .4) {
                templateProperties.put("pm_gs_boost", String.valueOf(pmgsb));
                String suffix = "--pmgs" + df.format(pmgsb);
                runExperiments(templateProperties, goldStandard, target, year, what, suffix);
            }
        }
        if (what.equals("pmclass")) {
            final List<String> pmfields = Arrays.asList("pmclass2017lstm.keyword",
                    "pmclass2017lstmatt.keyword",
                    "pmclass2017lstmgru.keyword",
                    "pmclass2018lstm.keyword",
                    "pmclass2018lstmat.keyword",
                    "pmclass2018lstmgru.keyword", "pmclass2017.keyword", "pmclass2018.keyword");
            //final List<String> pmfields = Arrays.asList( "pmclass2017.keyword");
            pmfields.parallelStream().forEach(pmfield -> {
                Map<String, String> parameters = templateProperties;
                parameters.put("pm_class_field", pmfield);
                runExperiments(parameters, goldStandard, target, year, what, "-" + pmfield);
            });
        } else throw new IllegalStateException("Unknown mode " + what);


    }
}
