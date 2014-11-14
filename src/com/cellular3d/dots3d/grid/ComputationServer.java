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
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ComputationServer {
	
	ServerSocket server = null;
	Socket       accClient = null;
	
//	String host = "127.0.0.1";
	int port    = 8000;
	float timeout = 1f;
	
//	ObjectOutputStream oos;
//	BufferedOutputStream bos;
	int bufferSize = 1024*10;
//	ObjectInputStream ois;
//	BufferedInputStream bis;
	InputStream in;
	OutputStream out;
//	InputStreamReader inReader;
//	OutputStreamWriter outWriter;

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
				System.out.println("[SOCKET] close client "+accClient);
				accClient.close();
			} else {
				System.out.println("[SOCKET] client(null) has been closed");
			}
			System.out.println("[SOCKET] server "+ server+" is waiting client");
			
			accClient = server.accept();
			accClient.setKeepAlive(true);
			accClient.setSoTimeout((int)(timeout*1000));
			
			System.out.println("[SOCKET] accepted client: "+accClient);
//			bos = new BufferedOutputStream(accClient.getOutputStream(), bufferSize);
//			oos = new ObjectOutputStream(bos);
			out = accClient.getOutputStream();
//			ZipEntry ze = new ZipEntry("z1");
//			ZipOutputStream zos = new ZipOutputStream(out);
//			zos.putNextEntry(ze);
			
//			oos = new ObjectOutputStream(out);
//			oos = new ObjectOutputStream(new GZIPOutputStream(out));
//			
			in = accClient.getInputStream();
//			outWriter
//			inReader = new InputStreamReader(in);
		} catch (IOException e) {
			System.err.println("[SOCKET] ERROR in waitClient: "+e.getMessage());
			return false;
		}
		return true;
	}
	
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
		} else {
			System.out.println("read null"+num+" from "+accClient.getInetAddress().getHostAddress());
			return null;
		}
	}
	
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
	
	public boolean sendGridPointsArray(GridPoints gridPoints) {
		
		ObjectOutputStream oos;
		try {
//			out.flush();
			
			BufferedOutputStream bos = new BufferedOutputStream(out,bufferSize);
			GZIPOutputStream gzipos = new GZIPOutputStream(bos);
			oos = new ObjectOutputStream(gzipos);
//			oos.reset(); // reset the old stat of object, if not do this, will not pass the updated object but the old one.
			oos.writeObject(gridPoints);
			
			gzipos.finish();
			bos.flush();
//			oos.close();

			String chk = this.waitMessage();
			if (chk.equals("GridPointsOK"))
				this.sendMessage("SendPointsGridFinished");
			else {
				System.out.println("Receive Unknown MSG, clear buf...");
//				System.exit(-1);
				this.clearBuf();
			}
//			out.flush();
		} catch (IOException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		}
		return true;
	}

	public void clearBuf() throws IOException {
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
