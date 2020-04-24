package at.medunigraz.imi.bst.retrieval;

import de.julielab.ir.model.QueryDescription;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>This class handles the following template elements:
 * <ul>
 * <li>"${topicField}" - the value of <tt>topicField></tt> injected as a String value - single values, collections flattened (all levels)</li>
 * <li>"${QUOTE topicField}" - value in quotes, even when the datatype is not a string</li>
 * <li>"${topicField[$INDEX]}" - must be subtemplate of FOR statement; INDEX is the passed index</li>
 * <li>"${topicField[10]}" - refers constantly to the 10th element of <tt>topicField</tt></li>
 * </ul>
 * </p>
 *
 * @param <T>
 */
public abstract class JsonMapQueryDecorator<T extends QueryDescription> extends QueryDecorator<T> {
    private static final Pattern VALUE_PATTERN = Pattern.compile("(\")\\$\\{(QUOTE\\s+)?(\\w+)(\\[(\\$INDEX|[0-9]+)\\])?}(\")", Pattern.CASE_INSENSITIVE);
    private final static Logger log = LoggerFactory.getLogger(JsonMapQueryDecorator.class);

    public JsonMapQueryDecorator(Query decoratedQuery) {
        super(decoratedQuery);
    }

    public String map(String jsonQuery, Map<String, ?> keymap, int index) {
        String ret = jsonQuery;
        if (jsonQuery == null)
            throw new IllegalStateException("Cannot find a template. When using the decorator chaining API, make sure to first set the parameters and then the template. The template loading decorator must be a delegate of this decorater that accesses the template.");

        StringBuffer sb = new StringBuffer();
        Matcher m = VALUE_PATTERN.matcher(jsonQuery);
        while (m.find()) {
            boolean hasBeginQuote = m.group(1) != null;
            boolean hasEndQuote = m.group(6) != null;
            boolean quote = m.group(2) != null;
            String indexGroup = m.group(5);
            boolean useIndex = indexGroup != null;
            boolean indexWasGiven = indexGroup != null && indexGroup.matches("[0-9]+");
            int givenIndex = useIndex && indexWasGiven ? Integer.parseInt(indexGroup) : -1;
            int effectiveIndex = givenIndex >= 0 ? givenIndex : index;
            String field = m.group(3);
            Object fieldValue = keymap.get(field);
            if (fieldValue != null) {
                Object replacement = getReplacementValue(field, fieldValue, m, indexWasGiven, effectiveIndex);
                // We have got our value, do the actual replacement.
                if (replacement == null)
                    throw new IllegalStateException("Neither was a replacement value found nor was an error detected previously.");
                if (replacement instanceof CharSequence || quote)
                    replacement = "\"" + replacement + "\"";
                m.appendReplacement(sb, String.valueOf(replacement));
            } else {
                log.warn("A template contains the topic field reference '{}'. However, the value for such a field was provided. The full template expressio is {}", field, m.group());
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    @Nullable
    private Object getReplacementValue(String field, Object fieldValue, Matcher m, boolean indexWasGiven, int effectiveIndex) {
        Object replacement = null;
        if (effectiveIndex < 0) {
            replacement = fieldValue;
        } else {
            // the field value is a collection or array and we should use a specific element
            if (effectiveIndex < 0) {
                String msg;
                if (!indexWasGiven)
                    msg = String.format("The template expression '%s' refers to a Collection. However, the current subtemplate is not embedded into a another template that would specify the index of the collection to get a value from. The containing template needs to contain a ${FOR INDEX in topicField REPEAT template} expression for this. Alternatively, you can just pass a constant number as an index to this template, e.g. ${topicField[2]}.", m.group());
                else
                    msg = String.format("The template expression '%s' specifies a constant index. However, this index is required to be >= 0.", m.group());
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
            try {
                if (fieldValue.getClass().isArray()) {
                    replacement = Array.get(fieldValue, effectiveIndex);
                } else {
                    Collection<?> c = (Collection) fieldValue;
                    // lists are quick and easy
                    if (c instanceof List) {
                        replacement = ((List) c).get(effectiveIndex);
                    } else {
                        // for non-random-access collection we need to iterate to the sought element
                        Iterator<?> it = c.iterator();
                        int i = 0;
                        while (it.hasNext() && i < effectiveIndex) {
                            Object o = it.next();
                            if (i == effectiveIndex)
                                replacement = o;
                            i++;
                        }
                    }
                }
            } catch (ClassCastException e) {
                log.error("The template expression {} refers to a Collection or Array. However, the value of the field '{}' is neither.", m.group(), field);
                throw e;
            }
        }
        return replacement;
    }

    protected void map(Map<String, ?> keymap, int index) {
        setJSONQuery(map(getJSONQuery(), keymap, index));
    }

}
