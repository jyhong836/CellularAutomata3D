package com.cellular3d;

import java.io.IOException;

import com.cellular3d.dots3d.grid.CAComputationKernel;
import com.cellular3d.dots3d.grid.CellularAutomataGrid;
import com.cellular3d.dots3d.grid.ComputationServer;
import com.cellular3d.dots3d.grid.GridDot;
import com.cellular3d.dots3d.grid.GridPoints;
import com.cellular3d.dots3d.grid.UnexpectedMessageException;
import com.cellular3d.dots3d.grid.UnknownMessageException;

public class CellularAutomata3DServer implements Runnable {
	
	Thread computeThread; // Thread for computation.
	Thread socketThread;  // Thread for socket communication.
	
//	String host = "127.0.0.1";//"0.0.0.0";
	int port = 8000;
	
	boolean runComputeThread = true;
	boolean runSocketThread = true;
	boolean stopComputeThread = true;
	
	int socketSendCount = 0;

	CAComputationKernel caKernel;
	
	int   gridsize;
	float boxscale;
	int xsize;
	int ysize;
	int zsize;
	
	private GridDot[][][] gridptr;
	private GridPoints gridPoints;
	/**
	 * GridPoints2 is the buffer of gridPoints, used for socket thread.
	 * This variable is designed to avoid the blocking of computeThread and 
	 * SocketThread.
	 */
	private GridPoints gridPoints2;

	
	/**
	 * Main method for server application
	 * @param args
	 */
	public static void main(String[] args) {
		float boxscale = .3f;
		int gridsize = 50;
		
		System.out.println("* Cellular Automata 3D Server *");
		CellularAutomata3DServer cas = new CellularAutomata3DServer(gridsize, boxscale);
		cas.startThread();
	}

	
	/**
	 * 
	 * @param gridsize set Grids size to [gridsize][gridsize][gridsize]
	 * @param boxscale the width, depth and height will be set equal to 
	 * boxscale
	 */
	public CellularAutomata3DServer(int gridsize, float boxscale) {
		
		this.boxscale = boxscale;
		this.gridsize = gridsize;
		
		caKernel = new CellularAutomataGrid(gridsize);
		caKernel.init();
		xsize = caKernel.getXSize();
		ysize = caKernel.getYSize();
		zsize = caKernel.getZSize();
		gridptr = new GridDot[xsize][ysize][zsize];
		gridPoints = new GridPoints(gridsize, boxscale);
		gridPoints2 = new GridPoints(gridsize, boxscale);

	}
	
	/**
	 * Start the computation thread and socket thread.
	 */
	public void startThread() {

		computeThread = new Thread(this);
		computeThread.start();
		computeThread.suspend();
		stopComputeThread = true;
		
		socketThread = new Thread(this);
		socketThread.start();
		
	}
	
