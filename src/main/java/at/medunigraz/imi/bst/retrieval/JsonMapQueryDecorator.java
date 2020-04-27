package at.medunigraz.imi.bst.retrieval;

import de.julielab.ir.model.QueryDescription;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * <p>This class handles the following template elements:
 * <ul>
 * <li>TODO</li>
 * </ul>
 * </p>
 *
 * @param <T>
 */
public abstract class JsonMapQueryDecorator<T extends QueryDescription> extends QueryDecorator<T> {
    private static final Pattern VALUE_PATTERN = Pattern.compile("(\")?\\$\\{(((QUOTE|NOQUOTE|JSONARRAY|CONCAT)\\s+)*)(\\w+)(\\[(\\$INDEX|[0-9]+)])?}(\")?", Pattern.CASE_INSENSITIVE);
    private final static Logger log = LoggerFactory.getLogger(JsonMapQueryDecorator.class);

    public JsonMapQueryDecorator(Query<T> decoratedQuery) {
        super(decoratedQuery);
    }

    public String map(String jsonQuery, Map<String, ?> keymap, int index) {
        if (jsonQuery == null)
            throw new IllegalStateException("Cannot find a template. When using the decorator chaining API, make sure to first set the parameters and then the template. The template loading decorator must be a delegate of this decorater that accesses the template.");

        StringBuffer sb = new StringBuffer();
        Matcher m = VALUE_PATTERN.matcher(jsonQuery);
        while (m.find()) {
            // QUOTE, JSONLIST et
            boolean hasBeginQuote = m.group(1) != null;
            boolean hasEndQuote = m.group(8) != null;
            Set<Modifier> modifiers = parseModifiers(m.group(2), m);
            String indexGroup = m.group(7);
            boolean useIndex = indexGroup != null;
            boolean indexWasGiven = indexGroup != null && indexGroup.matches("[0-9]+");
            int givenIndex = useIndex && indexWasGiven ? Integer.parseInt(indexGroup) : -1;
            int effectiveIndex = givenIndex >= 0 ? givenIndex : index;
            String field = m.group(5);
            Object fieldValue = keymap.get(field);
            if (fieldValue != null) {
                Object replacement = getReplacementValue(field, fieldValue, modifiers, m, useIndex, indexWasGiven, effectiveIndex);
                // We have got our value, do the actual replacement.
                if (replacement == null)
                    throw new IllegalStateException("Neither was a replacement value found nor was an error detected previously.");
                // We need to take care of the quoting. The template expression must always be a string to create a valid JSON document. However, the template expression
                // can also just be a part of an actual JSON string. The latter is detected by checking if the expression was surrounded by quotes to begin with (if yes it means
                // that the whole expression is the string, thus the quotes belong to the expression). We also obey fixed (NO)QUOTE modifiers.
                if ((hasBeginQuote && hasEndQuote && !modifiers.contains(Modifier.NOQUOTE) && replacement instanceof CharSequence) || modifiers.contains(Modifier.QUOTE))
                    replacement = "\"" + replacement + "\"";
                else if (!modifiers.contains(Modifier.NOQUOTE) && hasBeginQuote && !hasEndQuote)
                    replacement = "\"" + replacement;
                else if (!modifiers.contains(Modifier.NOQUOTE) && !hasBeginQuote && hasEndQuote)
                    replacement = replacement + "\"";
                m.appendReplacement(sb, String.valueOf(replacement));
            } else {
                throwTopicFieldDoesNotExist(m, field);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    protected void throwTopicFieldDoesNotExist(Matcher m, String field) {
        String msg = String.format("A template contains the topic field reference '%s'. However, no value for such a field was provided. The full template expression is '%s'. Perhaps the @QueryDescriptionAttribute annotation is missing?", field, m.group());
        log.warn(msg);
        throw new IllegalArgumentException(msg);
    }

    private Set<Modifier> parseModifiers(String modifierGroup, Matcher m) {
        Set<Modifier> modifiers = new HashSet<>();

        for (String modifierString : modifierGroup.split("\\s+")) {
            if (modifierString.isBlank())
                continue;
            try {
                modifiers.add(Modifier.valueOf(modifierString.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.error("Illegal modifier in JSON template expression {}: {}", m.group(), modifierString);
            }
        }

        if (modifiers.contains(Modifier.QUOTE) && modifiers.contains(Modifier.NOQUOTE)) {
            String msg = String.format("The template expression %s contains the modifiers QUOTE and NOQUOTE. However, only one of both is allowed.", m.group());
            throw new IllegalArgumentException(msg);
        }

        return modifiers;
    }

    private Stream<?> getElementStream(Object fieldValue) {
        Stream<?> stream;
        if (fieldValue instanceof Iterable)
            stream = StreamSupport.stream(((Iterable<?>) fieldValue).spliterator(), false);
        else if (fieldValue.getClass().isArray())
            stream = IntStream.range(0, Array.getLength(fieldValue)).mapToObj(i -> Array.get(fieldValue, i));
        else
            throw new IllegalArgumentException("Unexpected exception: The given field value is neither an Iterable nor an Array.");
        return stream;
    }

    @Nullable
    private Object getReplacementValue(String field, Object fieldValue, Set<Modifier> modifiers, Matcher m, boolean useIndex, boolean indexWasGiven, int effectiveIndex) {
        Object replacement;
        boolean isIterable = fieldValue instanceof Iterable;
        boolean isArray = fieldValue.getClass().isArray();
        if (!useIndex && !isIterable && !isArray) {
            replacement = fieldValue;
        } else if (!useIndex && (isIterable || isArray)) {
            if (modifiers.contains(Modifier.CONCAT)) {
                replacement = getElementStream(fieldValue).map(String::valueOf).collect(Collectors.joining(" "));
            } else if (modifiers.contains(Modifier.JSONARRAY)) {
                replacement = isArray ? new JSONArray(fieldValue) : new JSONArray(StreamSupport.stream(((Iterable<?>) fieldValue).spliterator(), false).collect(Collectors.toList()));
            } else {
                String msg = String.format("The template expression '%s' refers to the Iterable or Array field %s. However, no template modifier is given as to how the array should be retreated. Please specify one of JSONARRAY or CONCAT.", m.group(), field);
                log.error(msg);
                throw new IllegalArgumentException(msg);
            }
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
            replacement = getCollectionElement(fieldValue, effectiveIndex, field, m);
        }
        return replacement;
    }

    protected Object getCollectionElement(Object collection, int index, String fieldName, Matcher templateExpressionMatcher) {
        Object replacement = null;
        try {
            if (collection.getClass().isArray()) {
                replacement = Array.get(collection, index);
            } else {
                Iterable<?> c = (Iterable<?>) collection;
                // lists are quick and easy
                if (c instanceof List) {
                    replacement = ((List<?>) c).get(index);
                } else {
                    // for non-random-access collection we need to iterate to the sought element
                    Iterator<?> it = c.iterator();
                    int i = 0;
                    while (it.hasNext() && i <= index) {
                        Object o = it.next();
                        if (i == index)
                            replacement = o;
                        i++;
                    }
                }
            }
        } catch (ClassCastException e) {
            log.error("The template expression {} refers to a Collection or Array. However, the value of the field '{}' is neither.", templateExpressionMatcher.group(), fieldName);
            throw e;
        } catch (ArrayIndexOutOfBoundsException e) {
            String msg = String.format("The template expression %s refers to the index %d. However, the value of the field '%s' has only %d elements.", templateExpressionMatcher.group(), index, fieldName, getCollectionSize(collection, fieldName, templateExpressionMatcher));
            log.error(msg);
            throw new ArrayIndexOutOfBoundsException(msg);
        }
        return replacement;
    }

    protected int getCollectionSize(Object collection, String fieldName, Matcher templateExpressionMatcher) {
        if (collection.getClass().isArray())
            return Array.getLength(collection);
        else if (collection instanceof Collection)
            return ((Collection<?>) collection).size();
        else {
            String msg = String.format("The template expression %s refers to a Collection or Array. However, the value of the field '%s' is neither.", templateExpressionMatcher.group(), fieldName);
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    protected String map(Map<String, ?> keymap, int index) {
        return map(getJSONQuery(), keymap, index);
    }

    private enum Modifier {JSONARRAY, QUOTE, NOQUOTE, CONCAT}

}
