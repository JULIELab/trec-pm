package de.julielab.ir;

import com.wcohen.ss.BasicStringWrapper;
import com.wcohen.ss.BasicStringWrapperIterator;
import com.wcohen.ss.TFIDF;
import org.apache.commons.collections.map.HashedMap;

import java.util.Map;
import java.util.stream.Stream;

public class TfIdfManager {

    private static TfIdfManager instance;
    private Map<String, TFIDF> tfidfModels;

    private TfIdfManager() {
        this.tfidfModels = new HashedMap();
    }

    public static TfIdfManager getInstance() {
        if (instance == null)
            instance = new TfIdfManager();
        return instance;
    }

    /**
     * <p>Trains a TFIDF model on the given text, stores it in the internal cache and returns it.</p>
     * <p>This method will raise an exception if there is already a model in the cache with the given ID.</p>
     *
     * @param id           The ID of the learned TFIDF model.
     * @param trainingData The text to learn the TFIDF weights from.
     * @return The learned TFIDF model.
     * @throws IllegalArgumentException If there is already a cache entry with the given ID.
     * @see #trainAndSetTfIdf(String, Stream)
     */
    public TFIDF trainTfIdf(String id, Stream<String> trainingData) {
        if (tfidfModels.containsKey(id))
            throw new IllegalArgumentException("There is already a TFIDF model for ID " + id);
        return trainAndSetTfIdf(id, trainingData);
    }

    /**
     * <p>Trains a TFIDF model on the given text, stores it in the internal cache and returns it.</p>
     * <p>If the internal cache does already have an entry for the passed ID, it will be overridden.</p>
     *
     * @param id           The ID of the learned TFIDF model.
     * @param trainingData The text to learn the TFIDF weights from.
     * @return The learned TFIDF model.
     */
    public TFIDF trainAndSetTfIdf(String id, Stream<String> trainingData) {
        final Stream<BasicStringWrapper> stringWrapperStream = trainingData.map(BasicStringWrapper::new);
        TFIDF tfidf = new TFIDF();
        tfidf.train(new BasicStringWrapperIterator(stringWrapperStream.iterator()));
        tfidfModels.put(id, tfidf);
        return tfidf;
    }

    public TFIDF getTrainedTfIdf(String id) {
        final TFIDF tfidf = tfidfModels.get(id);
        if (tfidf == null)
            throw new IllegalArgumentException("There is no TFIDF model training with ID " + id);
        return tfidf;
    }
}
