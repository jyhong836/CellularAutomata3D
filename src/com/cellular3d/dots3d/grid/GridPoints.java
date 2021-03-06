package com.cellular3d.dots3d.grid;

import java.io.Serializable;

/**
 * GridPoints is a Serializable object, including Point4 array and other 
 * necessary data.
 * 
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014��11��14��
 *
 */
public class GridPoints implements Serializable {

	/**
	 * generated UID
	 */
	private static final long serialVersionUID = -5187507432229345457L;

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
	
	public void copy(GridPoints gridPoints) {
		this.xsize = gridPoints.xsize;
		this.ysize = gridPoints.ysize;
		this.zsize = gridPoints.zsize;
		this.width = gridPoints.width;
		this.depth = gridPoints.depth;
		this.height = gridPoints.height;
		this.pointsNum = gridPoints.pointsNum;
		this.updateCount = gridPoints.getUpdateCount();
		this.points = new Point4[pointsNum];
		for (int i = 0; i < gridPoints.points.length; i++) {
			this.points[i] = gridPoints.points[i];
		}
	}
	
	public void setValue(GridDot[][][] gridptr,
			int pointsNum, int updateCount) {
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
		this.updateCount = updateCount;
//		System.out.println("pointsNum:"+pointsNum+" Count:"+count);
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
