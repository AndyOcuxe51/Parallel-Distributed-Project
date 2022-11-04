package proj_2_new;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

//tested using ngrok

public class TCPServer {
	private static final String FILETOSEND = "p1.png";// This file should be sent
	public static final File FILETORECEIVE = new File("postSendFile.jpeg"); // file to download
	
	//building each ip section & port for easy access
	static String routerIP = "";
	static String thisIP;
	
	//ports are randomized and adjusted later, for now we use these defaults
	static int tempPort = 5557;
	static int thisPort = 5559;

//   private static final String FILENAME = "panda.jpg";
	public static void main(String args[]) throws IOException {
		new TCPServer();
	}

	public TCPServer() throws IOException {
		// important to have both for flavor, but the router and client IP's should be
		// the same
		// due to localized data

		Socket generatedConnectionSocket = null;
		Socket socketToClient = null;
		thisIP = InetAddress.getLocalHost().getHostAddress();
		routerIP = thisIP;
		Scanner scan = new Scanner(System.in); // will be used for user to select the data type and also to send
		// building the Client->Router connection, don't need to send anything from
		// Router back.
		PrintWriter out = new PrintWriter(new Socket(routerIP, 5556).getOutputStream(), true);

		// THE ONLY WAY TO FIX THIS IS TO PARSE DATA IN THE THREAD!
		// The client needs to be linear in this order:
		/*
		 * Become built in the router / logged in the table. C->R1 Then request to see
		 * the server. C->R1->R2->S, S->C Finally a communication loop between these
		 * two. S->C over and over
		 */

		// this section sends the data to the router.
		out.println("LogMe");
		out.println(thisIP);

		// Wait for request, then connecting to Router2 which hands over target IP
		ServerSocket tempSock = new ServerSocket(tempPort);
		System.out.println("Waiting for request from Client on port 5557");
		generatedConnectionSocket = tempSock.accept();
		tempSock.close();
		// read input from router
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(generatedConnectionSocket.getInputStream()));
		} catch (IOException e) {
			System.err.println("I/O Connetion failed for: " + routerIP);
			System.exit(1);
		}

		String clientIPFound = "";
		try {
			clientIPFound = in.readLine();
		} catch (IOException e) {
			System.err.println("Couldn't read from router");
			System.exit(1);
		}

		generatedConnectionSocket.close();

		// connecting to the client
		try {
			socketToClient = new Socket(clientIPFound, thisPort);
			System.out.println("FINALLY CONNECTED to Client!");
		} catch (IOException e) {
			System.err.println("Client not found.");
			System.exit(1);
		}

		// Receiving the string

		// this receives the string and then shoots it back as an upper case version
		String str = "";
		try {
			in = new BufferedReader(new InputStreamReader(socketToClient.getInputStream()));
			str = in.readLine();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + routerIP);
			System.exit(1);
		}

		System.out.println("Received: " + str);
		str = str.toUpperCase();

		try {
			out = new PrintWriter(socketToClient.getOutputStream(), true);
			out.println(str);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + routerIP);
			System.exit(1);
		}

		System.out.println("1. Terminal Typing Message");
		System.out.println("2. File Transfer");
		System.out.println("3. Nevermind, Close Connection");
		System.out.print("Please select method of communication: ");
		int messageType = scan.nextInt();

		// Based on the user choice, it determines what will be done. Same thing has to
		// be selected in the other client side
		switch (messageType) {

		// This is for sending messages through the terminal
		case 1:

			String fromServer; // messages received from ServerRouter
			String fromUser; // messages sent to ServerRouter

			long t0, t1, t;

			// Communication process (initial sends/receives

			// The program can echo terminal message to Client,
			// but if we have the input side on the server
			// Then the program will have messages off by one or two lines.
			String input; // used to receive input to send for the server side
			while ((fromUser = in.readLine()) != null) {
				if (fromUser.equalsIgnoreCase("Bye")) {// exit statement
					break;
				}
				fromServer = fromUser.toUpperCase(); // converting received message to upper case
				System.out.println("Client said: " + fromServer);

				// If we remove this section, then the echo works better
				System.out.print("Input message: ");
				fromServer = scan.nextLine(); // reading strings from a file
				if (fromServer.equalsIgnoreCase("bye")) {// exit statement
					break;
				}
				if (fromServer != null) {
					out.println(fromServer); // sending the strings to the Server via ServerRouter
					// t0 = System.currentTimeMillis();
				}
			}

			break;

		// This is to send and receive a file
		// When I ran this using the old router it did try to send and it download a
		// corrupt file, So I cannot confirm if this work as intended
		case 2:
			int DownSelect;
			System.out.println("Enter 1 to send");
			System.out.println("Enter 2 to download");
			System.out.println("Choose: ");
			DownSelect = scan.nextInt();
			switch (DownSelect) {
			case 1:

				File f1 = new File(FILETOSEND);
				// createNewFile makes one if not present, otherwise nothing
				f1.createNewFile();

				// this setup is for reading the data bytes from file into an array
				InputStream inputStream = new FileInputStream(f1);

				byte[] fileContent = inputStream.readAllBytes();
				// System.out.println("File successfully read!");

				// closing reading in from file and opening the socket output to send it
				inputStream.close();
				OutputStream byteOutStreams = socketToClient.getOutputStream();

				// make sure to log times, first when sending file
				long time1 = System.currentTimeMillis();
				// System.out.println("Pushing the file through OutStream");
				System.out.println(byteOutStreams);
				byteOutStreams.write(fileContent); // bing bing the server is written to
				byteOutStreams.flush();

				System.out.println(FILETOSEND + " file successfully flushed from stream!");
				byteOutStreams.close();

				long time2 = System.currentTimeMillis();
				System.out.println(FILETOSEND + " file sent in: " + (time2 - time1) + " miliseconds");
				break;
			case 2:
				long Dt2;

				try {

					// setting up IO basics, REMEMEBER THEY ARE STREAMS, which happen over time
					InputStream byteInStream = socketToClient.getInputStream(); // byte in
					FILETORECEIVE.createNewFile(); // as mentioned in Client, if this file doesn't exist it gets created
					OutputStream byteOutStream = new FileOutputStream(FILETORECEIVE); // byte out
					// out.println("sd");
					// always -1 so it defaults to false
					int byteFromStream = -1;
					long Dtime1 = System.currentTimeMillis();

					// don't know how long the stream will go / takes time, textbook dynamic use
					ArrayList<Integer> byteArray = new ArrayList<Integer>();

					boolean fileBeganDownload = true;

					// once again if the end of stream is detected, or a blank file, it kicks out
					// using -1
					System.out.println("Download of a file in progress!");

					while ((byteFromStream = byteInStream.read()) != -1) { // An error happens here not sure why
						if (fileBeganDownload) {
							Dtime1 = System.currentTimeMillis();
							fileBeganDownload = false;
						}
						//System.out.println(byteFromStream);
						byteArray.add(byteFromStream);

					}

					Dt2 = System.currentTimeMillis();
					System.out.println("File downloaded to Server in " + (Dt2 - Dtime1) + " miliseconds");

					// writing only works with arrays of bytes
					// can't unwrap an array of bytes so this is the next best thing
					byte[] primitiveBytes = new byte[byteArray.size()];

					for (int i = 0; i < byteArray.size(); i++) {
						primitiveBytes[i] = byteArray.get(i).byteValue();
					}

					// throwing everything into the file
					Dtime1 = System.currentTimeMillis();

					byteOutStream.write(primitiveBytes);
					byteOutStream.flush();
					// System.out.println(FILETORECEIVE.getName() + " file is the completed
					// download.");

					// close everything, we're done
					byteOutStream.close();
					Dt2 = System.currentTimeMillis();

					System.out.println("File localized to Server in " + (Dt2 - Dtime1) + " miliseconds");

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			break;
		// this closes the sockets right away
		case 3:
			System.out.println("Disconnecting, Goodbye!");
			socketToClient.close();
			in.close();
			out.close();
			scan.close();
			break;
		}
		// Code from the original instructions

		// close socket connections
		// after verification the buffered readers are done
		// can't be closed because they close the socket, binding.
		socketToClient.close();
		in.close();
		out.close();
		scan.close();
		// clientSocket.close();
//		serverSocket.close();
	}

}