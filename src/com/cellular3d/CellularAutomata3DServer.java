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
	boolean stopComputeThread = true;
	
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
		stopComputeThread = true;
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
				gridPoints.notifyAll();
			}
//			System.out.println("CA FPS:"+(1000.0/(System.currentTimeMillis() - msec)));
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
				System.out.println("wait client request...");
				msg = cs.waitMessage();
				if (msg==null)
					continue;
			} catch (IOException e) {
				System.err.println(e.getMessage());
				if (e.getMessage().equals("Connection reset")) {
					cs.waitClient();
				}
				continue;
			}
			if (msg.equals("RequestGridPoints")) {
				System.out.println("prepare send data");
				try {
					boolean updateStat = false;
					synchronized (gridPoints) {
						updateStat = gridPoints.isUpdated();
					}
					if (updateStat) {
						
						synchronized (gridPoints) {
							cs.sendMessage("GridPointsDataReady");
							
							/* send data */
							status = cs.sendGridPointsArray(gridPoints);
							gridPoints.clearUpdateFlag();
							socketSendCount++;
							System.out.println("* send GridPoints, index:"+socketSendCount);
							System.out.println(" |- GridPoints.updatecount:"+gridPoints.getUpdateCount());
							System.out.println(" |- GridPoints.pointsNum:  "+gridPoints.pointsNum);
						}
						
					} else if (!stopComputeThread) {

						System.out.println("data has not been updated");
						cs.sendMessage("GridPointsDataNotReady");
						msg = cs.waitMessage();
						if (msg.equals("WaitUntilGridPointsUpdate")) {
							waitUpdate(updateStat); // wait until data is ready.
							cs.sendMessage("GridPointsDataReady"); // tell client "Ready", but not send data
						} // XXX else go through??
						
					} else if (stopComputeThread) {
						
						cs.sendMessage("ComputeThreadStoped");
						msg = cs.waitMessage();
						if (msg.equals("StartComputeThread")) {
							stopComputeThread = !stopComputeThread;
							computeThread.resume(); // start thread
							cs.sendMessage("StartComputeThreadOK");
						}
						
					}
				} catch (IOException e) {
					System.err.println("**"+e.getMessage());
					e.printStackTrace();
				} catch (InterruptedException e) {
					System.err.println("**"+e.getMessage());
					e.printStackTrace();
				}
			} else if (msg.equals("GoodBye")) {
				// TODO 建立通信机制，这里需要进行信息交流确认连接正常
				cs.waitClient(); // this method will close socket also.
			}
//			else if (msg.equals("StartComputation")) {
//				computeThread.resume();
//			} else if (msg.equals("StopComputation")) {
//				computeThread.suspend();
//			}
		}
	}
	
	/**
	 * Wait until GridPoints is updated
	 * @throws InterruptedException
	 */
	private void waitUpdate(boolean updateStat) throws InterruptedException {
		int cout = 0;
		while (!updateStat) {
			System.out.println("["+cout+"] data is not ready, waiting...");
			synchronized (gridPoints) {
				gridPoints.wait();
				System.out.println("check if data is ready...");
				cout++;
				updateStat = gridPoints.isUpdated();
			}
		}
		System.out.println("data is ready");
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
