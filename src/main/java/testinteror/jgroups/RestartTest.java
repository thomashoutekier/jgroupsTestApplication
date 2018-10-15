package testinteror.jgroups;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class RestartTest {
	
	private final PrintWriter testLogs;
	static DateFormat dateformat = DateFormat.getInstance();
	int delay = 30;
	public RestartTest() {
		try{
			testLogs = new PrintWriter(new FileWriter(new File("/RestartTest." + new Date().getTime())+ ".log"));
		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void printToFile(String line) {
		testLogs.println(dateformat.format(new Date()) + " :" + line);
		testLogs.flush();
	}
	
	public static void main(String[] args) {
		if (args == null || args.length < 4) {
			throw new RuntimeException("please specify the ip, port for the webserver, #victims and delay");
		}
		RestartTest test = new RestartTest();
		String target = args[0];
		int port = Integer.parseInt(args[1]);
		int nbVictims = Integer.parseInt(args[2]);
		int delaySeconds = Integer.parseInt(args[3]);
		test.delay = delaySeconds;
		int cycle = 0;
		test.printToFile("starting test with target " + target);
		while (!Thread.currentThread().isInterrupted()) {
			try {
				test.printToFile("BEGIN ************** CYCLE + " + ++cycle + " *******************************");
				test.runTest(nbVictims, target, port);
			}catch (Exception e) {
				e.printStackTrace();
				try {
					System.out.println("waiting " + delaySeconds + "s after exception");
					test.printToFile("waiting " + delaySeconds + "s after exception");
					Thread.sleep(delaySeconds *1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} finally {
				test.printToFile("END ************** CYCLE + " + cycle + " *******************************");
			}
		}

	}

	private void runTest(int nbVictims, String target, int port) throws Exception{
		List<String> others = new ArrayList<String>(allNodes(target, port));
		//others.remove(target);
		int sizeBefore = others.size();
		System.out.println("other nodes: " + others);
		System.out.println("selecting " + nbVictims + " random nodes from cluster size " + sizeBefore);
		printToFile("cluster (" + others.size() + ") " + others);
		List<String> victims = new ArrayList<>();
		Random random = new Random(System.currentTimeMillis());
		for (int v = 0; v < nbVictims; v++) {
			String victim = others.get(random.nextInt(others.size()));
			while(victims.contains(victim)) {
				victim = others.get(random.nextInt(others.size()));
			}
			victims.add(victim);
		}
		System.err.println("restarting victims: " + victims);
		printToFile("restarting (" + victims.size() + ") victims " + victims);
		restart(victims, port);
		
		printToFile("waiting "+ delay + "s before check" );
		Thread.sleep(delay * 1000);
		
		
		List<String> after = new ArrayList<String>(allNodes(target, port));
		//after.remove(target);
		printToFile("cluster after restart: (" + after.size() + ") " + after);
		if (after.size() != others.size()) {
			printToFile("ERROR: expected a clustersize of " + others.size() + " but we have (" + after.size() + ") " + after);
		}
		
		
		
	}
	
	
	private List<String> allNodes(String target, int port) throws Exception{
		URL viewUrl = new URL("http://" + target + ":" + port + "/view");
		HttpURLConnection con = (HttpURLConnection) viewUrl.openConnection();
		con.setRequestMethod("GET");
		
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer nodesString = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
			nodesString.append(inputLine);
		}
		in.close();
		con.disconnect();
		
		List<String> others = Arrays.asList(nodesString.toString().split(","));
		return others;
	}
	
	private void restart(List<String> victims, int port) throws Exception{
		for (String victimIp : victims) {
			System.out.println("restarting " + victimIp);
			printToFile("restarting " + victimIp);
			URL restartURL = new URL("http://" + victimIp + ":" + port + "/stop");
			HttpURLConnection restartCon = (HttpURLConnection) restartURL.openConnection();
			restartCon.setRequestMethod("GET");
			restartCon.getResponseCode();
		}
	}
	
	
}
