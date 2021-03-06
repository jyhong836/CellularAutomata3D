package com.cellular3d.dots3d.kernel;

import java.io.IOException;

import com.cellular3d.dots3d.grid.GridDot;
import com.cellular3d.dots3d.grid.GridPoints;

/**
 * The interface of Cellular Automata Compation Kernel. The subclass include
 * {@linkplain com.cellular3d.dots3d.kernel.CellularAutomataGrid CellularAutomataGrid} 
 * and {@linkplain com.cellular3d.dots3d.kernel.ComputationClient ComputationClient}}
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014��11��12��
 * 
 */
public interface CAComputationKernel {
	
	public boolean initialized();
	
	public boolean init();
	
	public int updatePointsNum();
	
	public int getPointsNum();
	
	public boolean update() throws IOException;
	
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
	
	public int getCount();

}
