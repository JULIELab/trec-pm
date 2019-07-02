package at.medunigraz.imi.bst.trec;

import at.medunigraz.imi.bst.trec.experiment.Experiment;
import at.medunigraz.imi.bst.trec.experiment.ExperimentsBuilder;
import at.medunigraz.imi.bst.trec.model.GoldStandard;
import at.medunigraz.imi.bst.trec.model.Task;

import java.io.File;
import java.util.Set;

public class PubMedOnlineExperimenter {
    public static void main(String[] args) {
        final GoldStandard goldStandard = GoldStandard.OFFICIAL;
        final Task target = Task.PUBMED_ONLINE;
        final int year = 2017;


        ExperimentsBuilder builder = new ExperimentsBuilder();


        builder.newExperiment().withName("pmonlinetest").withYear(year).withGoldStandard(goldStandard).withTarget(target)
              .withWordRemoval().withGeneSynonym().
               withGeneDescription();


        Set<Experiment> experiments = builder.build();

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
