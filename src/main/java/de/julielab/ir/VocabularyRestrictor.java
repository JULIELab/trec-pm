package de.julielab.ir;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.wcohen.ss.BasicStringWrapper;
import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.TFIDF;
import com.wcohen.ss.api.StringWrapper;
import com.wcohen.ss.api.Token;
import com.wcohen.ss.tokens.BasicSourcedToken;
import com.wcohen.ss.tokens.BasicToken;
import com.wcohen.ss.tokens.SimpleTokenizer;
import de.julielab.ir.cache.CacheService;
import de.julielab.java.utilities.FileUtilities;
import org.apache.commons.collections4.multiset.HashMultiSet;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.mapdb.DB;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.springframework.cache.CacheManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VocabularyRestrictor {
    private static VocabularyRestrictor instance;
    private final HTreeMap<String, Set<String>> fieldVocabularies;
    private final DB filedb;
    private Set<String> stopwords;
    private final File cacheFile;

    private VocabularyRestrictor() {
        cacheFile = Path.of("cache", "fieldVocabularies.db").toFile();
        filedb = CacheService.getInstance().getFiledb(cacheFile);
        fieldVocabularies = CacheService.getInstance().getCache(cacheFile, "FieldVocabularies", Serializer.STRING, Serializer.JAVA);
        try {
            stopwords = FileUtilities.getReaderFromFile(new File("resources/stopwords.txt")).lines().collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static VocabularyRestrictor getInstance() {
        if (instance == null) {
            instance = new VocabularyRestrictor();
        }
        return instance;
    }

    public Set<String> calculateVocabulary(String field, Stream<String> fieldContents, Restriction restriction, int cutoff) {
        Set<String> vocabulary = fieldVocabularies.get(field);
        if (vocabulary == null) {
            switch (restriction) {
                case TFIDF:
                    vocabulary = calculateTfIdfVocabulary(fieldContents, cutoff);
                    break;
                case FREQUENCY:
                    vocabulary = calculateFrequencyVocabulary(fieldContents, cutoff);
                    break;
            }
            if(!CacheService.getInstance().isDbReadOnly(cacheFile)) {
                fieldVocabularies.put(field, vocabulary);
                filedb.commit();
            }
        }
        return vocabulary;
    }

    private Set<String> calculateFrequencyVocabulary(Stream<String> fieldContents, int cutoff) {
        // This is the tokenizer used with the default constructor of an TFIDF instance
        final SimpleTokenizer tokenizer = SimpleTokenizer.DEFAULT_TOKENIZER;
        final Map<String, List<String>> sets = fieldContents.map(tokenizer::tokenize).flatMap(Stream::of).map(Token::getValue).collect(Collectors.groupingBy(Function.identity()));
        final ArrayList<Map.Entry<String, List<String>>> entryList = new ArrayList<>(sets.entrySet());
        entryList.sort((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()));
        Set<String> vocabulary = new HashSet<>(cutoff);
        for (int i = 0; i < entryList.size() && vocabulary.size() < cutoff; i++) {

            final String token = entryList.get(i).getKey();
            if (acceptVocabularyToken(token))
                vocabulary.add(token);
        }
        return vocabulary;
    }

    private Set<String> calculateTfIdfVocabulary(Stream<String> fieldContents, int cutoff) {
        final List<String> fieldContentList = fieldContents.collect(Collectors.toList());
        final TFIDF tfidf = new TFIDF();
        final Stream<BasicStringWrapper> stringWrapperStream = fieldContentList.stream().map(BasicStringWrapper::new);
        tfidf.train(new BasicStringWrapperIterator(stringWrapperStream.iterator()));
        List<Pair<Token, Double>> weightedTokens = new ArrayList<>();

        for (String fieldValue : fieldContentList) {
            tfidf.prepare(fieldValue);
            for (Token t : tfidf.getTokens())
                weightedTokens.add(new ImmutablePair<>(t, tfidf.getWeight(t)));
        }
        weightedTokens.sort((t1, t2) -> Double.compare(t2.getRight(), t1.getRight()));
        Set<String> vocabulary = new HashSet<>(cutoff);
        for (int i = 0; i < weightedTokens.size() && vocabulary.size() < cutoff; i++) {
            final Pair<Token, Double> weightedToken = weightedTokens.get(i);
            final Token token = weightedToken.getLeft();
            if (!vocabulary.contains(token.getValue()) && acceptVocabularyToken(token.getValue()))
                vocabulary.add(token.getValue());
        }
        return vocabulary;
    }

    private boolean acceptVocabularyToken(String token) {
        if (token.isBlank())
            return false;
        if (token.length() == 1)
            return false;
        if (stopwords.contains(token))
            return false;
        if (token.matches("[0-9]+"))
            return false;
        return true;
    }

    public enum Restriction {TFIDF, FREQUENCY}

}
