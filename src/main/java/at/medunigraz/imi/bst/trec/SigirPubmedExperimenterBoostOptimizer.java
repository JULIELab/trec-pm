package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.text.DecimalFormat;
import java.util.*;

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

        if (args.length != 1 || !validParams.contains(args[0])) {
            System.err.println("Usage: " + SigirPubmedExperimenterBoostOptimizer.class.getSimpleName() + " <what>");
            System.err.println("Where <what> is one of " + validParams);
            System.exit(1);
        }

        String what = args[0];

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        Map<String, String> templateProperties = new HashMap<>();
        templateProperties.put("disease_boost", "1");
        templateProperties.put("disease_prefterm_boost", "1");
        templateProperties.put("disease_syn_boost", "1");
        templateProperties.put("gene_boost", "1");
        templateProperties.put("gene_syn_boost", "1");
        templateProperties.put("gene_desc_boost", "1");
        templateProperties.put("title_boost", "");
        templateProperties.put("abstract_boost", "");
        templateProperties.put("keyword_boost", "");
        templateProperties.put("meshTags_boost", "");
        templateProperties.put("genes_field_boost", "");
        templateProperties.put("pos_words_boost", "1");
        templateProperties.put("neg_words_boost", "1");
        templateProperties.put("cancer_boost", "1");
        templateProperties.put("chemo_boost", "1");
        templateProperties.put("dna_boost", "1");
        templateProperties.put("extra_boost", "1");
        templateProperties.put("pm_gs_boost", "1");
        templateProperties.put("non_mel_boost", "1");

        DecimalFormat df = new DecimalFormat("0.0");
        if (what.equals("disease")) {
            for (double disb = .4; disb < 2; disb += .4) {
                for (double ptb = .4; ptb < 2; ptb += .4) {
                    for (double synb = .4; synb < 2; synb += .4) {
                        templateProperties.put("disease_boost", String.valueOf(disb));
                        templateProperties.put("disease_prefterm_boost", String.valueOf(ptb));
                        templateProperties.put("disease_syn_boost", String.valueOf(synb));
                        String suffix = "XXdis" + df.format(disb) + "pt" + df.format(ptb) + "syn" + df.format(synb);
                        runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                    }
                }
            }
        } else if (what.equals("gene")) {
            for (double genb = .4; genb < 2; genb += .4) {
                for (double descb = .4; descb < 2; descb += .4) {
                    for (double synb = .4; synb < 2; synb += .4) {
                        templateProperties.put("gene_boost", String.valueOf(genb));
                        templateProperties.put("gene_desc_boost", String.valueOf(descb));
                        templateProperties.put("gene_syn_boost", String.valueOf(synb));
                        String suffix = "XXgen" + df.format(genb) + "desc" + df.format(descb) + "syn" + df.format(synb);
                        runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                    }
                }
            }
        } else if (what.equals("fields")) {
            for (double titb = .4; titb < 2; titb += .4) {
                for (double abstrb = .4; abstrb < 2; abstrb += .4) {
                    for (double kwb = .4; kwb < 2; kwb += .4) {
                        for (double meshb = .4; meshb < 2; meshb += .4) {
                            for (double genesb = .4; genesb < 2; genesb += .4) {
                                templateProperties.put("title_boost", "^" + titb);
                                templateProperties.put("abstract_boost", "^" + abstrb);
                                templateProperties.put("keyword_boost", "^" + kwb);
                                templateProperties.put("meshTags_boost", "^" + meshb);
                                templateProperties.put("genes_field_boost", "^" + genesb);
                                String suffix = "XXtit" + df.format(titb) + "ab" + df.format(abstrb) + "kw" + df.format(kwb) + "msh" + df.format(meshb) + "gen" + df.format(genesb);
                                runExperiments(templateProperties, goldStandard, target, year, what, suffix);

                            }
                        }
                    }
                }
            }
        } else if (what.equals("posneg")) {
            for (double posb = .4; posb < 2; posb += .4) {
                for (double negb = -1; negb < .8; negb += .4) {
                    templateProperties.put("pos_words_boost", String.valueOf(posb));
                    templateProperties.put("neg_words_boost", String.valueOf(negb));
                    String suffix = "XXpos" + df.format(posb) + "neg" + df.format(negb);
                    runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                }
            }
        } else if (what.equals("additional")) {
            for (double cancerb = .4; cancerb < 2; cancerb += .4) {
                for (double chemob = .4; chemob < 2; chemob += .4) {
                    for (double dnab = .4; dnab < 2; dnab += .4) {
                        for (double nonmelb = -1; nonmelb < .8; nonmelb += .4) {
                            templateProperties.put("cancer_boost", String.valueOf(cancerb));
                            templateProperties.put("chemo_boost", String.valueOf(chemob));
                            templateProperties.put("dna_boost", String.valueOf(dnab));
                            templateProperties.put("non_mel_boost", String.valueOf(dnab));
                            String suffix = "XXcanc" + df.format(cancerb) + "chem" + df.format(chemob) + "dna" + df.format(dnab) + "nonmel" + df.format(nonmelb);
                            runExperiments(templateProperties, goldStandard, target, year, what, suffix);
                        }
                    }
                }
            }
        } else if (what.equals("extra")) {
            for (double extrab = .4; extrab < 2; extrab += .4) {
                templateProperties.put("extra_boost", String.valueOf(extrab));
                String suffix = "XXextra" + df.format(extrab);
                runExperiments(templateProperties, goldStandard, target, year, what, suffix);
            }
        } else if (what.equals("pmgs")) {
            for (double pmgsb = -1; pmgsb < .8; pmgsb += .4) {
                templateProperties.put("pm_gs_boost", String.valueOf(pmgsb));
                String suffix = "XXpmgs" + df.format(pmgsb);
                runExperiments(templateProperties, goldStandard, target, year, what, suffix);
            }
        } else throw new IllegalStateException("Unknown mode " + what);


    }
}
