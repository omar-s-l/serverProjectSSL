import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.*;
import java.text.SimpleDateFormat;

class Response {
	// Path and opcode variables
	private String path;
	private String method;

	// Response content variables
	private String protocol = "HTTP/1.1";
	private int error;
	private String contentType;
	private byte[] file;

	// Date variables
	private String date;
	private SimpleDateFormat dateFormat;

	// Constructor
	public Response(String path, String method) throws IOException {
		this.path = path;
		this.method = method;
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
				System.out.println(1);
				this.file = Files.readAllBytes(nioPath);
				System.out.println(2);
				this.contentType = interpretContentType();
				System.out.println(3);
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
		str += "Date: " + date + "\r\n\r\n";
		
		return str;
	}
}