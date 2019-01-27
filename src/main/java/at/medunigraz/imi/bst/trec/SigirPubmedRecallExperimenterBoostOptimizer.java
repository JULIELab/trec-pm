package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

public class SigirPubmedRecallExperimenterBoostOptimizer extends SuperSigirPubmedRecallExperimenter {
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
            System.err.println("Usage: " + SigirPubmedRecallExperimenterBoostOptimizer.class.getSimpleName() + " <what>");
            System.err.println("Where <what> is one of " + validParams);
            System.exit(1);
        }

        String what = args[0];

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = 2017;


        Map<String, String> templateProperties = Collections.unmodifiableMap(SigirParameters.LITERATURE_ES_DEFAULTS);


        DecimalFormat df = new DecimalFormat("0.0");
        if (what.equals("disease")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double disb = .4; disb < 2.4; disb += .4) {
                for (double ptb = .2; ptb < 1.2; ptb += .4) {
                    for (double synb = .2; synb < 2; synb += .4) {
                        Map<String, String> paramcombination = new HashMap<>(templateProperties);
                        paramcombination.put("dis_boost", String.valueOf(disb));
                        paramcombination.put("dis_prefterm_boost", String.valueOf(ptb));
                        paramcombination.put("dis_syn_boost", String.valueOf(synb));
                        String suffix = "--dis" + df.format(disb) + "-pt" + df.format(ptb) + "-syn" + df.format(synb);
                        parameters.add(paramcombination);
                        suffixes.add(suffix);
                    }
                }
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("gene")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double genb = .4; genb < 2.4; genb += .4) {
                for (double descb = .2; descb < 1.8; descb += .4) {
                    for (double synb = .2; synb < 1.8; synb += .4) {
                        Map<String, String> paramcombination = new HashMap<>(templateProperties);
                        paramcombination.put("gene_boost", String.valueOf(genb));
                        paramcombination.put("gene_desc_boost", String.valueOf(descb));
                        paramcombination.put("gene_syn_boost", String.valueOf(synb));
                        String suffix = "--gen" + df.format(genb) + "-desc" + df.format(descb) + "-syn" + df.format(synb);
                        parameters.add(paramcombination);
                        suffixes.add(suffix);
                    }
                }
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("fields")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double titb = 1; titb < 1.6; titb += .2) {
                for (double abstrb = 1; abstrb < 1.6; abstrb += .2) {
                    for (double kwb = 1; kwb < 1.6; kwb += .2) {
                        for (double meshb = 1; meshb < 1.6; meshb += .2) {
                            for (double genesb = 1; genesb < 1.1; genesb += .4) {
                                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                                paramcombination.put("title_boost", "^" + titb);
                                paramcombination.put("abstract_boost", "^" + abstrb);
                                paramcombination.put("keyword_boost", "^" + kwb);
                                paramcombination.put("meshTags_boost", "^" + meshb);
                                paramcombination.put("genes_field_boost", "^" + genesb);
                                String suffix = "--tit" + df.format(titb) + "-ab" + df.format(abstrb) + "-kw" + df.format(kwb) + "-msh" + df.format(meshb) + "-gen" + df.format(genesb);
                                parameters.add(paramcombination);
                                suffixes.add(suffix);
                            }
                        }
                    }
                }
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("posneg")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double posb = .7; posb < 1.2; posb += .1) {
                for (double negb = -3; negb < .2; negb += .4) {
                    Map<String, String> paramcombination = new HashMap<>(templateProperties);
                    paramcombination.put("pos_words_boost", String.valueOf(posb));
                    paramcombination.put("neg_words_boost", String.valueOf(negb));
                    String suffix = "--pos" + df.format(posb) + "-neg" + df.format(negb);
                    parameters.add(paramcombination);
                    suffixes.add(suffix);
                }
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("additional")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double cancerb = .4; cancerb < 2; cancerb += .4) {
                for (double chemob = .4; chemob < 2; chemob += .4) {
                    for (double dnab = .4; dnab < 2; dnab += .4) {
                        for (double nonmelb = -1; nonmelb < .8; nonmelb += .4) {
                            Map<String, String> paramcombination = new HashMap<>(templateProperties);
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
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("extra")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double extrab = .4; extrab < 2; extrab += .4) {
                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                paramcombination.put("extra_boost", String.valueOf(extrab));
                String suffix = "--extra" + df.format(extrab);
                parameters.add(paramcombination);
                suffixes.add(suffix);
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("pmgs")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double pmgsb = -1; pmgsb < .8; pmgsb += .4) {
                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                paramcombination.put("pm_gs_boost", String.valueOf(pmgsb));
                String suffix = "--pmgs" + df.format(pmgsb);
                parameters.add(paramcombination);
                suffixes.add(suffix);
            }
            runExperimentsWithParameters(parameters, suffixes, year, what, goldStandard, target);
        }
        if (what.equals("pmclass")) {
            final List<String> pmfields = Arrays.asList("pmclass2017lstm.keyword",
                    "pmclass2017lstmatt.keyword",
                    "pmclass2017lstmgru.keyword",
                    "pmclass2018lstm.keyword",
                    "pmclass2018lstmatt.keyword",
                    "pmclass2018lstmgru.keyword",
                    "pmclass2017.keyword",
                    "pmclass2018.keyword");
            //final List<String> pmfields = Arrays.asList( "pmclass2017.keyword");
            pmfields.parallelStream().forEach(pmfield -> {
                Map<String, String> parameters = new HashMap<>(templateProperties);
                parameters.put("pm_class_field", pmfield);
                runExperiments(parameters, false, goldStandard, target, year, what, "-" + pmfield);
            });
        } else throw new IllegalStateException("Unknown mode " + what);


    }

    private static void runExperimentsWithParameters(List<Map<String, String>> parameters, List<String> suffixes, int year, String what, Experiment.GoldStandard goldStandard, Experiment.Task target) {
        IntStream.range(0, parameters.size()).parallel().forEach(i -> {
            Map<String, String> parameterset = parameters.get(i);
            String suffix = suffixes.get(i);
            runExperiments(parameterset, false, goldStandard, target, year, what, suffix);
        });
    }
}
