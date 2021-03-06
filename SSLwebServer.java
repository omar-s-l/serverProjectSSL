import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.net.*;
import javax.net.ssl.*;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.security.*;


// Attributions: Several of these methods were taken from instructor solutions to Project0
// Cite: http://stackoverflow.com/questions/12370351/setting-the-certificate-used-by-a-java-ssl-serversocket
class SSLwebServer {
	private final int serverPort;
	private ServerSocket socket;
	private DataOutputStream toClientStream;
	private BufferedReader fromClientStream;
	private SSLContext sslContext; 

	public SSLwebServer(int serverPort) throws Exception  {
		this.serverPort = serverPort;
		
		// Hardcoding for TA grading use
		System.out.println("Construct..."); 
		char [] passW = "frisbee".toCharArray();
		FileInputStream jksKey = new FileInputStream("server.jks"); 
		System.out.println("Reading key...");

		KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
		keyStore.load(jksKey, passW);
		System.out.println("KeyStore..."); 

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(keyStore, passW);

		KeyManager keyManagers[] = keyManagerFactory.getKeyManagers();
		System.out.println("KeyManager....."); 

		this.sslContext = SSLContext.getInstance("SSL");
		this.sslContext.init(keyManagers, null, null);
		System.out.println("SSLContext....."); 
		// System.setProperties("javax.net.ssl.keyStore", "server.jks"); 
		// System.setProperties("javax.net.ssl.keyStorePassword", "frisbee"); 
	}

	// Binds the server to the specified port
	public void start() throws Exception {
		System.out.println("Start()....."); 
		SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
		socket = socketFactory.createServerSocket(this.serverPort);
		System.out.println("Server bound and listening to port " + this.serverPort);
	}

	// Accepts the client socket and starts the I/O streams
	public boolean acceptFromClient() throws IOException {
		Socket clientSocket;
		// SSLSocket clientSocket; 
		try {
			clientSocket = socket.accept();
		} catch (SecurityException e) {
			System.out.println("Security manager intervened; your config is wrong. " + e);
			return false;
		} catch (IllegalArgumentException e) {
			System.out.println("Probably an invalid port number. " + e);
			return false;
		}

		// Create the I/O streams
		toClientStream = new DataOutputStream(clientSocket.getOutputStream());
		fromClientStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		return true;
	}

	public Request processGetRequest() throws IOException {
		ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
		String str = ".";
		while (!str.equals("")) {
			str = fromClientStream.readLine();

			// Separate parts of the response by white space
			String[] split = str.split("\\s+");
			ArrayList<String> wordsInLine = new ArrayList<String>();
			for (String s : split) {
				wordsInLine.add(s);
			}

			lines.add(wordsInLine);
		}

		// Print out all of the lines (for testing)
		for (ArrayList<String> s : lines) {
			System.out.println(s);
		}

		// Constructing the request object 
		Request request = new Request(lines); 

		return request; 
	}

	public static void main(String[] args) throws Exception {
		Map<String, String> flags = Utils.parseCmdlineFlags(args);
		if (!flags.containsKey("--sslServerPort")) {
			System.out.println("usage: Server --sslServerPort=12345");
			System.exit(-1);
		}
		if (!flags.containsKey("--serverPort")) {
			System.out.println("usage: Server --serverPort=12345");
			System.exit(-1);
		}

		int sslServerPort = -1;
		int serverPort = -1;
		
		try {
			sslServerPort = Integer.parseInt(flags.get("--sslServerPort"));
			System.out.println("sslServerPort: " + sslServerPort);
			serverPort = Integer.parseInt(flags.get("--serverPort"));
			System.out.println("serverPort: " + serverPort);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number! Must be an integer.");
			System.exit(-1);
		}

		// Passing in both ports to the SSLwebServer
		SSLwebServer webServer = new SSLwebServer(sslServerPort);

		
		// Try to start the web server
		try {
			webServer.start();
			
			// Loop so that the server stay up
			while (true) {
				if (webServer.acceptFromClient()) {

					// Process the request and create a Request object
					Request request = webServer.processGetRequest();
					
					// Use the request path to create a Response object
					Response response = new Response(request.getPath(), request.getMethod());

					// Print out the response (for debugging)
					System.out.println(response);

					// Write the response and the file to the client
					webServer.toClientStream.writeBytes(response.toString());
					if (response.getMethod().equalsIgnoreCase("GET") && response.getError() == 200)
						webServer.toClientStream.write(response.getFile(), 0, response.getFile().length);
					
					// Close the I/O streams
					webServer.fromClientStream.close();
					webServer.toClientStream.close();

				} else {
					System.out.println("Error accepting client communication.");
				}
			}
		} catch (IOException e) {
			System.out.println("Error communicating with client. Aborting. Details: " + e);
		}
	}
}