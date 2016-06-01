// MPCS 54001 - Project 2
// CJ Hodnefield and Omar Latif

// Utility functions and supplementary classes

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

// Attributions: Taken from instructor solutions to Project0
final class Utils {
	private Utils() {}; // can't instantiate this static class

	// Chew on the provided arguments and build a map
	static Map<String, String> parseCmdlineFlags(String argv[]) {
		Map<String, String> flags = new HashMap<String, String>();
		for (String flag : argv) {
			
			// Command line flag for the server port
			if (flag.startsWith("--serverPort")) {
				String[] parts = flag.split("=");
				if (parts.length == 2) {
					flags.put(parts[0], parts[1]);
				}
			}
			
			// Command line flag for the SSL server port
			if (flag.startsWith("--sslServerPort")) {
				String[] parts = flag.split("=");
				if (parts.length == 2) {
					flags.put(parts[0], parts[1]);
				}
			}
		}
		return flags;
	}
}

// Request object generated from client request
class Request { 
	// Path and opcode variables
	private String path;
	private String method;
	private String protocol;
	private String version;
	private boolean keepAlive;

	// Constructor
	Request(ArrayList<ArrayList<String>> lines) {
		if (!lines.isEmpty() && lines.get(0).size() > 0) {
			this.method = lines.get(0).get(0);
			this.path = lines.get(0).get(1);
			String[] protocolAndVersion = lines.get(0).get(2).split("/");
			this.protocol = protocolAndVersion[0];
			this.version = protocolAndVersion[1];
		}
		String keepAliveStr = "";
		
		for (ArrayList<String> line : lines) {
			if (!line.isEmpty() && line.get(0).toLowerCase().contains("connection")) {
				keepAliveStr = line.get(1);
			}
		}

		if (keepAliveStr.equals("keep-alive")) {
			this.keepAlive = true;
		} else {
			this.keepAlive = false;
		}
	}

	// Methods
	// Accessor methods
	public String getMethod(){
		return this.method;
	}

	public String getPath(){
		return this.path; 
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public boolean keepAlive() {
		return this.keepAlive;
	}
	
	// For debugging
	public String toString() {
		String str = "===========================";
		str += "\nRequest toString:";
		str += "\nMethod: " + method;
		str += "\nPath: " + path;
		str += "\nProtocol: " + protocol;
		str += "\nVersion: " + version;
		str += "\nKeep-alive: " + keepAlive;
		str += "\n===========================";
		return str;
	}
}

// Response object used to send a response from server to client
class Response {
	// Path and opcode variables
	private String path;
	private String method;
	private String keepAlive;

	// Response content variables
	private String protocol = "HTTP/1.1";
	private int error;
	private String contentType;
	private byte[] file;

	// Date variables
	private String date;
	private SimpleDateFormat dateFormat;

	// Constructor
	public Response(String path, String method, boolean keepAlive) throws IOException {
		this.path = path;
		this.method = method;
		
		if (keepAlive)
			this.keepAlive = "keep-alive";
		else
			this.keepAlive = "close";
		
		this.contentType = "text/html";

		// Acquire the singleton as a local variable for ease of use
		RedirectMap redirects = RedirectMap.getInstance();

		/* The following logic is used to set the correct error value
		 * and to extract the requested file as a byte[] (if applicable)
		 * to be sent to the client's output stream
		 */
		// If the client sends a POST request, throw a 403
		if (!method.equalsIgnoreCase("HEAD") && !method.equalsIgnoreCase("GET")) {
			error = 403;

		// If the client asks for the redirect.defs file, throw a 404
		} else if (path.endsWith("redirect.defs")) {
			error = 404;

		// If the client asks for a location that has been moved, throw a 301,
		// search the map in the RedirectMap singleton for the key, and serve
		// the client the value as the new location
		} else if (redirects.getMap().containsKey(this.path)) {
			error = 301;
			this.path = redirects.getMap().get(this.path);

		// Otherwise, prepare the response object with the 200 header
		// and the file info
		} else {
			this.path = "www" + this.path;
			try {
				// Read the file into a byte stream
				// http://stackoverflow.com/questions/858980/file-to-byte-in-java
				Path nioPath = Paths.get(this.path);
				this.file = Files.readAllBytes(nioPath);
				this.contentType = interpretContentType();
				this.error = 200;

			} catch (FileNotFoundException e) {
				System.out.println("The requested file was not found: " + e);
				error = 404;
			} catch (IOException e) {
				System.out.println("Exception: " + e);
				error = 404;
			}
		}
		
		// Format the date appropriately
		// http://stackoverflow.com/questions/7707555/getting-date-in-http-format-in-java
		this.dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
		this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		this.date = dateFormat.format(Calendar.getInstance().getTime());
	}
	
	// Methods
	// Getter methods
	public String getMethod() {
		return method;
	}

	public byte[] getFile() {
		return file;
	}

	public String getContentType() {
		return contentType;
	}

	public int getError() {
		return error;
	}

	// Interprets the end of the path variable to figure out what MIME type to use
	public String interpretContentType() {
		if (path.endsWith(".html"))
			this.contentType = "text/html";
		else if (path.endsWith(".txt"))
			this.contentType = "text/plain";
		else if (path.endsWith(".pdf"))
			this.contentType = "application/pdf";
		else if (path.endsWith(".png"))
			this.contentType = "image/png";
		else if (path.endsWith(".jpg") || path.endsWith(".jpeg"))
			this.contentType = "image/jpeg";

		return contentType;
	}

	// Returns the header response as a string, which
	// can then be sent to the client's output stream
	public String toString() {
		String str = "";
		str += protocol + " " + error;

		// Format the header for a 200
		if (error == 200) {
			str += " OK\r\n";
		
		// Format the header for a redirect
		// https://en.wikipedia.org/wiki/HTTP_301#Example
		} else if (error == 301) {
			str += " Moved Permanently\r\n";
			str += "Location: " + this.path + "\r\n";
		} else {
			str += "\r\n";
		}

		str += "Content-Type: " + contentType + "\r\n";
		str += "Date: " + date + "\r\n";
		str += "Connection: " + keepAlive + "\r\n";
		str += "\r\n";
		
		return str;
	}
}

// Maps permanently moved locations for redirects
class RedirectMap {
	private static RedirectMap instance = null;
	private HashMap<String, String> redirectMap;

	protected RedirectMap() {
		String path = "www/redirect.defs";
		redirectMap = new HashMap<String, String>();

		try {
			BufferedReader input =  new BufferedReader(new FileReader(path));
			String line = null;
            while ((line = input.readLine()) != null) {
            	String[] split = line.split("\\s+");
            	redirectMap.put(split[0], split[1]);
            }
            input.close();
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	public static RedirectMap getInstance() {
		if (instance == null) {
			synchronized(RedirectMap.class) {
                if (instance == null) {
                    instance = new RedirectMap();
                }
            }
		}
		return instance;
	}

	public HashMap<String, String> getMap() {
		return redirectMap;
	}
}