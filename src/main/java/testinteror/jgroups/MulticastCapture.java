package testinteror.jgroups;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.List;

import org.jgroups.Message;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.tests.ParseMessages;

public class MulticastCapture {

	public static void main(String[] args) {
		if (args.length < 2) {
			throw new IllegalArgumentException("please provice mcaddress and port");
		}
		String mc = args[0];
		int port = Integer.parseInt(args[1]);
		System.out.println("subscribing to " + mc + ":" + port);
		MulticastSocket s = null;
		ClassConfigurator c = new ClassConfigurator();
		
		
		try {
			s = new MulticastSocket(port);
			s.joinGroup(InetAddress.getByName(mc));
			while(!Thread.interrupted()) {
				byte buf[] = new byte[1024];
				DatagramPacket pack = new DatagramPacket(buf, buf.length);
				s.receive(pack);
				System.out.println("Received data from: " + pack.getAddress().toString() +  ":" + pack.getPort() + " with length: " +  pack.getLength());
				//System.out.write(pack.getData(),0,pack.getLength());
				System.out.println();
				/*
				int offset= pack.getOffset();
				short version=Bits.readShort(pack.getData(), offset);
		        if(!versionMatch(version, sender))
		            return;
		        offset+=Global.SHORT_SIZE;
		        byte flags=data[offset];
		        offset+=Global.BYTE_SIZE;
				*/
				//ByteArrayDataInputStream in=new ByteArrayDataInputStream(pack.getData(), pack.getOffset(), pack.getLength());
				/*
	            Message msg=new Message(false);
	            try{
	            	msg.readFrom(in);
	            }catch (Exception e) {
	            	e.printStackTrace();
				}*/
	            ByteArrayInputStream input = new ByteArrayInputStream(buf);
	            
	            
	            
	            ParseMessages parse = new ParseMessages(input);
	            List<Message> messages = null;
	            try {
	            	//messages = ParseMessages.parse(buf, 0, buf.length);
	            	messages = parse.parse();
	            }catch (Exception e) {
					e.printStackTrace();
				}
	            
	            System.out.println(messages.size() + " messages: ");
	            /*
	            for (Message msg : messages) {
	            	System.out.println("message: " + msg.getHeaders() +  "-> " + msg);
	            	System.out.println("flags: " + msg.getFlags());
	            	Object object = msg.getObject();
	            	System.out.println("object: " + object);
	            	System.out.println();
					
				}
				*/
				
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (s != null) {
					s.leaveGroup(InetAddress.getByName(mc));
					s.close();					
				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
}
