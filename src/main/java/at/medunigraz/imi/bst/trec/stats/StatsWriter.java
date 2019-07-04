package at.medunigraz.imi.bst.trec.stats;

import at.medunigraz.imi.bst.trec.model.Metrics;

import java.io.Closeable;
import java.io.Flushable;
import java.util.Map;

public interface StatsWriter extends Closeable, Flushable {

	public static final String[] FIELDS = new String[] { "Topic", "ndcg", "Rprec", "infNDCG", "P_5", "P_10", "P_15", "recall_5",
			"recall_10", "set_P", "set_recall", "set_F" };

	public default void write(Map<String, Metrics> metricsByTopic) {
		for (Map.Entry<String, Metrics> entry : metricsByTopic.entrySet()) {
			write(entry.getKey(), entry.getValue());
		}
	}

	public void write(String topic, Metrics metrics);

}