package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class PubMedOnlineExperimenter {
    public static void main(String[] args) {
        final GoldStandard goldStandard = GoldStandard.OFFICIAL;
        final Task target = Task.PUBMED_ONLINE;
        final int year = 2017;

        Experiment pmonlinetest = new ExperimentBuilder().withName("pmonlinetest").withYear(year).withGoldStandard(goldStandard).withTarget(target)
              .withWordRemoval().withGeneSynonym().
               withGeneDescription().build();


        Set<Experiment> experiments = new LinkedHashSet<>(Arrays.asList(pmonlinetest));

        for (Experiment exp : experiments) {
            exp.start();
            try {
                exp.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
