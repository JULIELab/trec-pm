package at.medunigraz.imi.bst.clinicaltrial;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ClinicalTrialTest {

    @Test
    public void baseline() {
    	File xmlFile = new File(getClass().getResource("/data/clinicaltrials-samples/NCT00283075.xml").getFile());

        ClinicalTrial trial = ClinicalTrial.fromXml(xmlFile.getAbsolutePath());

        assertThat(trial.brief_title, startsWith("Mouse Cancer Cell-containing Macrobeads in the Treatment of Human"));
        assertThat(trial.official_title, startsWith("Use of Mouse Renal Adenocarcinoma Cell-containing"));
        assertThat(trial.summary, startsWith("This is a phase 1 trial to evaluate the safety and toxicity"));
        assertThat(trial.description, startsWith("Cancer in its various forms continues to be a major U.S. health"));
        assertThat(trial.studyType, is("Interventional"));
        assertThat(trial.interventionModel, is("Single Group Assignment"));
        assertThat(trial.primaryPurpose, is("Treatment"));
        assertThat(trial.outcomeMeasures, hasItems("Maximum Tolerated Dose (MTD) of RENCA Macrobeads", "Tumor Marker Response"));
        assertThat(trial.outcomeDescriptions, hasSize(4));
        assertThat(trial.conditions, contains("Intraabdominal Cancers (Various Types)"));
        assertThat(trial.interventionTypes, hasItems("Biological"));
        assertThat(trial.interventionNames, hasItems("Cancer Macrobead placement in abdominal cavity"));
        assertThat(trial.armGroupDescriptions, contains("Cancer Macrobead placement in abdominal cavity"));
        assertThat(trial.sex, contains("female", "male"));
        assertThat(trial.minAge, is(18));
        assertThat(trial.maxAge, is(65));
        assertThat(trial.inclusion, containsString("End-stage"));
        assertThat(trial.inclusion, containsString("survival"));
        assertThat(trial.exclusion, containsString("Multiple"));
        assertThat(trial.exclusion, containsString("individual"));
        assertThat(trial.keywords, hasItems("intraabdominal cancer (carcinomas)", "cancer cell growth inhibition"));
        assertThat(trial.meshTags, empty());
    }

    @Test
    public void noExclusionCriteria() {
        File xmlFile = new File(getClass().getResource("/data/clinicaltrials-samples/NCT02912559.xml").getFile());

        ClinicalTrial trial = ClinicalTrial.fromXml(xmlFile.getAbsolutePath());

        assertThat(trial.brief_title, startsWith("Combination Chemotherapy With or Without Atezolizumab in Treating"));
        assertThat(trial.official_title, startsWith("Randomized Trial of FOLFOX Alone or Combined With Atezolizumab"));
        assertThat(trial.summary, startsWith("This randomized phase III trial studies combination chemotherapy and"));
        assertThat(trial.description, startsWith("PRIMARY OBJECTIVES:"));
        assertThat(trial.studyType, is("Interventional"));
        assertThat(trial.interventionModel, is("Parallel Assignment"));
        assertThat(trial.primaryPurpose, is("Treatment"));
        assertThat(trial.outcomeMeasures, hasItems("DFS", "OS"));
        assertThat(trial.outcomeDescriptions, hasSize(3));
        assertThat(trial.conditions, hasSize(7));
        assertThat(trial.interventionTypes, hasItems("Drug", "Other"));
        assertThat(trial.interventionNames, hasItems("Atezolizumab", "Laboratory Biomarker Analysis", "Quality-of-Life Assessment"));
        assertThat(trial.armGroupDescriptions, hasSize(2));
        assertThat(trial.sex, contains("female", "male"));
        assertThat(trial.minAge, is(18));
        assertThat(trial.maxAge, is(100));
        assertThat(trial.inclusion, containsString("Histologically"));
        assertThat(trial.inclusion, containsString("leucovorin"));
        assertThat(trial.exclusion, isEmptyOrNullString());
        assertThat(trial.keywords, empty());
        assertThat(trial.meshTags, hasItems("Adenocarcinoma", "Colonic Neoplasms", "Calcium, Dietary", "Folic Acid"));
    }

    @Test
    public void noColon() {
        File xmlFile = new File(getClass().getResource("/data/clinicaltrials-samples/NCT00897650.xml").getFile());

        ClinicalTrial trial = ClinicalTrial.fromXml(xmlFile.getAbsolutePath());

        assertThat(trial.inclusion, containsString("Diagnosis of suspected lung cancer"));
        assertThat(trial.exclusion, containsString("Inability to undergo therapy"));
    }

    @Test
    public void noInclusionCriteria() {
        File xmlFile = new File(getClass().getResource("/data/clinicaltrials-samples/NCT00445783.xml").getFile());

        ClinicalTrial trial = ClinicalTrial.fromXml(xmlFile.getAbsolutePath());

        assertThat(trial.inclusion, containsString("Newly diagnosed primary invasive"));
        assertThat(trial.inclusion, containsString("Healthy participant meeting"));
        assertThat(trial.exclusion, isEmptyOrNullString());
    }

}