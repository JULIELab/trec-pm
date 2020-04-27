package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonTemplateQueryDecorator<T extends QueryDescription> extends JsonMapQueryDecorator<T> {
    private static final Logger log = LoggerFactory.getLogger(JsonTemplateQueryDecorator.class);
    private static final Pattern LOOP_PATTERN = Pattern.compile("(\")\\$\\{FOR\\s+INDEX\\s+IN\\s(\\w+)((\\[[^]]*])+)?\\s+REPEAT\\s+(\\w+\\.json)}(\")", Pattern.CASE_INSENSITIVE);
    protected String template;
    private boolean prettyPrint;
    private boolean checkSyntax;

    /**
     * @param template       File to the JSON template. Elements of the topic or the passed properties must be enclosed by double
     *                       curly braces to be correctly filled with the desired values.
     * @param decoratedQuery The query to be decorated
     */
    public JsonTemplateQueryDecorator(String template, Query<T> decoratedQuery) {
        this(template, decoratedQuery, false, false);
    }

    /**
     * @param template       File to the JSON template. Elements of the topic or the passed properties must be enclosed by double
     *                       curly braces to be correctly filled with the desired values.
     * @param decoratedQuery The query to be decorated
     */
    public JsonTemplateQueryDecorator(String template, Query<T> decoratedQuery, boolean prettyPrint, boolean checkSyntax) {
        super(decoratedQuery);
        this.prettyPrint = prettyPrint;
        this.checkSyntax = checkSyntax;
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
        return expandTemplateExpressions(topic, template, new ArrayList<>());
    }

    private String expandTemplateExpressions(T topic, String template, List<Integer> indices) {
        StringBuilder sb = new StringBuilder();
        Matcher m = LOOP_PATTERN.matcher(template);
        Map<String, Object> topicAttributes = topic.getAttributes();
        while (m.find()) {
            String field = m.group(2);
            List<Integer> effectiveIndices = getEffectiveIndices(indices, m, 3);
            String indexSpec = m.group(4);
            String templatePath = m.group(5);
            Object objectToIterateOver = topicAttributes.get(field);
            if (objectToIterateOver == null)
                throwTopicFieldDoesNotExist(m, field);
            if (indexSpec != null)
                objectToIterateOver = getValueAtIndex(topicAttributes.get(field), field, m, effectiveIndices, 0);
            String subtemplate = readTemplate(TrecConfig.SUBTEMPLATES_FOLDER + templatePath);
            StringBuilder filledSubtemplates = new StringBuilder();
            assert objectToIterateOver != null;
            int collectionSize = getCollectionSize(objectToIterateOver, field, m);
            String ls = System.getProperty("line.separator");
            for (int i = 0; i < collectionSize; i++) {
                ArrayList<Integer> recursiveIndices = new ArrayList<>(effectiveIndices);
                recursiveIndices.add(i);
                filledSubtemplates.append(expandTemplateExpressions(topic, subtemplate, recursiveIndices));
                if (i < collectionSize - 1) {
                    filledSubtemplates.append(",").append(ls);
                }
            }
            m.appendReplacement(sb, filledSubtemplates.toString());
        }
        m.appendTail(sb);
        String templateWithExpandedSubTemplates = sb.toString();
        // TODO pass correct index array
        String mappedTemplate = map(templateWithExpandedSubTemplates, topicAttributes, indices);
        // TODO checkDanglingTemplateExpressions
        if (prettyPrint || checkSyntax) {
            try {
                mappedTemplate = new JSONObject(mappedTemplate).toString(prettyPrint ? 4 : 0);
            } catch (JSONException e) {
                log.error("The created JSON document is invalid. The document is {}", mappedTemplate, e);
                throw e;
            }
        }
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