	/**
	 * This loop is used for computation
	 * 
	 * FIXME 已知的问题，在进行计算的过程中强行中断连接会导致问题，原因是同步没有做好，中断刚好发生在数据交换的过程中。
	 */
	private void computeLoop() {
		
		while (runComputeThread) {
			try {
				caKernel.update();
			} catch (IOException e) {
				System.out.println("Unknown error. exit...");
				System.exit(-1);
			}
			caKernel.updatePointsNum();
			gridptr = caKernel.getGridPtr();
			synchronized (gridPoints) { 
				gridPoints.setValue(gridptr, caKernel.getPointsNum(), caKernel.getCount());
				gridPoints.notifyAll();
			}
//			System.out.println("[Computing] pointsNum:"+gridPoints.pointsNum+" Count:"+gridPoints.getUpdateCount());
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
		
		cs.waitClient(); // wait client to connect
		
		System.out.println("start socket thread");
		boolean status = false;;
		String msg = null;
		while (runSocketThread) { // socket thread
			
			try {
				
				System.out.println("wait client request...");
				
				msg = cs.waitMessage(); // Get Message from client
				
				if (msg==null)
					continue;
				
			} catch (IOException e) { // IOException
				
				System.err.println(e.getMessage());
				if (e.getMessage().equals("Connection reset") || e.getMessage().equals("socket closed")) {
					
					computeThread.suspend();
					cs.waitClient();
					
				} else if (e.getMessage().equals("Read timed out")) {
					
					try {
						cs.clearBuf();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
				} else {
					
					e.printStackTrace();
					System.exit(-1);
					
				}
				continue;
				
			}
			
			/* process the received message */
			try {
				
				if (msg.equals("RequestGridPoints")) {
					
					// Client request the gridpoints data
					System.out.println("prepare for sending data");
					try {
						boolean updateStat = false;
						synchronized (gridPoints) {
							updateStat = gridPoints.isUpdated(); // check if the data is updated
						}
						if (updateStat) {
							
							synchronized (gridPoints) {
								gridPoints2.copy(gridPoints); // copy the data to buffer
							}
							
							synchronized (gridPoints2) {
								cs.sendMessage("GridPointsDataReady"); // tell the client data is ready
								
								/* send data */
								status = cs.sendGridPointsArray(gridPoints);
								gridPoints.clearUpdateFlag();
								
								/* print some message */
								socketSendCount++;
								System.out.println("* send GridPoints, index:"+socketSendCount);
								System.out.println(" |- GridPoints.updatecount:"+gridPoints.getUpdateCount());
								System.out.println(" |- GridPoints.pointsNum:  "+gridPoints.pointsNum);
							}
							
						} else { // Data has not been updated
							
							if (!stopComputeThread) {
	
								System.out.println("data has not been updated");
								cs.sendMessage("GridPointsDataNotReady");
								msg = cs.waitMessage();
								if (msg.equals("WaitUntilGridPointsUpdate")) {
									waitUpdate(updateStat); // wait until data is ready.
									cs.sendMessage("GridPointsDataReady"); // tell client "Ready", but not send data
								} else 
									throw new UnexpectedMessageException("Expected WaitUntilGridPointsUpdate but get "+msg);
								
							} else { // computation thread has been stopped, DEFAULT start it.
								
								cs.sendMessage("ComputeThreadStoped"); // tell client
								msg = cs.waitMessage();
								if (msg.equals("StartComputeThread")) {  
									// FIXME This thread will not start unless the client start client thread loop. 
									stopComputeThread = !stopComputeThread;
									computeThread.resume(); // start thread
									cs.sendMessage("StartComputeThreadOK");
								} else
									throw new UnexpectedMessageException("Expected StartComputeThread but get"+msg);
								
							}
							
						}
					} catch (IOException e) {
						System.err.println("**"+e.getMessage());
						e.printStackTrace();
						System.exit(-1); // FORCE EXIT
					} catch (InterruptedException e) {
						System.err.println("**"+e.getMessage());
						e.printStackTrace();
						System.exit(-1); // FORCE EXIT
					}
					
				} else if (msg.equals("GoodBye")) {
					
					// TODO 建立通信机制，这里需要进行信息交流确认连接正常
					computeThread.suspend();
					cs.clearBuf();
					cs.waitClient(); // this method will close socket also.
					
				} else if(msg.equals("RequestClearBuffer")) {
					
					cs.clearBuf();
					
				} else 
					throw new UnknownMessageException(msg);
				
			} catch (IOException e1) { 
				
				e1.printStackTrace();
				System.exit(-1); // FORCE EXIT
				
			} catch (UnexpectedMessageException e1) {
				
				System.out.println("[MSG] UnexpectedMessageException:"+e1.getMessage());
				try {
					cs.clearBuf(); // clear buffer
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1); // FORCE EXIT
				}
				
			} catch (UnknownMessageException e1) {
				
				System.out.println("[MSG] UnknownMessageException:"+e1.getMessage());
				try {
					cs.clearBuf(); // clear buffer
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(-1); // FORCE EXIT
				}
				
			}
			
//			else if (msg.equals("StartComputation")) {
//				computeThread.resume();
//			} else if (msg.equals("StopComputation")) {
//				computeThread.suspend();
//			}
		}
	}
	
	/**
	 * Blocking until GridPoints is updated
	 * @throws InterruptedException
	 */
	private void waitUpdate(boolean updateStat) throws InterruptedException {
		
		int cout = 0;
		while (!updateStat) {
			System.out.println("["+cout+"] data is not ready, waiting...");
			synchronized (gridPoints) {
				gridPoints.wait(10000); // max wait time
				System.out.println("check if data is ready...");
				cout++;
				updateStat = gridPoints.isUpdated();
			}
			if (cout>10)
			{
				System.out.println("ERROR: the computation can not be updated, exit...");
				System.exit(-1);
			}
		}
		System.out.println("data is ready");
		
	}

	@Override
	public void run() {
		
		if (Thread.currentThread().equals(computeThread)) {
			
			computeLoop(); // computation
			
		} else if (Thread.currentThread().equals(socketThread)) {
			
			socketLoop();  // socket
			
		}
		
	}

}

