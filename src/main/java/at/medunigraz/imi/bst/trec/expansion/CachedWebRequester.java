package at.medunigraz.imi.bst.trec.expansion;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.*;
import java.util.HashMap;

public class CachedWebRequester {

    private File cacheFile;

    private HashMap<String, String> cache = new HashMap<>();

    public CachedWebRequester(String filename) {
        this.cacheFile = new File(filename);

        if (cacheFile.exists()) {
            try {
                cache = load(cacheFile);
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Make a web request to a given url and cache results.
     *
     * @param url
     * @return
     */
    public String get(String url) {
        if (!cache.containsKey(url)) {
            String data = getResource(url);
            put(url, data);
        }
        return cache.get(url);
    }

    /**
     * Make a web request to a given restricted url and cache results.
     *
     * @param url
     * @param bearer The bearer token.
     * @return
     */
    public String get(String url, String bearer) {
        if (!cache.containsKey(url)) {
            String data = getRestrictedResource(url, bearer);
            put(url, data);
        }
        return cache.get(url);
    }

    private HashMap<String, String> load(File file) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
        HashMap<String, String> ret = (HashMap) ois.readObject();
        ois.close();
        return ret;
    }

    private void save(Object object, File file) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(object);
        oos.close();
    }

    private void put(String url, String data) {
        cache.put(url, data);

        // Try to persist cache on disk
        try {
            save(cache, cacheFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getRestrictedResource(String url, String bearer) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.get(url)
                    .header("authorization", String.format("Bearer %s", bearer))
                    .asJson();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

        return parseResponse(response);
    }

    private String getResource(String url) {
        HttpResponse<JsonNode> response = null;
        try {
            response = Unirest.get(url).asJson();
        } catch (UnirestException e) {
            throw new RuntimeException(e);
        }

        return parseResponse(response);
    }

    private String parseResponse(HttpResponse<JsonNode> response) {
        if (response.getStatus() == 401) {
            throw new RuntimeException("Unauthorized access to API. Check your keys in the file trec-pm.properties.");
        }

        if (response.getStatus() != 200) {
            throw new RuntimeException("Got status code " + response.getStatus() + " from API with body " + response.getBody());
        }

        return response.getBody().toString();
    }
}
