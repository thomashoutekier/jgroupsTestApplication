package testinteror.jgroups;



import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class TestAppJgroups implements Receiver {

	final static Logger logger = Logger.getLogger(TestAppJgroups.class.getName());
	final JChannel channel;
	
	public TestAppJgroups() throws Exception{
		channel =new JChannel("udp.xml");
	}
	
	public static void main(String[] args) {
		System.setProperty("java.net.preferIPv4Stack" , "true");
		if (args == null || args.length <= 1) {
			throw new RuntimeException("please specify the port for the webserver and ip");
		}
		logger.log(Level.WARNING, "starting ");
		int port = Integer.parseInt(args[0]);
		String ip = args[1];
		try {
			TestAppJgroups app = new TestAppJgroups();
			HttpServer server = HttpServer.create(new InetSocketAddress(ip, port), 0);
			HttpContext stopContext = server.createContext("/stop");
			stopContext.setHandler(exchange -> {
				logger.log(Level.INFO, "stopping node");
				app.channel.close();
				exchange.sendResponseHeaders(200, 0);
				final OutputStream output = exchange.getResponseBody();
				output.write(new byte[0]);
				exchange.close();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Thread.currentThread().interrupt();
				System.exit(0);
			});
			HttpContext viewContext = server.createContext("/view");
			viewContext.setHandler(exchange -> {
				logger.log(Level.INFO, "retrieving view");
				exchange.sendResponseHeaders(200, 0);
				final OutputStream output = exchange.getResponseBody();
				output.write(app.getView().getBytes());
				output.write('\n');
				exchange.close();
			});
			
			server.start();
			
			app.start(ip);
		}catch (Exception e) {
			e.printStackTrace();
		}
		 
	}
	
	
	
	public void start(String ip) throws Exception{
		 
	     channel.setReceiver(this);
	     channel.setName(ip);
	     channel.connect("interOR-test-cluster");
	     eventLoop();
	     channel.close();	      
	}

	
	private String getView(){
		return String.join(",", channel.getView().getMembers().stream().map(address -> address.toString()).collect(Collectors.toList()));
	}
	
	private void eventLoop() {
	    BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
	    while(!Thread.interrupted()) {
	        try {
	            System.out.print("> "); System.out.flush();
	            String line=in.readLine().toLowerCase();
	            if(line.startsWith("quit") || line.startsWith("exit"))
	            	Thread.currentThread().interrupt();
	            	System.exit(0);
	                break;
	            
	        }
	        catch(Exception e) {
	        	e.printStackTrace();
	        	logger.log(Level.SEVERE, e.getMessage(), e);
	        }
	    }
	}


	@Override
	public void receive(Message msg) {
       	  logger.log(Level.INFO, "received msg from " + msg.getSrc() + ": " + msg.getObject());
	}



	@Override
	public void getState(OutputStream output) throws Exception {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void setState(InputStream input) throws Exception {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void viewAccepted(View new_view) {
    	logger.log(Level.WARNING, "accepted new view: " + new_view);
	}



	@Override
	public void suspect(Address suspected_mbr) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void block() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void unblock() {
		// TODO Auto-generated method stub
		
	}
	
	
}
