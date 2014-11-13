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

import javax.swing.JOptionPane;

public class ComputationClient implements CAComputationKernel {
	
	Socket client = null;
	
	String host = "127.0.0.1";
	int port    = 8888;
	
	OutputStream out;
	InputStream  in;
//	ObjectOutputStream oos;
//	BufferedOutputStream bos;
	int bufferSize = 1024*1024;
	ObjectInputStream ois;
//	BufferedInputStream bis;

	GridPoints gridPoints;
	
	private int pointsNum = 100;
	
//	private int size  = 50;
	private int xsize = 50;
	private int ysize = 50;
	private int zsize = 50;
	private float  width, depth, height;

	public ComputationClient(int size, float boxscale) {
		this(size, size, size, boxscale, boxscale, boxscale);
	}
	
	public ComputationClient(int xsize, int ysize, int zsize,
			float width, float depth, float height) {
		
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
		
		/* initialize the memory of grid */
		pointsNum = 0;
		gridPoints = null;//new GridPoints(xsize, ysize, zsize, width, depth, height);
//		grid = new GridDot[xsize][ysize][zsize];
//		gridptr = grid[gridIndex];
//		gridPtrBuff = grid[gridIndexBuff];
		
		// initialize grid dots
//		initGridDots();
	}
	
//	@Override
	public void setSocket(String host, int port) {
		this.host = host;
		this.port = port;
	}
	
//	@Override
	public boolean closeSocket() {
		if (client == null)
			return true;
		try {
//			System.out.println("*Closing");
			this.sendMessage("GoodBye");
			this.client.close();
			this.client = null;
//			System.out.println("*Success");
		} catch (IOException e) {
			System.err.println("In closeSocket() "+e.getMessage()+client);
			return false;
		}
		return true;
	}

	@Override
	public boolean init() {
		return initClient(host, port);
	}
	
	private boolean initClient(String host, int port) {
		
//		char cbuf[] = new char[512];
		try {
			System.out.println(" connecting to "+host+":"+port);
			client = new Socket(host, port);
			if (client == null)
				return false;
			System.out.println(" connect success Socket"+client);
			System.out.print(" creating streams...");
//			bos = new BufferedOutputStream(client.getOutputStream(), bufferSize);
//			oos = new ObjectOutputStream(bos);
			
//			bis = new BufferedInputStream(client.getInputStream(), bufferSize);
			in = client.getInputStream();
			out = client.getOutputStream();
			ois = new ObjectInputStream(client.getInputStream());
			System.out.println("ok");
		} catch (UnknownHostException e) {
//			e.printStackTrace();
			System.err.println(e.getMessage());
			return false;
		} catch (ConnectException e) {
			System.err.println(e.getMessage());
			JOptionPane.showMessageDialog(null, e.getMessage()+"@"+host+":"+port);
			return false;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		return true;
		
	}
	
	private String waitMessage() throws IOException {
		byte[] buf = new byte[128];
		int num = in.read(buf);
		// XXX clear the test code
		System.out.println("[MSG] Recv " + new String(buf,0,num)+" from "+client.getInetAddress().getHostAddress());
		
		return new String(buf,0,num);
	}
	
	private void sendMessage(String msg) throws IOException {
		// XXX clear the test code
		System.out.println("[MSG] Send " + new String(msg.getBytes())+" to "+client.getInetAddress().getHostAddress());
		
		out.write(msg.getBytes());
	}
	
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
	public boolean update() {

		try {
			this.sendMessage("RequestGridPoints");
			String msg = this.waitMessage();
			if (msg.equals("GridPointsDataReady")) {
				gridPoints = (GridPoints)ois.readObject();
				System.out.println("* Get gridpoints");
				System.out.println(" |- GridPoints.updatecount:"+gridPoints.getUpdateCount());
				System.out.println(" |- GridPoints.pointsNum:  "+gridPoints.pointsNum);
				System.out.println(" |- GridPoints.update:     "+gridPoints.isUpdated());
				return true;
			} else if (msg.equals("GridPointsDataNotReady")) {
				System.out.println("* Remote Data is not ready, wait until ready...");
				this.sendMessage("WaitUntilGridPointsUpdate");
				msg = this.waitMessage();
				if (msg.equals("GridPointsDataReady")) {
					System.out.println("Data is ready but not read");
//					return update(); // TODO 更新已经准备好了，一定要马上传输吗？会陷入死循环吗？
				}
			} else if (msg.equals("ComputeThreadStoped")) {
				this.sendMessage("StartComputeThread");
				System.out.println("start remote compute thread...");
				msg = this.waitMessage();
				if (msg.equals("StartComputeThreadOK"))
					System.out.println("start remote compute thread...ok");
			}
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: "+e.getMessage());
			return false;
		} catch (IOException e) {
			System.err.println("ERROR in CompuataionClient.update: "+e.getMessage());
			e.printStackTrace();
			return false;
		}
		return false;
		
	}
	
	@Override
	public boolean initialized() {
		if (client==null)
			return false;
		else 
			return true;
	}
	
	/* END of implements of the methods of interface CAComputaionKernel */

}

