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
		this.method = lines.get(0).get(0);
		this.path = lines.get(0).get(1);
		String[] protocolAndVersion = lines.get(0).get(2).split("/");
		this.protocol = protocolAndVersion[0];
		this.version = protocolAndVersion[1];
		System.out.println(protocol);
		System.out.println(version);
		
		String keepAliveStr = lines.get(2).get(1);
		if (keepAliveStr.equals("keep-alive")) {
			this.keepAlive = true;
		} else {
			this.keepAlive = false;
		}
	}

	// Methods
	// Getter methods
	public String getMethod(){
		return this.method;
	}

	public String getPath(){
		return this.path; 
	}
	
	public boolean keepAlive() {
		return this.keepAlive;
	}
}