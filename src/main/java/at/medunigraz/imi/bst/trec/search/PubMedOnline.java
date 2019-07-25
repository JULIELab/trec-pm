package at.medunigraz.imi.bst.trec.search;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.model.Result;
import de.julielab.xml.JulieXMLConstants;
import de.julielab.xml.JulieXMLTools;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class PubMedOnline implements SearchEngine {
    public static final String EUTILS_URL = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
    public static final String SORT = "relevance";
    public static final String FIXED_PARAMS = "&retmax=1000&sort=" + SORT;
    public static final String EUTILS_SEARCH_FMT = "esearch.fcgi?db=%s&term=%s" + FIXED_PARAMS;
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger();
    private final BlockingQueue<Object> apiTicketQueue = new ArrayBlockingQueue<>(1);
    private ApiTicketGenerator ticketGenerator;

    public static void main(String args[]) {
        final PubMedOnline pmo = new PubMedOnline();
        final JSONObject q = new JSONObject();
        q.put("query", "mtor");
        pmo.query(q);
    }

    @Override
    public List<Result> query(JSONObject jsonQuery) {
        List<Result> results = new ArrayList<>(TrecConfig.SIZE);
        String query = jsonQuery.getString("query");
        query = query.replaceAll("\\s+", "+");

        if (ticketGenerator == null) {
            ticketGenerator = new ApiTicketGenerator();
            ticketGenerator.start();
        }

        try {
            // We do not really care about the returned object itself. We just want the program to stop until a ticket
            // object becomes available.
            log.trace("Awaiting PubMed API ticket");
            apiTicketQueue.take();
            log.trace("Obtained PubMed API ticket");
            final URL url = new URL(EUTILS_URL + String.format(EUTILS_SEARCH_FMT, "pubmed", query));
            log.debug("Querying PubMed with URL {}", url);
            final byte[] bytes = IOUtils.toByteArray(url);
            log.trace("Received response from PubMed, parsing it");
            final Map<String, String> idsField = JulieXMLTools.createField(JulieXMLConstants.NAME, "id", JulieXMLConstants.XPATH, ".");
            final Iterator<Map<String, Object>> it = JulieXMLTools.constructRowIterator(bytes, 0, "/eSearchResult/IdList/Id", Arrays.asList(idsField), "none");
            float rank = 1;
            while (it.hasNext()) {
                Map<String, Object> row = it.next();
                final String id = (String) row.get("id");
                final Result result = new Result(id, 1 / rank++);
                results.add(result);
            }
            log.debug("Created {} results from PubMed response", results.size());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return results;
    }

    private class ApiTicketGenerator extends Thread {
        @Override
        public void run() {
            // TODO using the history server would help a lot with repeated evaluations here! See URL given in the log message.
            log.debug("Starting PubMed API ticket generator. This thread creates at most 3 tickets per second as to not overstep this exact limit allowed by the NCBI EUtils without requireing an API key (see https://www.ncbi.nlm.nih.gov/books/NBK25497/).");
            try {
                boolean ticketWasTaken = true;
                while (ticketWasTaken) {
                    ticketWasTaken = apiTicketQueue.offer(new Object(), 5, TimeUnit.SECONDS);
                    final int size = apiTicketQueue.size();
                    Thread.sleep((size+1) * 360);
                }
                log.debug("No ticket was requested for 5 seconds. Assuming evaluation has finished. Terminating ticket generation thread.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
