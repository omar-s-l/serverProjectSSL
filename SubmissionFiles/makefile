JCC = javac
JFLAGS = -g

default: Utils.class Request.class Response.class RedirectMap.class WebServer.class WebServerSSL.class
        
Utils.class: Utils.java
		$(JCC) $(JFLAGS) Utils.java
        
Request.class: Utils.java
		$(JCC) $(JFLAGS) Utils.java
        
Response.class: Utils.java
		$(JCC) $(JFLAGS) Utils.java
        
RedirectMap.class: Utils.java
		$(JCC) $(JFLAGS) Utils.java

WebServer.class: WebServer.java
		$(JCC) $(JFLAGS) WebServer.java
        
WebServerSSL.class: WebServerSSL.java
		$(JCC) $(JFLAGS) WebServerSSL.java

clean:
		$(RM) *.class