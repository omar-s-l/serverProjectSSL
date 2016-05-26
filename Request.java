import java.util.ArrayList;

class Request { 
	// Path and opcode variables
	private String path;
	private String method;
	private String protocol;
	private String version;
	private boolean keepAlive;

	// Constructor
	Request(ArrayList<ArrayList<String>> lines) {
		System.out.println("lines: " + lines);
		if (!lines.isEmpty() && lines.get(0).size() > 0) {
			this.method = lines.get(0).get(0);
			this.path = lines.get(0).get(1);
			String[] protocolAndVersion = lines.get(0).get(2).split("/");
			this.protocol = protocolAndVersion[0];
			this.version = protocolAndVersion[1];
		}
		String keepAliveStr = "";
		
		for (ArrayList<String> line : lines) {
			System.out.println("line: " + line);
			if (!line.isEmpty() && line.get(0).toLowerCase().contains("connection")) {
				keepAliveStr = line.get(1);
			}
		}
		System.out.println("keepAliveStr: " + keepAliveStr);
		if (keepAliveStr.equals("keep-alive")) {
			this.keepAlive = true;
		} else {
			this.keepAlive = false;
		}
		
		// DEBUGGING
		// REMOVE THIS LATER
		System.out.println(this.toString());
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