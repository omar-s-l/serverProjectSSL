import java.io.*;
import java.util.*;

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