package de.julielab.ir.paramopt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static spark.Spark.port;
import static spark.Spark.post;

public class HttpParamOptServer {
    public static final String GET_CONFIG_SCORE = "get_configuration_score";
    public static final String INSTANCE = "instance";
    public static final String INSTANCE_INFO = "instance_info";
    public static final String CUTOFF_TIME = "cutoff_time";
    public static final String CUTOFF_LENGTH = "cutoff_length";
    public static final String SEED = "seed";
    private static final Logger log = LogManager.getLogger();


    public HttpParamOptServer(int port) {
        port(port);
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            log.error("Usage: " + HttpParamOptServer.class.getSimpleName() + " <port>");
            System.exit(1);
        }

        int port = Integer.valueOf(args[0]);

        HttpParamOptServer server = new HttpParamOptServer(port);
        server.startServer();
    }

    public void startServer() {
        post("/" + GET_CONFIG_SCORE, new EvaluateConfigurationRoute());

        log.info("Server is ready for requests.");
    }

}
