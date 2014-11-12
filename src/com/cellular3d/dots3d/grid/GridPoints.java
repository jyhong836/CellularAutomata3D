package com.cellular3d.dots3d.grid;

public class GridPoints {
	
	public Point4 [] points = null;
	
	public int xsize, ysize, zsize;
	public float width, depth, height;
	public int pointsNum;
	public int count;
	
	private boolean isUpdated = false;

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
		count = 0;
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
	}
	
	public boolean isUpdated() {
		return isUpdated;
	}

}
