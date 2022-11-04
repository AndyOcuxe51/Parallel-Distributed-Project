package proj_2_new;

//package proj_2_new;

import java.io.*;
import java.net.*;

public class SThread extends Thread {
	private BufferedReader in;
	private Object[][] routingTable;
	private int index;

	// constructor that takes in a socket and a routing table
	//needs to call toClient for a stream to gather data & localize it, no need to declare it directly
	SThread(Object[][] table, Socket toClient, int ind) throws IOException {
		in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
		routingTable = table;
		index = ind;
	}

	//the backbone of the threading project: Threads!
	//and a directory at that!
	@Override
	public void run() {
		String director = null;

		do {
			try {
				director = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			// building the client or router here
			// remember that these connections are 1-way until a new socket is made
			// C->R1->R2->S

			if (director == null)
				break;

			// since this is used as an intermediate for connections as well
			// as nodes, a registry is necessary
			
			switch (director) {
			
			// treating nodes and routers the same here, the router is always going to be
			// index = 0.
			case "LogMe": {
				//System.out.println("Caller's IP written to table.");

				String callerIP = "";
				try {
					callerIP = in.readLine();
					//System.out.println(ip);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				//next in line in routing table
				routingTable[index][0] = callerIP;
				
				//printing table each time it is logged and time managed
				long t1 = System.currentTimeMillis();
				for (int i = 0; i < routingTable.length; i++) {
					System.out.println("RTable " + i + ": " + routingTable[i][0]);
				}
				long t2 = System.currentTimeMillis();
				
				System.out.println("Table Routing LookupTime: " + (t2- t1)+ "ms");
				break;
			}
			case "ClientToRouter1": {
				//System.out.println("connectToRouter tripped.");
				String ip = "";
				try {
					ip = in.readLine();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				Socket toRouter = null;
				try {
					//uses [0][0] because that will send the data to Router2
					toRouter = new Socket((String) routingTable[0][0], 5556);
					//System.out.println("This is toRouter Socket(5556):" + toRouter);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				PrintWriter outToRouter = null;
				try {
					outToRouter = new PrintWriter(toRouter.getOutputStream(), true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// this calls the router version, pushing the IP to client.
				outToRouter.println("Router2ToServer");
				outToRouter.println(ip);

				try {
					toRouter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			}
			case "Router2ToServer":
				try {
					
					//sleeping thread so that if client is called first it will not crash unless server takes 5 seconds.
					Thread.currentThread();
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//System.out.println("Forward2Router tripped.");
				String ClientIP = "";
				try {
					ClientIP = in.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				
				Socket toServer = null;
				try {
					toServer = new Socket((String) routingTable[index][0], 5557);
				}catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				
				//sends message out to server
				PrintWriter outToServer = null;
				try {
					outToServer = new PrintWriter(toServer.getOutputStream(), true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				outToServer.println(ClientIP);
				break;
			}
		} while (director != null);
	}
}