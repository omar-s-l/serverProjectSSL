import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;

// Attributions: Several of these methods were taken from instructor solutions to Project0
class SimpleWebServer {
	private final int serverPort;
	private ServerSocket socket;
	private DataOutputStream toClientStream;
	private BufferedReader fromClientStream;
	private boolean keepConnectionAlive = true;

	public SimpleWebServer(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public void setConnectionStatus(boolean keepConnectionAlive) {
		this.keepConnectionAlive = keepConnectionAlive;
	}

	// Binds the server to the specified port
	public void start() throws IOException {
		socket = new ServerSocket(serverPort);
		System.out.println("Server bound and listening to port " + serverPort);
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
			System.out.println("exception creating the stream objects.");
		}
		
		return clientSocket;
	}

	public Request processRequest() throws IOException {
		ArrayList<ArrayList<String>> lines = new ArrayList<ArrayList<String>>();
		String inputLine;;

		do {
			inputLine = fromClientStream.readLine();
			String[] split = inputLine.split("\\s+");
			ArrayList<String> wordsInLine = new ArrayList<String>();
			for (String s : split) {
				wordsInLine.add(s);
			}
			lines.add(wordsInLine);
			System.out.println(1);
		} while ((inputLine != null) && (inputLine.length() > 0));

		// Print out all of the lines (for testing)
		for (ArrayList<String> s : lines) {
			System.out.println(s);
		}

		// Constructing the request object 
		Request request = new Request(lines); 

		return request; 
	}

	public static void main(String[] args) throws IOException {
		Map<String, String> flags = Utils.parseCmdlineFlags(args);
		if (!flags.containsKey("--serverPort")) {
			System.out.println("usage: Server --sslServerPort=12345");
			System.exit(-1);
		}

		int serverPort = -1;
		try {
			serverPort = Integer.parseInt(flags.get("--serverPort"));
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number! Must be an integer.");
			System.exit(-1);
		}

		SimpleWebServer webServer = new SimpleWebServer(serverPort);
		
		// Try to start the web server
		try {
			webServer.start();
			while (true) {
				System.out.println("while loop #1");
				Socket clientSocket = webServer.acceptFromClient();
				if (clientSocket != null && clientSocket.isConnected()) {
					while (webServer.keepConnectionAlive) {
						System.out.println("while loop #2");
						// Process the request and create a Request object
						Request request = webServer.processRequest();
						System.out.println("GET request process");
						// keep-alive or close
						webServer.setConnectionStatus(request.keepAlive());
						
						// Use the request path to create a Response object
						Response response = new Response(request.getPath(), request.getMethod());
	
						// Print out the response (for debugging)
						System.out.println("===========================\nResponse toString:");
						System.out.println(response);
						System.out.println("===========================");
						
						// Write the response and the file to the client
						webServer.toClientStream.writeBytes(response.toString());
						if (response.getMethod().equalsIgnoreCase("GET") && response.getError() == 200)
							webServer.toClientStream.write(response.getFile(), 0, response.getFile().length);
						
					}
					// Close the I/O streams
					webServer.fromClientStream.close();
					webServer.toClientStream.close();
					clientSocket.close();
					webServer.setConnectionStatus(true);
					
				} else {
					System.out.println("Error accepting client communication.");
				}
			}
		} catch (IOException e) {
			System.out.println("Error communicating with client. Aborting. Details: " + e);
		}
	}
}