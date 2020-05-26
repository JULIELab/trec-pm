package de.julielab.ir.experiments.ablation;

import org.assertj.core.data.Offset;
import org.junit.BeforeClass;
import org.junit.Test;

import static de.julielab.ir.paramopt.HttpParamOptServer.INFNDCG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class AblationCrossValResultTest {

    private static AblationCrossValResult result;

    @BeforeClass
    public static void setup() {
        result = new AblationCrossValResult("experiment");
        result.add(new AblationComparisonPair("1", INFNDCG, new double[]{0.8}, new double[]{0.1}));
        result.add(new AblationComparisonPair("2", INFNDCG, new double[]{0.2}, new double[]{0.5}));
        result.add(new AblationComparisonPair("3", INFNDCG, new double[]{0.6}, new double[]{0.7}));
    }
    @Test
    public void getMeanReferenceScore() {
        assertThat(result.getMeanReferenceScore(INFNDCG)).isCloseTo(0.533, Offset.offset(0.001));
    }

    @Test
    public void getMeanAblationScore() {
        assertThat(result.getMeanAblationScore(INFNDCG)).isCloseTo(0.433, Offset.offset(0.001));
    }

    @Test
    public void getReferenceStandardDeviation() {
        assertThat(result.getReferenceStandardDeviation(INFNDCG)).isCloseTo(0.305505, Offset.offset(0.000001));
    }

    @Test
    public void getAblationStandardDeviation() {
        assertThat(result.getAblationStandardDeviation(INFNDCG)).isCloseTo(0.305505, Offset.offset(0.000001));
    }

    @Test
    public void checkDiff() {
        assertEquals("! -Solid Tumor\t0.5545\t0.8283\t0.3919\t0.8592\t0.2911\t0.5445\t0.6713\t0.339\t0.7496\t0.2488\t0.7722\t0.7675\t0.2375\t0.6342\t0.4087\t0.6568\t0.698\t0.4433\t0.4752\t0.4102\t0.3574\t0.5924\t0.8458\t0.549\t0.4373\t0.6972\t0.2476\t0.5485\t0.5542\t0.3843\t0.6962\t0.8926\t0.6642\t0.8321\t0.8257\t0.8731\t0.5365\t0.6526\t0.6039\t0.798\t0.5359\t0.3818\t0.6194\t0.5989\t0.2704\t0.0787\t0.798\t0.539\t0.7723\t0.2104\t0.8239\t0.2399\t0.6504\t0.0\t0.4232\t0.7495\t0.8407\t0.9483\t0.8091\t0.645\t0.4007\t0.6464\t0.7503\t0.4008\t0.2442\t0.3743\t0.7821\t0.7782\t0.4367\t0.6543\t0.6223\t0.4677\t0.3826\t0.2808\t0.8899\t0.9336\t0.9887\t0.6661\t0.2837\t0.555\t0.6153\t0.7177\t0.399\t0.5957\t0.7668\t0.8655\t0.9007\t0.531\t0.7759\t0.5529\t0.7157\t0.4017\t0.4733\t0.2048\t0.5631\t0.9188\t0.3074\t0.787\t0.4278\t0.3154\t0.621\t0.8143\t0.5738\t0.4273\t0.8833\t0.6542\t0.5488\t0.831\t0.5843\t0.8779\t0.6509\t0.4369\t0.2371\t0.3491\t0.586\t0.4528\t0.7472\t0.3714\t0.4159\t0.2924\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953", "! -Solid Tumor\t0.5545\t0.8283\t0.3919\t0.8592\t0.2911\t0.5445\t0.6713\t0.339\t0.7496\t0.2488\t0.7722\t0.7675\t0.2375\t0.6342\t0.4087\t0.6568\t0.698\t0.4433\t0.4752\t0.4102\t0.3574\t0.5924\t0.8458\t0.549\t0.4373\t0.6972\t0.2476\t0.5485\t0.5542\t0.3843\t0.6962\t0.8926\t0.6642\t0.8321\t0.8257\t0.8731\t0.5365\t0.6526\t0.6039\t0.798\t0.5359\t0.3818\t0.6194\t0.5989\t0.2704\t0.0787\t0.798\t0.539\t0.7723\t0.2104\t0.8239\t0.2399\t0.6504\t0.0\t0.4232\t0.7495\t0.8407\t0.9483\t0.8091\t0.645\t0.4007\t0.6464\t0.7503\t0.4008\t0.2442\t0.3743\t0.7821\t0.7782\t0.4367\t0.6543\t0.6223\t0.4677\t0.3826\t0.2808\t0.8899\t0.9336\t0.9887\t0.6661\t0.2837\t0.555\t0.6153\t0.7177\t0.399\t0.5957\t0.4438\t0.8655\t0.9007\t0.531\t0.7759\t0.5529\t0.7157\t0.4017\t0.4733\t0.2048\t0.5631\t0.9188\t0.3074\t0.787\t0.4278\t0.3154\t0.621\t0.8143\t0.5738\t0.4273\t0.8833\t0.6542\t0.5488\t0.831\t0.5843\t0.8779\t0.6509\t0.4369\t0.2371\t0.3491\t0.586\t0.4528\t0.7472\t0.3714\t0.4159\t0.2924\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953\t0.5953");
    }
}