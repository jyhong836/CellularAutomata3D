package com.cellular3d.dots3d.grid;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

//FIXME 目前同步锁的设定是不合理的，最好在class内部设定

public class ComputationClient implements CAComputationKernel {
	
	Socket client = null;
	
	String host = "127.0.0.1";
	int port    = 8000;
	float timeout = 1;
	
	OutputStream out;
	InputStream  in;

	GridPoints gridPoints;
	private int updateCount = 0;
	
//	private int size  = 50;
	private int xsize = 50;
	private int ysize = 50;
	private int zsize = 50;
//	private float  width, depth, height;

	public ComputationClient(int size, float boxscale) {
		this(size, size, size);
	}
	
	public ComputationClient(int xsize, int ysize, int zsize) {
		
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
		
		gridPoints = null;
		
	}
	
	public void setSocket(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
	public boolean closeSocket() {
		System.out.println("[SOCKET] closing socket...");
		if (client == null) {
			System.out.println("[SOCKET] client is null. client socket is already closed");
			return true;
		} try {
			System.out.println("[SOCKET] Closing");
			this.sendMessage("GoodBye");
			this.client.close();
			this.client = null;
			System.out.println("[SOCKET] close success");
		} catch (IOException e) {
			if (e.getMessage().equals("Socket closed")) {
				System.out.println("[SOCKET] socket has been closed");
				this.client = null;
				return true;
			} else {
				System.err.println("ERROR in closeSocket() "+e.getMessage()+" "+client);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean init() {
		return initClient(host, port);
	}
	
	private boolean initClient(String host, int port) {
		
		try {
			System.out.println(" connecting to "+host+":"+port);
			client = new Socket(host, port);
			if (client == null)
				return false;
			client.setKeepAlive(true);
			client.setSoTimeout((int)(timeout*1000));
			System.out.println(" connect success Socket"+client);
			System.out.print(" creating streams...");

			in = client.getInputStream();
			out = client.getOutputStream();
			
			System.out.println("ok");
		} catch (UnknownHostException e) {
//			e.printStackTrace();
			System.err.println("ERROR in initClient: "+e.getMessage());
			return false;
		} catch (ConnectException e) {
			System.err.println("ERROR in initClient: "+e.getMessage());
			JOptionPane.showMessageDialog(null, e.getMessage()+"@"+host+":"+port);
			return false;
		} catch (IOException e) {
			System.err.println("ERROR in initClient: "+e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		return true;
		
	}
	
	private String waitMessage() throws IOException {
		byte[] buf = new byte[128];
		int num = in.read(buf);
		System.out.println("[MSG] Recv " + new String(buf,0,num)+" from "+client.getInetAddress().getHostAddress());
		
		return new String(buf,0,num);
	}
	
	private void sendMessage(String msg) throws IOException {
		System.out.println("[MSG] Send " + new String(msg.getBytes())+" to "+client.getInetAddress().getHostAddress());
		
		try {
			out.write(msg.getBytes());
		} catch (IOException e) {
			System.out.println("[MSG] Send "+msg+" FAILED for "+e.getMessage());
			throw e;
		}
	}
	
//	private void flushSendMessage() throws IOException {
//		out.flush();
//	}
	
	/**
	 * FIXME 这里还需要验证是否已经更新了
	 */
	@Override
	public GridPoints getGridPoints() {
		return gridPoints;
	}
	
//	public GridDot[][][] getGridDots(int xsize, int ysize, int zsize) {
//		GridDot[][][] griddots = new GridDot[xsize][ysize][zsize];
//		
//		try {
//			for (GridDot[][] gridDots2 : griddots) {
//				for (GridDot[] gridDots3 : gridDots2) {
//					for (GridDot gridDot : gridDots3) {
//						gridDot = (GridDot)ois.readObject();
//					}
//				}
//			}
//		} catch (ClassNotFoundException e) {
//			System.err.println("ERROR: "+e.getMessage());
//			return null;
//		} catch (IOException e) {
//			System.err.println("ERROR: "+e.getMessage());
//			return null;
//		}
//		
//		return griddots;
//	}

	/* implement the methods of interface CAComputaionKernel */
	@Override
	public int updatePointsNum() {
		return getPointsNum();
	}

	@Override
	public int getPointsNum() {
		if (gridPoints!=null)
			return gridPoints.pointsNum;
		else
			return 0;
	}

	/**
	 * 
	 */
	@Override
	public GridDot[][][] getGridPtr() {
		return null;
	}

	@Override
	public long getRequiredMemory() {
		return xsize*ysize*zsize*700;
	}

	@Override
	public int getXSize() {
		return xsize;
	}

	@Override
	public int getYSize() {
		return ysize;
	}

	@Override
	public int getZSize() {
		return zsize;
	}

	@Override
	public boolean update() throws IOException {

		try {
			this.sendMessage("RequestGridPoints");
			String msg = this.waitMessage();
			if (msg.equals("GridPointsDataReady")) {
				
				/* init */
				GZIPInputStream gzipis = new GZIPInputStream(in);
				ObjectInputStream ois = new ObjectInputStream(gzipis);
				
				/* read */
				gridPoints = (GridPoints)ois.readObject();
				
				/* -------TODO these code may be useless-------- */
				System.out.println("Read Object Ok, waiting FIN MSG...");
				this.sendMessage("GridPointsOK");
				msg = this.waitMessage();
				if (msg.equals("SendPointsGridFinished")) { // CHK data fininshed

					this.updateCount = gridPoints.getUpdateCount();
					System.out.println("* Get gridpoints");
					System.out.println(" |- GridPoints.updatecount:"+updateCount);
					System.out.println(" |- GridPoints.pointsNum:  "+gridPoints.pointsNum);
					System.out.println(" |- GridPoints.update:     "+gridPoints.isUpdated());
					
				} else 
					throw new UnexpectedMessageException("Expected SendPointsGridFinished but get "+msg);
				/* --------------------------------------------- */
				
				return true;
				
			} else if (msg.equals("GridPointsDataNotReady")) {
				
				System.out.println("* Remote Data is not ready, wait until ready...");
				this.sendMessage("WaitUntilGridPointsUpdate");
				msg = this.waitMessage();
				if (msg.equals("GridPointsDataReady")) {
					System.out.println("Data is ready but not read");
				} else 
					throw new UnexpectedMessageException("Expected GridPointsDataReady but get "+msg);
				
			} else if (msg.equals("ComputeThreadStoped")) {
				
				this.sendMessage("StartComputeThread");
				System.out.println("start remote compute thread...");
				msg = this.waitMessage();
				if (msg.equals("StartComputeThreadOK"))
					System.out.println("start remote compute thread...ok");
				else
					throw new UnexpectedMessageException("Expected StartComputeThreadOK but get "+msg);
				
			} else // unknown msg
				throw new UnknownMessageException(msg);
			
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.flush();
			System.err.println("ERROR in "+this.getClass().getSimpleName()+".update():"+e.getMessage());
			System.err.flush();
//			System.err.flush();
			if (e.getMessage().equals("Socket Closed"));
			else if (e.getMessage().equals("Read timed out")) {
				this.clearBuf();
			} else if (e.getMessage().equals("Not in GZIP format")) {
				this.clearBuf();
			} else {
				e.printStackTrace();
//				throw e;
				System.exit(-1); // FORCE EXIT
			}

		} catch (UnexpectedMessageException e) {
			
			System.out.println("[MSG] UnexpectedMessageException:"+e.getMessage());
			try {
				this.clearBuf(); // clear buffer
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1); // FORCE EXIT
			}
		} catch (UnknownMessageException e1) {
			
			System.out.println("[MSG] UnknownMessageException:"+e1.getMessage());
			try {
				this.clearBuf(); // clear buffer
				this.sendMessage("RequestClearBuffer"); // request server clear buffer
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1); // FORCE EXIT
			}
			
		}
		return false;
		
	}
	
	private void clearBuf() throws IOException {
		int num = in.available();
		in.skip(num);
	}

	@Override
	public boolean initialized() {
		if (client==null)
			return false;
		else 
			return true;
	}

	@Override
	public int getCount() {
		return updateCount;
	}
	
	/* END of implements of the methods of interface CAComputaionKernel */

}

