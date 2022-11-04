package proj_2_new;
//package proj_2_new;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

//tested using ngrok

public class TCPClient {
	private static final String FILETOSEND = "j2.JPG";// This file should be sent
	public static final File FILETORECEIVE = new File("postj4.JPG"); // file to download

	static String connectionIP = "";
	static String thisIP;
	
	//once again random later, but manual for simple testing
	static int recieverPort = 5559;

	public static void main(String args[]) throws IOException {
		new TCPClient();
	}

	public TCPClient() throws IOException {

		// important to have both for flavor, but the router and client IP's should be
		// the same
		// due to localized data
		Scanner scan = new Scanner(System.in); // will be used for user to select the data type and also to send
		Socket generatedConnectionSocket = null;
		thisIP = InetAddress.getLocalHost().getHostAddress();
		connectionIP = thisIP;

		// building the Client->Router connection, don't need to send anything from
		// Router back.
		PrintWriter out = new PrintWriter(new Socket(connectionIP, 5555).getOutputStream(), true);

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

		// Request B connects to me, then wait for B to connect to me
		// once registered we can ask the router to push us over to the other client
		out.println("ClientToRouter1");
		out.println(thisIP);

		// This piece is the waiting piece, we find the Server by it polling and accept
		// it.
		try {
			ServerSocket serverSocket = new ServerSocket(recieverPort);
			System.out.println("Waiting on socket connection from Server");
			generatedConnectionSocket = serverSocket.accept();
			serverSocket.close();
			System.out.println("Socket connection from Server created");
		} catch (IOException e) {
			System.err.println("Client Server Socket Failed.");
			System.exit(1);
		}

		// this will establish everything, now we just need to communicate with the
		// other client.
		// we now assign the output and input to the new connection
		// The generatedConnectionSocket will be the socket between C->S

		// sample of sending a message, simulation of a echo server
		String clientMessage = "Howdy Howdy";

		try {
			out = new PrintWriter(generatedConnectionSocket.getOutputStream(), true);
			out.println(clientMessage);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + connectionIP);
			System.exit(1);
		}

		// then receiving a message
		String temp = "";
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(generatedConnectionSocket.getInputStream()));
			temp = in.readLine();
		} catch (IOException e) {
			System.err.println("No I/O For: " + connectionIP);
			System.exit(1);
		}

		System.out.println("Received: " + temp);

//		boolean Running = true;
//		while (Running) {
//
//		}
		// Menu to let the user know the options for communicating
		System.out.println("1. Terminal Typing Message");
		System.out.println("2. File Transfer");
		System.out.println("3. Nevermind, Close Connection");
		System.out.print("Please select method of communication:");
		int messageType = scan.nextInt();

		// Based on the user choice, it determines what will be done. Same thing has to
		// be selected in the other client side
		switch (messageType) {

		// This is for sending messages through the terminal
		case 1:

			String fromServer; // messages received from ServerRouter
			String fromUser = null; // messages sent to ServerRouter

			long t0, t1, t;

			// Communication process (initial sends/receives

			System.out.println("Input message(enter 'Bye' to exits): ");

//					// Communication while loop
			t0 = System.currentTimeMillis();
			// An initial input has to be sent before the loop or else the program gets
			// stuck waiting
			System.out.print("Input message: ");
			out.println(fromUser);
			out.flush();
			// Slightly modified code from provided example

			while ((fromServer = in.readLine()) != null) {
				t1 = System.currentTimeMillis();
				if (fromServer.equalsIgnoreCase("BYE")) { // exit statement
					break;
				}
				t = t1 - t0;
				// System.out.println("Cycle time: " + t);
				System.out.println("Server said: " + fromServer);

				System.out.print("Input message: ");
				fromUser = scan.nextLine(); // reading strings from a file
				if (fromUser.equalsIgnoreCase("bye")) {// exit statement
					break;
				}
				if (fromUser != null) {
					out.println(fromUser); // sending the strings to the Server via ServerRouter
					t0 = System.currentTimeMillis();
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
				OutputStream byteOutStreams = generatedConnectionSocket.getOutputStream();

				// make sure to log times, first when sending file
				long time1 = System.currentTimeMillis();
				// System.out.println("Pushing the file through OutStream");
				// System.out.println(byteOutStreams);
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
					InputStream byteInStream = generatedConnectionSocket.getInputStream(); // byte in
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
						// System.out.println(byteFromStream);
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
			generatedConnectionSocket.close();
			in.close();
			out.close();
			scan.close();
			break;
		}
		// Code from the original instructions

		// close socket connections
		// after verification the buffered readers are done
		// can't be closed because they close the socket, binding.
		generatedConnectionSocket.close();
		in.close();
		out.close();
		scan.close();
	}
}