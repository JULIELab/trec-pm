package at.medunigraz.imi.bst.retrieval;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Topic;
import de.julielab.ir.model.QueryDescription;
import joptsimple.internal.Strings;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubTemplateQueryDecorator<T extends QueryDescription> extends TemplateQueryDecorator<T> {
    /**
     * <p>This Java system property allows to alter the classpath location where the subtemplates
     * are expected. The default location is <code>/subtemplates/</code>.</p>
     * <p>This is currently used in our unit tests.</p>
     */
    public static final String SUBTEMPLATES_FOLDER_PROP = "at.medunigraz.imi.bst.retrieval.subtemplates.folder";
    /**
     * Matches and captures regular filenames enclosed in double curly braces, e.g.
     * "{{positive_boosters.json}}" or
     * "{{diseaseSynonyms:disease_synonym.json}}" or
     * "{{biomedical_articles/positive_boosters.json}}" or
     * "{{diseaseSynonyms:biomedical_articles/disease_synonym.json}}"
     */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\{\\{(?:(\\w+):)?([/\\w]+\\.json)\\}\\}");

    /**
     * Matches e.g. "{{[diseaseSynonyms]}}"
     */
    private static final Pattern DYNAMIC_TEMPLATE_PATTERN = Pattern.compile("\\{\\{(\\[\\w+\\])\\}\\}");

    private static final String FIELD_SEPARATOR = ", ";

    public SubTemplateQueryDecorator(String template, Query decoratedQuery) {
        super(template, decoratedQuery);
    }

    @Override
    protected void loadTemplate(T topic) {
        setJSONQuery(recursiveLoadTemplate(topic, template));
    }

    private String recursiveLoadTemplate(T topic, String file) {
        String templateString = readTemplate(file);

        StringBuffer sb = new StringBuffer();

        Matcher matcher = TEMPLATE_PATTERN.matcher(templateString);
        while (matcher.find()) {
            String field = matcher.group(1);
            String filename = matcher.group(2);

            String subTemplate = recursiveLoadTemplate(topic, TrecConfig.SUBTEMPLATES_FOLDER + filename);

            // Handle dynamic expansions, e.g. {{diseaseSynonyms:disease_synonym.json}}
            // TODO consider DRY and simplify syntax: check only the existence of DYNAMIC_TEMPLATE_PATTERN
            if (field != null) {
                // TODO check empty expansions
                subTemplate = handleDynamicExpansion(topic, field, subTemplate);
            }

            matcher.appendReplacement(sb, subTemplate);
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * <p>Repeats the <tt>subtemplate</tt> for each value in the field <tt>fieldName</tt> in the query object <tt>topic</tt>.</p>
     * <p>This may seem counter-intuitive when multiple values should go into a single template instance, for example
     * for a multi word phrase query. For such cases, the query object field should hold a single value which is
     * a collection of the values that should go into the template instance. Note that this case is currently not
     * used, however, and might not be properly implemented right now, causing errors on try.</p>
     * <p>The good thing on the copy strategy is that it also allows for 0 values in which case the whole subtemplate
     * will be omitted from the final query.</p>
     * <p>Does not actually replace the templates with the target values but just adds numbered references to them for
     * use in {@link MapQueryDecorator#map(java.util.Map)}.</p>
     *
     * @param topic       The query object that must have a field named <tt>fieldName</tt> to get the replacement values from,
     * @param fieldName   The name of the field to get values for from <tt>topic</tt>.
     * @param subtemplate The JSON template in which the replacement value should be injected.
     * @return The subtemplates with numbered references to the values in the query object field.
     */
    private String handleDynamicExpansion(T topic, String fieldName, String subtemplate) {
        Matcher matcher = DYNAMIC_TEMPLATE_PATTERN.matcher(subtemplate);

        // TODO obtain fieldName here and do not receive via parameter
        //matcher.find();
        // if it didn't find, there's no dynamic expansion, return subtemplate
        //String fieldName = matcher.group(1);

        int numExpansions = 0;

        // Uses reflection to get field value
        try {
            Class topicClass = Topic.class;
            Field field = topicClass.getField(fieldName);
            List<String> values = (List<String>) field.get(topic);
            numExpansions = values.size();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(e);
        }

        // Repeats the subtemplate k times, with k being the size of the field with expansions
        // Replace [fieldName] with fieldName#, # being a counter
        List<String> allTemplates = new ArrayList<>();
        for (int k = 0; k < numExpansions; k++) {
            String substitution = String.format("{{%s%d}}", fieldName, k);
            String replacedSubTemplate = matcher.replaceAll(substitution);
            allTemplates.add(replacedSubTemplate);
        }

        return Strings.join(allTemplates, FIELD_SEPARATOR);
    }
}
