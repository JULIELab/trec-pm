package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.ir.model.QueryDescription;
import de.julielab.java.utilities.FileUtilities;
import de.julielab.java.utilities.IOStreamUtilities;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Handles the following template expressions:
 * <ul>
 * <li>"${FOR INDEX IN topicField REPEAT templatePath.json}" - topicField must be collection; one copy of 'templatePath' for each value from 'topicField'</li>
 * <li>"${FOR INDEX IN topicField[] REPEAT templatePath.json}" - recursive FOREACH application</li>
 * <li>"${INSERT templatePath.json}" - inserts the given template (after injecting topic values, if any are referenced)</li>
 * </ul>
 * </p>
 */
public class JsonTemplateQueryDecorator<T extends QueryDescription> extends JsonMapQueryDecorator<T> {
    private static final Logger log = LoggerFactory.getLogger(JsonTemplateQueryDecorator.class);
    private static final Pattern LOOP_PATTERN = Pattern.compile("(\")\\s*\\$\\{(FOR\\s+INDEX\\s+IN\\s(\\w+)((\\[[^]]*])+)?\\s+REPEAT|INSERT)\\s+([\\w\\/_-]+\\.json)}\\s*(\")", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    private static final Pattern DANGLING = Pattern.compile("\\$\\{[^}]+}");
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
        String resolvedTemplate = expandTemplateExpressions(topic, template, new ArrayList<>(), null);
        Matcher m = DANGLING.matcher(resolvedTemplate);
        boolean danglingFound = false;
        while (m.find()) {
            log.error("Found non-resolved template expression {}", m.group());
            danglingFound = true;
        }
        if (danglingFound)
            throw new IllegalArgumentException("Could not fully resolve the template at " + template + " with topic " + topic + ". Check the template expression syntax and the topic field names.");
        return resolvedTemplate;
    }

    private String expandTemplateExpressions(T topic, String template, List<Integer> indices, Object parentValue) {
        StringBuilder sb = new StringBuilder();
        Matcher m = LOOP_PATTERN.matcher(template);
        Map<String, Object> topicAttributes = topic.getAttributes();
        while (m.find()) {
            String modusGroup = m.group(2).toLowerCase();
            String templatePath = m.group(6);
            String subtemplate = readTemplate(TrecConfig.SUBTEMPLATES_FOLDER + templatePath);
            if (modusGroup.contains("for")) {
                String field = m.group(3);
                List<Integer> effectiveIndices = getEffectiveIndices(indices, m, 4);
                // Check if the current expression needs to access implicit indices from FOR INDEX IN expression
                // up the chain. This decides if this starts a new index list or extends the existing.
                boolean hasImplicitIndices = parseIndices(m.group(4), m).contains(-1);
                String indexSpec = m.group(5);
                Object objectToIterateOver = topicAttributes.get(field);
                if (objectToIterateOver == null)
                    throwTopicFieldDoesNotExist(m, field);
                if (indexSpec != null)
                    objectToIterateOver = getValueAtIndex(topicAttributes.get(field), field, m, effectiveIndices, 0);
                StringBuilder filledSubtemplates = new StringBuilder();
                // This is here to calm down IntelliJ which doesn't infer that objectToIterateOver can actually
                // not be null here.
                assert objectToIterateOver != null;
                int collectionSize = getCollectionSize(objectToIterateOver, field, m);
                String ls = System.getProperty("line.separator");
                for (int i = 0; i < collectionSize; i++) {
                    List<Integer> recursiveIndices;
                    if (hasImplicitIndices) {
                        // This expression belongs to an existing chain of subindex accesses
                        recursiveIndices = new ArrayList<>(effectiveIndices);
                        recursiveIndices.add(i);
                    } else {
                        // This expression is independent of upstream applications of FOR INDEX IN expressions.
                        recursiveIndices = Collections.singletonList(i);
                    }
                    filledSubtemplates.append(expandTemplateExpressions(topic, subtemplate, recursiveIndices, getCollectionElement(objectToIterateOver, i, field, m)));
                    if (i < collectionSize - 1) {
                        filledSubtemplates.append(",").append(ls);
                    }
                }
                m.appendReplacement(sb, filledSubtemplates.toString());
            } else {
                // "INSERT"
                String filledSubtemplate = expandTemplateExpressions(topic, subtemplate, indices, null);
                m.appendReplacement(sb, filledSubtemplate);
            }
        }
        m.appendTail(sb);
        String templateWithExpandedSubTemplates = sb.toString();
        String mappedTemplate = map(templateWithExpandedSubTemplates, topicAttributes, parentValue, indices);
        if (prettyPrint || checkSyntax) {
            try {
                if (mappedTemplate.startsWith("{"))
                    mappedTemplate = new JSONObject(mappedTemplate).toString(prettyPrint ? 4 : 0);
                else
                    mappedTemplate = new JSONArray(mappedTemplate).toString(prettyPrint ? 4 : 0);
            } catch (JSONException e) {
                log.error("The created JSON document is invalid. The document is {}", mappedTemplate, e);
                throw e;
            }
        }
        return mappedTemplate;
    }

    @Override
    protected String getMyName() {
        return getSimpleClassName() + "(" + template + ")";
    }

}
