package at.medunigraz.imi.bst.trec.utils;

import at.medunigraz.imi.bst.config.TrecConfig;
import de.julielab.costosys.configuration.ConfigReader;
import de.julielab.costosys.configuration.DBConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;

public class ConnectionUtils {
	/**
	 * Checks whether a given port is open on a server
	 * 
	 * @param hostname
	 * @param port
	 * @return
	 */
	public static boolean checkOpenPort(String hostname, int port) {		
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(hostname, port), 1000);
			return true;
		} catch (IOException ignored) {
			return false;
		}
	}

	public static boolean checkElasticOpenPort() {
		return ConnectionUtils.checkOpenPort(TrecConfig.ELASTIC_HOSTNAME[0], TrecConfig.ELASTIC_PORT[0]);
	}

	public static boolean checkPostgresOpenPort() throws FileNotFoundException, MalformedURLException {
		final ConfigReader configReader = new ConfigReader(new FileInputStream(TrecConfig.COSTOSYS_CONFIG));
		final DBConfig dbConfig = configReader.getDatabaseConfig();
        final String[] hostAndPort = dbConfig.getUrl().split("/")[2].split(":");
        return ConnectionUtils.checkOpenPort(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
	}
}
