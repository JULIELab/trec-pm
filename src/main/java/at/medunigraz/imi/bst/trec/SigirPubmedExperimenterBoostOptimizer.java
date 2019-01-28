package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;

import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.IntStream;

public class SigirPubmedExperimenterBoostOptimizer extends SuperSigirPubmedRecallExperimenter {
    public static void main(String[] args) {

        Set<String> validParams = new LinkedHashSet<>();
        validParams.add("genedis");
        validParams.add("fields");
        validParams.add("posneg");
        validParams.add("additional");
        validParams.add("extra");
        validParams.add("pmclass");
        validParams.add("mutation");
        validParams.add("drug");

        if (args.length != 2 || !validParams.contains(args[0])) {
            System.err.println("Usage: " + SigirPubmedExperimenterBoostOptimizer.class.getSimpleName() + " <what> <year>");
            System.err.println("Where <what> is one of " + validParams);
            System.exit(1);
        }

        String what = args[0];

        final Experiment.GoldStandard goldStandard = Experiment.GoldStandard.OFFICIAL;
        final Experiment.Task target = Experiment.Task.PUBMED;
        final int year = Integer.parseInt(args[1]);


        Map<String, String> templateProperties = Collections.unmodifiableMap(SigirParameters.LITERATURE_ES_DEFAULTS);


        DecimalFormat df = new DecimalFormat("0.0");
        if (what.equals("genedis")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double genb = 1; genb < 3; genb += .5) {
                for (double descb = 1; descb < 3; descb += .5) {
                    for (double gsynb = 1; gsynb < 3; gsynb += .5) {
                        for (double disb = 1; disb < 3; disb += .5) {
                            for (double dsynb = 1; dsynb < 3; gsynb += .5) {
                                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                                paramcombination.put("gene_boost", String.valueOf(genb));
                                paramcombination.put("gene_desc_boost", String.valueOf(descb));
                                paramcombination.put("gene_syn_boost", String.valueOf(gsynb));
                                paramcombination.put("dis_boost", String.valueOf(disb));
                                paramcombination.put("dis_syn_boost", String.valueOf(dsynb));
                                String suffix = "--gen" + df.format(genb) + "-gdes" + df.format(descb) + "-gsyn" + df.format(gsynb) + "--dis" + df.format(disb) + "-dsyn" + df.format(dsynb);
                                parameters.add(paramcombination);
                                suffixes.add(suffix);
                            }
                        }
                    }
                }
            }
            runExperimentsWithParameters("/templates/sigir19_experiments_biomed/baseline.json", parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("fields")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double titb = 1; titb < 3; titb += .5) {
                for (double abstrb = 1; abstrb < 3; abstrb += .5) {
                    for (double kwb = 1; kwb < 3; kwb += .5) {
                        for (double meshb = 1; meshb < 3; meshb += .5) {
                            for (double genesb = 1; genesb < 3; genesb += .5) {
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
            runExperimentsWithParameters("/templates/sigir19_field_boost_optimization", parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("posneg")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double posb = .5; posb < 3; posb += .5) {
                for (double negb = -3; negb <= .5; negb += .5) {
                    Map<String, String> paramcombination = new HashMap<>(templateProperties);
                    paramcombination.put("pos_words_boost", String.valueOf(posb));
                    paramcombination.put("neg_words_boost", String.valueOf(negb));
                    String suffix = "--pos" + df.format(posb) + "-neg" + df.format(negb);
                    parameters.add(paramcombination);
                    suffixes.add(suffix);
                }
            }
            runExperimentsWithParameters("/templates/sigir19_boost_optimization/posneg.json",parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("additional")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double cancerb = .5; cancerb < 3; cancerb += .5) {
                for (double chemob = .5; chemob < 3; chemob += .5) {
                    for (double dnab = .5; dnab < 3; dnab += .5) {
                        Map<String, String> paramcombination = new HashMap<>(templateProperties);
                        paramcombination.put("cancer_boost", String.valueOf(cancerb));
                        paramcombination.put("chemo_boost", String.valueOf(chemob));
                        paramcombination.put("dna_boost", String.valueOf(dnab));
                        String suffix = "--canc" + df.format(cancerb) + "-chem" + df.format(chemob) + "-dna" + df.format(dnab);
                        parameters.add(paramcombination);
                        suffixes.add(suffix);
                    }
                }
            }
            runExperimentsWithParameters("/templates/sigir19_boost_optimization/additional.json", parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("extra")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double extrab = .5; extrab <= 3; extrab += .5) {
                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                paramcombination.put("extra_boost", String.valueOf(extrab));
                String suffix = "--extra" + df.format(extrab);
                parameters.add(paramcombination);
                suffixes.add(suffix);
            }
            runExperimentsWithParameters("/templates/sigir19_experiments_biomed/extra.json",parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("mutation")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            for (double extrab = .5; extrab <= 3; extrab += .5) {
                Map<String, String> paramcombination = new HashMap<>(templateProperties);
                paramcombination.put("mut_boost", String.valueOf(extrab));
                String suffix = "--mut" + df.format(extrab);
                parameters.add(paramcombination);
                suffixes.add(suffix);
            }
            runExperimentsWithParameters("/templates/sigir19_experiments_biomed/mutations.json", parameters, suffixes, year, what, goldStandard, target);
        } else if (what.equals("pmclass")) {
            List<Map<String, String>> parameters = new ArrayList<>();
            List<String> suffixes = new ArrayList<>();
            final List<String> pmfields = Arrays.asList("pmclass2017lstm.keyword",
                    "pmclass2017lstmatt.keyword",
                    "pmclass2017lstmgru.keyword",
                    "pmclass2018lstm.keyword",
                    "pmclass2018lstmatt.keyword",
                    "pmclass2018lstmgru.keyword",
                    "pmclass2017.keyword",
                    "pmclass2018.keyword");
            for (String pmfield : pmfields) {
                for (double extrab = .5; extrab <= 3; extrab += .5) {
                    Map<String, String> paramcombination = new HashMap<>(templateProperties);
                    paramcombination.put("pm_boost", String.valueOf(extrab));
                    paramcombination.put("pm_class_field", pmfield);
                    String suffix = "--pm" + df.format(extrab) + "-pmf:";
                    parameters.add(paramcombination);
                    suffixes.add(suffix);
                }
            }
            runExperimentsWithParameters("/templates/sigir19_pmclass_biomed", parameters, suffixes, year, what, goldStandard, target);

        } else throw new IllegalStateException("Unknown mode " + what);


    }

    private static void runExperimentsWithParameters(String template, List<Map<String, String>> parameters, List<String> suffixes, int year, String what, Experiment.GoldStandard goldStandard, Experiment.Task target) {
        IntStream.range(0, parameters.size()).parallel().forEach(i -> {
            Map<String, String> parameterset = parameters.get(i);
            String suffix = suffixes.get(i);
             runBoostExperiments(template, parameterset, goldStandard, target, year, what, suffix);
        });
    }
}
