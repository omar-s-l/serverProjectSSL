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

	public SimpleWebServer(int serverPort) {
		this.serverPort = serverPort;
	}

	// Binds the server to the specified port
	public void start() throws IOException {
		socket = new ServerSocket(serverPort);
		System.out.println("Server bound and listening to port " + serverPort);
	}

	// Accepts the client socket and starts the I/O streams
	public boolean acceptFromClient() throws IOException {
		Socket clientSocket;
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

	public static void main(String[] args) throws IOException {
		Map<String, String> flags = Utils.parseCmdlineFlags(args);
		if (!flags.containsKey("--serverPort")) {
			System.out.println("usage: Server --serverPort=12345");
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