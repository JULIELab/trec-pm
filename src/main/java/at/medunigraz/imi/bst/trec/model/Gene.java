package at.medunigraz.imi.bst.trec.model;

import java.util.*;

public class Gene {
	private String symbol;
	private String[] synonyms;
	private String description;
	private String[] otherDesignations;

	private static final int SYMBOL_COLUMN = 2;
	private static final int SYNONYMS_COLUMN = 4;
	private static final int DESCRIPTION_COLUMN = 8;
	private static final int OTHER_DESIGNATIONS_COLUMN = 13;

	public static enum Field {
		SYMBOL, SYNONYMS, DESCRIPTION, OTHER_DESIGNATIONS
	}

	public Gene(String symbol, String[] synonyms, String description, String[] otherDesignations) {
		this.symbol = symbol;
		this.synonyms = synonyms;
		this.description = description;
		this.otherDesignations = otherDesignations;
	}

	public static Gene fromArray(String[] geneInfo) {
		String symbol = geneInfo[SYMBOL_COLUMN];
		String[] synonyms = parseMultipleField(geneInfo[SYNONYMS_COLUMN]);
		String description = geneInfo[DESCRIPTION_COLUMN];
		String[] otherDesignations = parseMultipleField(geneInfo[OTHER_DESIGNATIONS_COLUMN]);

		return new Gene(symbol, synonyms, description, otherDesignations);
	}

	public String getExpandedField(String delimiter, Gene.Field[] expandTo) {
		Set<String> expansions = new HashSet<>();

		for (Gene.Field field : expandTo) {
			switch (field) {
			case SYMBOL:
				expansions.add(getSymbol());
				break;
			case SYNONYMS:
				expansions.addAll(getSynonyms());
				break;
			case DESCRIPTION:
				expansions.add(getDescription());
				break;
			case OTHER_DESIGNATIONS:
				expansions.addAll(getOtherDesignations());
				break;
			default:
				break;
			}
		}

		StringBuilder ret = new StringBuilder();
		String prefix = "";
		for (String str : expansions) {
			ret.append(prefix);
			ret.append(escapeChars(str));
			prefix = delimiter;
		}

		return ret.toString();
	}
	
	private String escapeChars(String input) {
		return input.replace(",", " ");
	}

	private static String[] parseMultipleField(String field) {
		if (field.equals("-")) {
			return new String[0];
		}

		return field.split("\\|");
	}

	public String getSymbol() {
		return symbol;
	}

	public List<String> getSynonyms() {
		return new ArrayList<>(Arrays.asList(synonyms));
	}

	public String getDescription() {
		return description;
	}

	public List<String> getOtherDesignations() {
		return new ArrayList<>(Arrays.asList(otherDesignations));
	}

}
