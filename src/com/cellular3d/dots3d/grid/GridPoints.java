package com.cellular3d.dots3d.grid;

import java.io.Serializable;

public class GridPoints implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1308053840893751234L;

	public Point4 [] points = null;
	
	public int xsize, ysize, zsize;
	public float width, depth, height;
	public int pointsNum;
	
	private boolean isUpdated = false;
	private int updateCount = 0;
	
	public GridPoints(int size, float scale) {
		this(size, size, size, scale*2, scale*2, scale*2);
	}

	public GridPoints(int xsize, int ysize, int zsize, 
			float width, float depth, float height) {
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
		this.width = width;
		this.depth = depth;
		this.height = height;
	}
	
	public void setValue(GridDot[][][] gridptr,
			int pointsNum) {
		int count = 0;
		this.pointsNum = pointsNum;
		this.points = new Point4[pointsNum];
		for (int i = 0; i < xsize; i++)
			for (int j = 0; j < ysize; j++)
				for (int k = 0; k < zsize; k++)
					if (gridptr[i][j][k].value>0) {
						points[count] = new Point4(width * i/xsize - width/2 , depth * j/ysize - depth/2 , height * k/zsize - height/2, 
								gridptr[i][j][k].value);
						count++;
					}
		isUpdated = true;
		updateCount++;
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}
	
	public void clearUpdateFlag() { 
		isUpdated = false;
	}
	
	public int getUpdateCount() {
		return updateCount;
	}
	
	public void clear() {
		this.isUpdated = false;
		this.updateCount = 0;
	}

}
