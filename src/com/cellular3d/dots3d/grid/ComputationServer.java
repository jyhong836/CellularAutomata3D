package com.cellular3d.dots3d.grid;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

public class ComputationServer {
	
	ServerSocket server = null;
	Socket       accClient = null;
	
//	String host = "127.0.0.1";
	int port    = 8000;
	float timeout = 1f;
	
	InputStream in;
	OutputStream out;

	/**
	 * Create a computation server at port 8000
	 */
	public ComputationServer() {
	}

	/**
	 * Create a computation server at port
	 * @param port
	 */
	public ComputationServer(int port) {
		this.port = port;
	}

	/**
	 * Create a computation server at port, and set timeout.
	 * @param port
	 * @param timeout the timeout in second.
	 */
	public ComputationServer(int port, float timeout) {
		this.port = port;
		this.timeout = timeout;
	}
	
	/**
	 * Initialize the server, if success return true, else false.
	 * @return the status of the init process
	 */
	public boolean initServer() {
		try {
			System.out.print("init server socket at port:"+port+"...");
			server = new ServerSocket(port);
			System.out.println("ok");
		} catch (IOException e) {
			System.err.println("ERROR when init server socket: "+e.getMessage());
			return false;
		}
		
		return true;
	}
	
	/**
	 * Wait until the last client connected.
	 * @return true, if success.
	 */
	public boolean waitClient() {
		
		try {
			if (accClient!=null) { // check if the client has been closed correctly.
				System.out.println("[SOCKET] close client "+accClient);
				accClient.close();
			} else {
				System.out.println("[SOCKET] client(null) has been closed");
			}
			
			System.out.println("[SOCKET] server "+ server+" is waiting client");
			
			/* wait client blocking */
			accClient = server.accept();
			accClient.setKeepAlive(true);
			accClient.setSoTimeout((int)(timeout*1000)); // default will blocking forever
			
			System.out.println("[SOCKET] accepted client: "+accClient);
			
			/* init Streams*/
			out = accClient.getOutputStream();
			in  = accClient.getInputStream();

		} catch (IOException e) {
			System.err.println("[SOCKET] ERROR in waitClient: "+e.getMessage());
			return false;
		}
		return true;
		
	}
	
	public String waitMessage(int timeout) throws IOException {
		accClient.setSoTimeout(timeout*1000);
		String string = this.waitMessage();
		accClient.setSoTimeout(timeout*1000);
		return string;
	}
	
	/**
	 * This method will block until get the message from client.
	 * @return the message got from server
	 * @throws IOException
	 */
	public String waitMessage() throws IOException {
		
		byte[] buf = new byte[128];
		int num = 0;
		try {
			num = in.read(buf);
		} catch (IOException e) {
			System.out.println("[MSG] Read data error for "+e.getMessage());
			throw e;
		}
		if (num>0) {
			System.out.println("[MSG] Recv " + new String(buf,0,num)+" from "+accClient.getInetAddress().getHostAddress());
			
			return new String(buf,0,num);
		} else { // num<=0
			System.out.println("read null"+num+" from "+accClient.getInetAddress().getHostAddress());
			return null;
		}
		
	}
	
	/**
	 * Send the String msg to client.
	 * @param msg the message to be sent.
	 * @throws IOException
	 */
	public void sendMessage(String msg) throws IOException {
		
		System.out.println("[MSG] Send " + msg+" to "+accClient.getInetAddress().getHostAddress());
		
		byte[] buf = msg.getBytes();
		try {
			out.write(buf);
//			System.out.println("[MSG] Send " + msg+" to "+accClient.getInetAddress().getHostAddress()+"...ok");
		} catch (IOException e) {
			System.out.println("[MSG] Send " + msg+" FAILED for "+e.getMessage());
			throw e;
		}
		
	}
	
	/**
	 * Send the computed data, GridPoints.
	 * @param gridPoints
	 * @return
	 * @see {@linkplain com.cellular3d.dots3d.grid.GridPoints}
	 */
	public boolean sendGridPointsArray(GridPoints gridPoints) {
		
		try {
			ObjectOutputStream oos;
			
			GZIPOutputStream gzipos = new GZIPOutputStream(out);
			oos = new ObjectOutputStream(gzipos);
			oos.writeObject(gridPoints);
			
			gzipos.finish();

			String chk = this.waitMessage();
			if (chk.equals("GridPointsOK"))
				this.sendMessage("SendPointsGridFinished");
			else {
				System.out.println("Receive Unknown MSG, clear buf...");
				this.clearBuf();
			}
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Clear all data in InputStream Buffer.
	 * @throws IOException
	 */
	public void clearBuf() throws IOException {
		System.out.println(" * FORCE to clear the InputStream buffer");
		int num = in.available();
		in.skip(num);
	}

//	public void flushSendMessage() throws IOException {
//		this.out.flush();
//	}
	
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
