package com.cellular3d.dots3d.grid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ComputationServer {
	
	ServerSocket server = null;
	Socket       accClient = null;
	
//	String host = "127.0.0.1";
	int port    = 8000;
	
	ObjectOutputStream oos;
//	BufferedOutputStream bos;
	int bufferSize = 1024*10;
	ObjectInputStream ois;
	BufferedInputStream bis;
	InputStream in;
	OutputStream out;
	InputStreamReader inReader;
	OutputStreamWriter outWriter;

	public ComputationServer() {
	}

	public ComputationServer(int port) {
		this.port = port;
	}
	
	public boolean initServer() {
		try {
			System.out.print("init server socket at port:"+port+"...");
			server = new ServerSocket(port);
			System.out.println("ok");
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean waitClient() {
		try {
			if (accClient!=null) {
				System.out.println("close client socket "+accClient);
				accClient.close();
			}
			System.out.println("server "+ server+" is waiting client");
			accClient = server.accept();
			System.out.println("accepted client: "+accClient);
//			bos = new BufferedOutputStream(accClient.getOutputStream(), bufferSize);
//			oos = new ObjectOutputStream(bos);
			out = accClient.getOutputStream();
			oos = new ObjectOutputStream(out);
			in = accClient.getInputStream();
//			outWriter
//			inReader = new InputStreamReader(in);
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		return true;
	}
	
	public String waitMessage() throws IOException {
		byte[] buf = new byte[128];
		int num = in.read(buf);
		if (num>0) {
			// XXX clear the test code
			System.out.println("Get " +num+" bytes Message:" + new String(buf,0,num)+" from "+accClient.getInetAddress().getHostAddress());
			
			return new String(buf,0,num);
		} else {
			System.out.println("read null"+num+" from "+accClient.getInetAddress().getHostAddress());
			return null;
		}
	}
	
	public void sendMessage(String msg) throws IOException {
		// XXX clear the test code
		System.out.println("Send Message" + msg+" to "+accClient.getInetAddress().getHostAddress());
		
		byte[] buf = msg.getBytes();
		out.write(buf);
	}
	
	public boolean sendGridPointsArray(GridPoints gridPoints) {
		try {
			oos.writeObject(gridPoints);
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		return true;
	}
	
//	public boolean sendGridDots(GridDot[][][] griddots) {
//		try {
//			for (GridDot[][] gridDots2 : griddots)
//				for (GridDot[] gridDots3 : gridDots2)
//					for (GridDot gridDot : gridDots3)
//						oos.writeObject(gridDot);
//		} catch (IOException e) {
//			System.err.println("ERROR: "+e.getMessage());
//			return false;
//		}
//		return true;
//	}

}
