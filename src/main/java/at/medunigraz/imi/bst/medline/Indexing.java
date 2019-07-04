package at.medunigraz.imi.bst.medline;

import at.medunigraz.imi.bst.config.TrecConfig;
import at.medunigraz.imi.bst.trec.search.ElasticClientFactory;
import org.apache.commons.lang3.StringEscapeUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class Indexing {

    public static void main(String[] args) throws Exception {
        if (args.length == 2)
            indexAllCompressedMedline(args[0], args[1]);
        else
            System.out.println("2 args needed: (1) folder containing the COMPRESSED medline XMLs, " +
                                              "(2) initial doc to index, normally 1 ");
    }

    static void indexAllCompressedMedline(String dataFolderWithCompressedFiles, String firstArticleToIndex) throws Exception {

        Client client = ElasticClientFactory.getClient();

        long startTime = System.currentTimeMillis();
        System.out.println("START: " + new SimpleDateFormat("MMM dd,yyyy HH:mm").format(new Date(startTime)));

        for (int fileNumber = Integer.parseInt(firstArticleToIndex); fileNumber <= TrecConfig.MEDLINE_FILE_END; fileNumber++) {
            String articleXml = String.format(dataFolderWithCompressedFiles + "/" + TrecConfig.MEDLINE_FILE_PATTERN + ".gz", fileNumber);
            System.out.println(articleXml);
            List<PubMedArticle> pubMedArticles = XmlPubMedArticleSet.getPubMedArticlesFromGzippedXml(articleXml);
            indexArticlesBulk(client, pubMedArticles);
        }
        System.out.println("END: " + new SimpleDateFormat("MMM dd,yyyy HH:mm").format(new Date(System.currentTimeMillis())));

        System.out.println("TOTAL: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " seconds");

        client.close();

    }

    static long indexArticlesBulk(Client client, List<PubMedArticle> pubMedArticles) throws Exception{

        long startTime = System.currentTimeMillis();


        BulkRequestBuilder bulkRequest = client.prepareBulk();

        for (PubMedArticle article: pubMedArticles) {

            bulkRequest.add(client.prepareIndex(TrecConfig.ELASTIC_BA_INDEX, TrecConfig.ELASTIC_BA_MEDLINE_TYPE, article.pubMedId)
                    .setSource(jsonBuilder()
                            .startObject()
                            .field("pubmedId", StringEscapeUtils.escapeJson(article.pubMedId))
                            .field("title", StringEscapeUtils.escapeJson(article.docTitle))
                            .field("publicationDate", StringEscapeUtils.escapeJson(article.publicationMonth +
                                                             " " + StringEscapeUtils.escapeJson((article.publicationYear))))
                            .field("publicationYear", article.getPublicationYear())
                            .field("abstract", StringEscapeUtils.escapeJson(article.docAbstract))
                            .field("meshTags", article.meshTags)
                            .endObject()
                    )
            );
        }

        BulkResponse bulkResponse = bulkRequest.get();

        if (bulkResponse.hasFailures()) {
            System.out.println("Failures!!!!");
        }

        long indexingDuration = (System.currentTimeMillis() - startTime);

        System.out.println("INDEXING TIME BULK: " + indexingDuration/1000 + " secs - " + pubMedArticles.size() + " articles");

        return indexingDuration;

    }

    /*
    static void indexAllPlain(String dataFolderWithPlainFiles) throws Exception {

        long startTime = System.currentTimeMillis();
        System.out.println("START: " + startTime);

        for (int fileNumber = TrecConfig.MEDLINE_FILE_START; fileNumber <= TrecConfig.MEDLINE_FILE_END; fileNumber++) {
            String articleXml = String.format(dataFolderWithPlainFiles + "/" + TrecConfig.MEDLINE_FILE_PATTERN, fileNumber);
            System.out.println(articleXml);
            List<PubMedArticle> pubMedArticles = XmlPubMedArticleSet.getPubMedArticlesFromXml(articleXml);
            indexArticlesBulk(pubMedArticles);
        }
        System.out.println("END: " + System.currentTimeMillis());

        System.out.println("TOTAL: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime) + " seconds");
    }
    */
}
