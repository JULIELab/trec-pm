package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTemplateQueryDecorator<T extends QueryDescription> extends JsonMapQueryDecorator<T> {
    private static final Logger log = LoggerFactory.getLogger(JsonTemplateQueryDecorator.class);
    private static final Pattern LOOP_PATTERN = Pattern.compile("\\$\\{FOR\\s+INDEX\\s+IN\\s(\\w+)(\\[(\\$INDEX|[0-9]+)])?\\s+REPEAT\\s+(\\w+\\.json)}");
    protected String template;

    /**
     * @param template       File to the JSON template. Elements of the topic or the passed properties must be enclosed by double
     *                       curly braces to be correctly filled with the desired values.
     * @param decoratedQuery The query to be decorated
     */
    public JsonTemplateQueryDecorator(String template, Query<T> decoratedQuery) {
        super(decoratedQuery);
        if (template == null)
            throw new IllegalArgumentException("The passed template is null");
        this.template = readTemplate(template);
    }

    protected static String readTemplate(String template) {
        String ret = "";
        try {
            InputStream resource = FileUtilities.findResource(template);
            if (resource == null)
                throw new FileNotFoundException("Did not find resource " + template + " as file or as classpath resource.");
            ret = IOStreamUtilities.getStringFromInputStream(resource);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    @Override
    public List<Result> query(T topic) {
        // We reload the template for each new query, as the jsonQuery has been filled with the previous topic data
        expandTemplateExpressions(topic);
        setJSONQuery(getJSONQuery());
        try {
            return decoratedQuery.query(topic);
        } catch (JSONException e) {
            log.error("JSON exception when trying to build template from {}: {}", template, getJSONQuery());
            throw e;
        }
    }
    public String expandTemplateExpressions(T topic) {
        return expandTemplateExpressions(topic, template, -1);
    }

    private String expandTemplateExpressions(T topic, String template, int index) {
        StringBuilder sb = new StringBuilder();
        Matcher m = LOOP_PATTERN.matcher(template);
        Map<String, Object> topicAttributes = topic.getAttributes();
        while (m.find()) {
            String field = m.group(1);
            String indexSpec = m.group(3);
            String templatePath = m.group(4);
            boolean constantIndexGiven = indexSpec != null && indexSpec.matches("[0-9]+");
            int constantIndex = constantIndexGiven ? Integer.parseInt(indexSpec) : -1;
            // if a constant index was specified it takes precedence over the dynamic index
            int effectiveIndex = constantIndexGiven ? constantIndex : index;
            Object objectToIterateOver = topicAttributes.get(field);
            if (objectToIterateOver == null)
                throwTopicFieldDoesNotExist(m, field);
            if (indexSpec != null)
                objectToIterateOver = getCollectionElement(objectToIterateOver, effectiveIndex, field, m);
            String subtemplate = readTemplate(TrecConfig.SUBTEMPLATES_FOLDER + templatePath);
            StringBuilder filledSubtemplates = new StringBuilder();
            int collectionSize = getCollectionSize(objectToIterateOver, field, m);
            String ls = System.getProperty("line.separator");
            for (int i = 0; i < collectionSize; i++) {
                filledSubtemplates.append(expandTemplateExpressions(topic, subtemplate, i));
                if (i < collectionSize - 1) {
                    filledSubtemplates.append(",").append(ls);
                }
            }
            m.appendReplacement(sb, filledSubtemplates.toString());
        }
        m.appendTail(sb);
        String templateWithExpandedSubTemplates = sb.toString();
        String mappedTemplate = map(templateWithExpandedSubTemplates, topicAttributes, -1);
        // TODO checkDanglingTemplateExpressions
        return mappedTemplate;
    }

    private void checkDanglingTemplateExpressions(String jsonQuery) {
        Pattern p = Pattern.compile("\\{\\{([^}]+)}}");
        final Matcher matcher = p.matcher(jsonQuery);
        Set<String> missingTemplates = new HashSet<>();
        while (matcher.find()) {
            missingTemplates.add(matcher.group(1));
        }
        if (!missingTemplates.isEmpty())
            throw new IllegalStateException("The following template properties have not been set: " + missingTemplates);
    }


    @Override
    protected String getMyName() {
        return getSimpleClassName() + "(" + template + ")";
    }

}
