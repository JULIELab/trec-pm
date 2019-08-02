package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.trec.model.Challenge;
import at.medunigraz.imi.bst.trec.model.Task;

public class StoredFieldsRegistry {
    public static String[] getStoredFields(Challenge challenge, Task task, int year) {
        if (challenge == Challenge.TREC_PM && task == Task.PUBMED && year == 2019)
            return new String[]{"focusedTreatmentCuis", "broadTreatmentCuis", "focusedTreatmentText", "broadTreatmentText"};
        return new String[0];
    }
    private StoredFieldsRegistry() {

    }
}
