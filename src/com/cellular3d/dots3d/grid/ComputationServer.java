package com.cellular3d.dots3d.grid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ComputationServer {
	
	ServerSocket server = null;
	Socket       accClient = null;
	
//	String host = "127.0.0.1";
	int port    = 8000;
	
	ObjectOutputStream oos;
	BufferedOutputStream bos;
	int bufferSize = 1024*10;
	ObjectInputStream ois;
	BufferedInputStream bis;

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
			System.out.println("server "+ server+" is waiting client");
			accClient = server.accept();
			System.out.println("accepted client: "+accClient);
//			bos = new BufferedOutputStream(accClient.getOutputStream(), bufferSize);
//			oos = new ObjectOutputStream(bos);
			oos = new ObjectOutputStream(accClient.getOutputStream());
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		return true;
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
