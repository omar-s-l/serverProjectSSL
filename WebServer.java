import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

// Attributions: Several of these methods were taken from instructor solutions to Project0
class WebServer implements Runnable {
	protected String serverName;
	protected final int serverPort;
	protected ServerSocket socket;
	protected DataOutputStream toClientStream;
	protected BufferedReader fromClientStream;
	protected boolean keepConnectionAlive = true;

	public WebServer(int serverPort) {
		this.serverName = "Server:" + serverPort;
		this.serverPort = serverPort;
	}
	
	public void setConnectionStatus(boolean keepConnectionAlive) {
		this.keepConnectionAlive = keepConnectionAlive;
	}

	// Binds the server to the specified port
	public void start() throws IOException {
		socket = new ServerSocket(serverPort);
		System.out.println(serverName + " bound and listening to port " + serverPort);
	}

	// Accepts the client socket and starts the I/O streams
	// Improved with some code taken from instructor solution
	public Socket acceptFromClient() throws IOException {
		Socket clientSocket;
		try {
			clientSocket = socket.accept();
		} catch (SecurityException e) {
			System.out.println("The security manager intervened; your config is very wrong. " + e);
			return null;
		} catch (IllegalArgumentException e) {
			System.out.println("Probably an invalid port number. " + e);
			return null;
		} catch (IOException e) {
			System.out.println("IOException in socket.accept()");
			return null;
		}

		// Create the I/O streams
		try {
			toClientStream = new DataOutputStream(clientSocket.getOutputStream());
			fromClientStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch (IOException e) {
			System.out.println("Exception creating the stream objects.");
		}
		
		return clientSocket;
	}

	public Request processRequest() throws IOException {
		ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
		String inputLine;
		do {
			inputLine = fromClientStream.readLine();
			ArrayList<String> wordsInLine = new ArrayList<String>();
			if (inputLine != null) {
				String[] split = inputLine.split("\\s+");
				for (String s : split) {
					wordsInLine.add(s);
				}
			}
			lines.add(wordsInLine);
		} while ((inputLine != null) && (inputLine.length() > 0));

		System.out.println("Request received from client");
		// Print out all of the lines (for testing)
		for (ArrayList<String> s : lines) {
			System.out.println(s);
		}

		// Constructing the request object 
		Request request = new Request(lines); 

		if (request.getMethod() != null) {
			System.out.println("Valid request from client...");
			System.out.println("Request processed...");
			return request;
		} else {
			System.out.println("Invalid request from client...");
			return null;
		}
	}

	public void run() {
		// Try to start the web server
		try {
			start();
			while (true) {
				System.out.println("while loop #1...");
				Socket clientSocket = acceptFromClient();
				
				if (clientSocket != null && clientSocket.isConnected()) {
					while (keepConnectionAlive) {
						System.out.println("while loop #2...");
						// Process the request and create a Request object
						Request request = processRequest();
						
						if (request != null) {
							// keep-alive or close
							setConnectionStatus(request.keepAlive());
							//setConnectionStatus(false);
							
							// Use the request path to create a Response object
							Response response = new Response(request.getPath(), request.getMethod());
		
							// Print out the response (for debugging)s
							System.out.println("===========================");
							System.out.println("Response toString() from SimpleWebServer:");
							System.out.println(response);
							System.out.println("===========================");
							
							// Write the response and the file to the client
							toClientStream.writeBytes(response.toString());
							if (response.getMethod().equalsIgnoreCase("GET") && response.getError() == 200)
								toClientStream.write(response.getFile(), 0, response.getFile().length);
							System.out.println("Response sent to client...");
						} else {
							setConnectionStatus(false);
						}
					}
					// Close the I/O streams
					fromClientStream.close();
					toClientStream.close();
					clientSocket.close();
					setConnectionStatus(true);
					
				} else {
					System.out.println("Error accepting client communication.");
				}
			}
		} catch (IOException e) {
			System.out.println("Error communicating with client. Aborting. Details: " + e);
			System.out.println("Restart the server");
		}
	}
	
	public static void main(String[] args) throws Exception {
		Map<String, String> flags = Utils.parseCmdlineFlags(args);
		if (!flags.containsKey("--serverPort")) {
			System.out.println("usage: Server --serverPort=12345");
			System.exit(-1);
		}
		if (!flags.containsKey("--sslServerPort")) {
			System.out.println("usage: Server --sslServerPort=12345");
			System.exit(-1);
		}

		int serverPort = -1;
		int sslServerPort = -1;
		
		try {
			serverPort = Integer.parseInt(flags.get("--serverPort"));
			System.out.println("serverPort: " + serverPort);
			sslServerPort = Integer.parseInt(flags.get("--sslServerPort"));
			System.out.println("sslServerPort: " + sslServerPort);
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number! Must be an integer.");
			System.exit(-1);
		}
		
		Thread thread1 = new Thread(new WebServer(serverPort));
		Thread thread2 = new Thread(new WebServerSSL(sslServerPort));
		thread1.start();
		thread2.start();
	}
}