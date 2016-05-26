import java.io.*;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

public class WebServerSSL extends WebServer {
	private SSLContext sslContext;
	
	public WebServerSSL(int serverPort) throws Exception  {
		super(serverPort);
		this.serverName = "SSL" + this.serverName;
		
		// Hardcoding for TA grading use
		System.out.println("Construct..."); 
		char [] passW = "frisbee".toCharArray();
		FileInputStream jksKey = new FileInputStream("server.jks"); 

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(jksKey, passW);
		System.out.println("KeyStore..."); 

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, passW);

		KeyManager keyManagers[] = keyManagerFactory.getKeyManagers();
		System.out.println("KeyManager....."); 

		this.sslContext = SSLContext.getInstance("SSL");
		this.sslContext.init(keyManagers, null, new SecureRandom());
		System.out.println("SSLContext.....");
	}
	
	// Binds the server to the specified port
	public void start() throws IOException {
		System.out.println("Start()....."); 
		SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
		socket = socketFactory.createServerSocket(this.serverPort);
		System.out.println(serverName + " bound and listening to port " + this.serverPort);
	}
}
