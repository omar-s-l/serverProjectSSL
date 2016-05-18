import java.util.ArrayList;

class Request { 
	// Path and opcode variables
	private String path;
	private String method;

	// Constructor
	Request(ArrayList<ArrayList<String>> lines) {
		this.method = lines.get(0).get(0);
		this.path = lines.get(0).get(1);
	}

	// Methods
	// Getter methods
	public String getMethod(){
		return this.method;
	}


	public String getPath(){
		return this.path; 
	}
}