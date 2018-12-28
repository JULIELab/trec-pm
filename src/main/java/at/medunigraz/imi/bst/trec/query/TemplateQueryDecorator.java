package at.medunigraz.imi.bst.trec.query;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.Topic;

public class TemplateQueryDecorator extends MapQueryDecorator {
	
	protected File template;
	private final Map<String, String> templateProperties;

	/**
	 * Matches double+ commas with any whitespace in between (this happens when two dynamic subtemplates are not expanded).
	 * @todo cleanup double curly braces
	 */
	private static final Pattern DOUBLE_COMMA = Pattern.compile("(\\p{javaWhitespace}*,){2,}");

	public TemplateQueryDecorator(File template, Query decoratedQuery) {
		this(template, decoratedQuery, Collections.emptyMap());
	}

	/**
	 *
	 * @param template File to the JSON template. Elements of the topic or the passed properties must be enclosed by double
	 *                 curly braces to be correctly filled with the desired values.
	 * @param decoratedQuery The query to be decorated
	 * @param templateProperties A map string map that allows to add arbitrary properties that can be used in the template.
	 */
	public TemplateQueryDecorator(File template, Query decoratedQuery, Map<String, String> templateProperties) {
		super(decoratedQuery);
		this.template = template;
		// XXX This cannot be called here anymore, as the final template generated may depend on the topic
		//loadTemplate(null);
		this.templateProperties = templateProperties;
	}

	@Override
	public List<Result> query(Topic topic) {
	    // We reload the template for each new query, as the jsonQuery has been filled with the previous topic data
		loadTemplate(topic);
		map(topic.getAttributes());
		map(templateProperties);
		setJSONQuery(cleanup(getJSONQuery()));
		return decoratedQuery.query(topic);
	}

	protected static String readTemplate(File template) {
		String ret = "";
		try {
			ret = FileUtils.readFileToString(template, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	protected void loadTemplate(Topic topic) {
		setJSONQuery(readTemplate(template));
	}

	private static String cleanup(String template) {
		return DOUBLE_COMMA.matcher(template).replaceAll(",");
	}
	
	@Override
	protected String getMyName() {
		return getSimpleClassName() + "(" + template.getName() + ")";
	}

}
