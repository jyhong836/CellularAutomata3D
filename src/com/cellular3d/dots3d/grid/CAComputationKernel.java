package com.cellular3d.dots3d.grid;

/**
 * The interface of Cellular Automata Compation Kernel. The subclass include
 * {@linkplain com.cellular3d.dots3d.grid.CellularAutomataGrid CellularAutomataGrid} 
 * and {@linkplain com.cellular3d.dots3d.grid.ComputationClient ComputationClient}}
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014Äê11ÔÂ12ÈÕ
 * 
 */
public interface CAComputationKernel {
	
	public boolean initialized();
	
	public boolean init();
	
	public int updatePointsNum();
	
	public int getPointsNum();
	
	public boolean update();
	
	/**
	 * This method does not always work.
	 * @return null, if not support this method, try getGridPoints
	 */
	public GridDot[][][] getGridPtr();

	/**
	 * This method does not always work.
	 * @return null, if not support this method, try 
	 * {@linkplain com.cellular3d.dots3d.grid.CAComputationKernel.getGridPtr getGridPtr}
	 */
	public GridPoints getGridPoints();
	
	public long getRequiredMemory();
	
	public int getXSize();
	
	public int getYSize();
	
	public int getZSize();
	
//	public void setSocket(String host, int port);
	
//	public boolean closeSocket();
	
//	public String toString();

}
