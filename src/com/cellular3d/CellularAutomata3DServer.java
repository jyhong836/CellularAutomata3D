package com.cellular3d;

import java.io.IOException;

import com.cellular3d.dots3d.grid.CAComputationKernel;
import com.cellular3d.dots3d.grid.CellularAutomataGrid;
import com.cellular3d.dots3d.grid.ComputationServer;
import com.cellular3d.dots3d.grid.GridDot;
import com.cellular3d.dots3d.grid.GridPoints;

public class CellularAutomata3DServer implements Runnable {
	
	Thread computeThread;
	Thread socketThread;
	
//	String host = "127.0.0.1";//"0.0.0.0";
	int port = 8000;
	
	boolean runComputeThread = true;
	boolean runSocketThread = true;
	
	int socketSendCount = 0;

	CAComputationKernel caKernel;
//	CellularAutomataGrid cag;
	int   gridsize;
	float boxscale;
	int xsize;
	int ysize;
	int zsize;
	GridDot[][][] gridptr;
	GridPoints gridPoints;

	/**
	 * Main method for server application
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("* Cellular Automata 3D Server *");
		CellularAutomata3DServer cas = new CellularAutomata3DServer();
	}

	/**
	 * constructor
	 */
	public CellularAutomata3DServer() {
		boxscale = .3f;
		gridsize = 50;
		caKernel = new CellularAutomataGrid(gridsize);
		caKernel.init();
		xsize = caKernel.getXSize();
		ysize = caKernel.getYSize();
		zsize = caKernel.getZSize();
		gridptr = new GridDot[xsize][ysize][zsize];
		gridPoints = new GridPoints(xsize, ysize, zsize, boxscale, boxscale, boxscale);

		computeThread = new Thread(this);
		computeThread.start();
		computeThread.suspend();
		socketThread = new Thread(this);
		socketThread.start();
	}
	
	/**
	 * This loop is used for computation
	 */
	private void computeLoop() {
		long msec = 0;
		
		while (runComputeThread) {
			msec = System.currentTimeMillis();
			caKernel.update();
			caKernel.updatePointsNum();
			gridptr = caKernel.getGridPtr();
			synchronized (gridPoints) {
				gridPoints.setValue(gridptr, caKernel.getPointsNum());
			}
			System.out.println("CA FPS:"+(1000.0/(System.currentTimeMillis() - msec)));
		}
	}
	
	/**
	 * This loop is used for socket connection
	 */
	private void socketLoop() {
		ComputationServer cs = new ComputationServer(port);
		System.out.println("socket thread: init server socket at port:"+port);
		if (!cs.initServer()) {
			System.out.println("exit program");
			System.exit(-1);
		}
		cs.waitClient();
		
		System.out.println("start socket thread");
		boolean status = false;;
		String msg = null;
		while (runSocketThread) {
			try {
				msg = cs.waitMessage();
				if (msg==null)
					continue;
			} catch (IOException e) {
				System.out.println(e.getMessage());
				continue;
			}
			if (msg.equals("RequestGridPoints")) {
				System.out.println("prepare send data");
				synchronized (gridPoints) {
					// FIXME Test it, if we can pass the class include array directly.
					if (gridPoints.isUpdated()) {
						status = cs.sendGridPointsArray(gridPoints);
						socketSendCount++;
						System.out.println("send pack, index:"+socketSendCount);
					} else 
						System.out.println("data has not been updated");
				}
			}
			if (msg.equals("GoodBye")) {
				// TODO 建立通信机制，这里需要进行信息交流确认连接正常
				cs.waitClient();
			}
		}
	}

	@Override
	public void run() {
		if (Thread.currentThread().equals(computeThread)) {
			computeLoop();
		} else if (Thread.currentThread().equals(socketThread)) {
			socketLoop();
		}
	}

}
