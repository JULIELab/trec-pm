package at.medunigraz.imi.bst.trec.stats;

import at.medunigraz.imi.bst.trec.model.Metrics;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVStatsWriter implements StatsWriter {

	private CSVWriter writer;

	public CSVStatsWriter(File output) {
		try {
			writer = new CSVWriter(new FileWriter(output));
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeHeader();
	}

	private void writeHeader() {
		writer.writeNext(FIELDS);
		flush();
	}

	@Override
	public void write(String topic, Metrics metrics) {
		String[] entries = new String[FIELDS.length];
		entries[0] = topic;
		for (int i = 1; i < FIELDS.length; i++) {
			entries[i] = String.valueOf(metrics.getMetric(FIELDS[i]));

		}

		writer.writeNext(entries);
	}

	@Override
	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
