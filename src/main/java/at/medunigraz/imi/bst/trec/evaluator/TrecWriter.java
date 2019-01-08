package at.medunigraz.imi.bst.trec.evaluator;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import com.opencsv.CSVWriter;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;

public class TrecWriter implements Closeable {
	private static final String VALID_RUN_NAME_REGEX = "[a-zA-Z0-9]{1,12}";
	private static final int NUM_FIELDS = 6;
	private CSVWriter writer;
	private String runName;

	public TrecWriter(File output, String runName) {
		if (!checkRunName(runName)) {
			throw new RuntimeException("Invalid run name!");
		}

		this.runName = runName;

		try {
			writer = new CSVWriter(new FileWriter(output), '\t', CSVWriter.NO_QUOTE_CHARACTER, '\\', System.getProperty("line.separator"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkRunName(String runName) {
		//final Pattern valid = Pattern.compile(VALID_RUN_NAME_REGEX);
		//return valid.matcher(runName).matches();
		return true;
	}
	
	public void write(List<ResultList> resultListSet) {
		for (ResultList resultList : resultListSet) {
			write(resultList);
		}
	}

	public void write(ResultList resultList) {		
		String[] entries = new String[NUM_FIELDS];
		
		// Sets fixed fields
		entries[0] = String.valueOf(resultList.getTopic().getNumber());
		entries[1] = "Q0";
		entries[5] = runName; // XXX must be 1-12 alphanumeric characters
		
		int rank = 1;
		for (Result result : resultList.getResults()) {
			entries[2] = String.valueOf(result.getId());
			entries[3] = String.valueOf(rank++);
			entries[4] = String.format(Locale.ROOT, "%.6f", result.getScore());
			writer.writeNext(entries);
		}
	}
	
	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
