package de.julielab.ir.experiments.ablation;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class AblationLatexTableBuilderTest {

    @Test
    public void buildLatexTable() {
        AblationCrossValResult r = new AblationCrossValResult("td1");
        r.add(new AblationComparisonPair("td1", "infndcg", new double[]{0.6}, new double[]{0.3}));
        r.add(new AblationComparisonPair("td1", "infndcg", new double[]{0.3}, new double[]{0.2}));
        r.add(new AblationComparisonPair("td1", "infndcg", new double[]{0.8}, new double[]{0.4}));
        AblationCrossValResult r2 = new AblationCrossValResult("td2");
        r2.add(new AblationComparisonPair("td2", "infndcg", new double[]{0.6}, new double[]{0.5}));
        r2.add(new AblationComparisonPair("td2", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r2.add(new AblationComparisonPair("td2", "infndcg", new double[]{0.8}, new double[]{0.7}));
        AblationCrossValResult r3 = new AblationCrossValResult("td3");
        r3.add(new AblationComparisonPair("td3", "infndcg", new double[]{0.6}, new double[]{0.6}));
        r3.add(new AblationComparisonPair("td3", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r3.add(new AblationComparisonPair("td3", "infndcg", new double[]{0.8}, new double[]{0.7}));
        AblationCrossValResult r4 = new AblationCrossValResult("td4");
        r4.add(new AblationComparisonPair("td4", "infndcg", new double[]{0.6}, new double[]{0.7}));
        r4.add(new AblationComparisonPair("td4", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r4.add(new AblationComparisonPair("td4", "infndcg", new double[]{0.8}, new double[]{0.8}));

        Map<String, AblationCrossValResult> topDown = new LinkedHashMap<>();
        topDown.put("td1", r);
        topDown.put("td2", r2);
        topDown.put("td3", r3);
        topDown.put("td4", r4);


        AblationCrossValResult r5 = new AblationCrossValResult("bu1");
        r5.add(new AblationComparisonPair("bu1", "infndcg", new double[]{0.1}, new double[]{0.3}));
        r5.add(new AblationComparisonPair("bu1", "infndcg", new double[]{0.3}, new double[]{0.2}));
        r5.add(new AblationComparisonPair("bu1", "infndcg", new double[]{0.2}, new double[]{0.4}));
        AblationCrossValResult r6 = new AblationCrossValResult("bu2");
        r6.add(new AblationComparisonPair("bu2", "infndcg", new double[]{0.1}, new double[]{0.5}));
        r6.add(new AblationComparisonPair("bu2", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r6.add(new AblationComparisonPair("bu2", "infndcg", new double[]{0.2}, new double[]{0.7}));
        AblationCrossValResult r7 = new AblationCrossValResult("bu3");
        r7.add(new AblationComparisonPair("bu3", "infndcg", new double[]{0.1}, new double[]{0.6}));
        r7.add(new AblationComparisonPair("bu3", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r7.add(new AblationComparisonPair("bu3", "infndcg", new double[]{0.2}, new double[]{0.7}));
        AblationCrossValResult r8 = new AblationCrossValResult("bu4");
        r8.add(new AblationComparisonPair("bu4", "infndcg", new double[]{0.1}, new double[]{0.7}));
        r8.add(new AblationComparisonPair("bu4", "infndcg", new double[]{0.3}, new double[]{0.3}));
        r8.add(new AblationComparisonPair("bu4", "infndcg", new double[]{0.2}, new double[]{0.8}));

        Map<String, AblationCrossValResult> bottomUp = new LinkedHashMap<>();
        bottomUp.put("bu1", r5);
        bottomUp.put("bu2", r6);
        bottomUp.put("bu3", r7);
        bottomUp.put("bu4", r8);

        StringBuilder sb = AblationLatexTableBuilder.buildLatexTable(topDown, bottomUp, null, null, null, null);

        System.out.println(sb);
    }
}