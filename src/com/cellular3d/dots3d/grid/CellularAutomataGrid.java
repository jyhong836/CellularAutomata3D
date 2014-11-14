package com.cellular3d.dots3d.grid;

/**
 * CellularAutomataGrid is a computation only Cellular Automata and a local
 * Computation Kernel.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014��11��11��
 *
 */
public class CellularAutomataGrid implements CAComputationKernel {
	
	boolean colorMod = false; // TODO: Add colorMod change code
	
	private int pointsNum = 100;
	private boolean initialized = false;
	private int updateCount = 0;
	
//	private int size  = 50;
	private int xsize = 50;
	private int ysize = 50;
	private int zsize = 50;
	
	private GridDot[][][][] grid; 
	private int             gridIndex = 0;
	private GridDot[][][]   gridptr;
	private int             gridIndexBuff = 1;
	private GridDot[][][]   gridPtrBuff;
	
//	private float width  = .8f;
//	private float depth  = .8f;
//	private float height = .8f;
	private float initAirDensity = .1f;
	
	private double rotXAngle = .0f;
	
//	CellularAutomata3D parentApplet = null;
	
	/**
	 * constructor
	 * @param size the xsize, depth and height will be set to scale*2.
	 * @param parentApplet the Applet will be used to display status.
	 */
	public CellularAutomataGrid(int size) {
		this(size, size, size);
	}

	/**
	 * constructor
//	 * @param width
//	 * @param depth
//	 * @param height
	 * @param parentApplet the Applet will be used to display status.
	 */
	public CellularAutomataGrid(int xsize, int ysize, int zsize) {
		
		this.xsize = xsize;
		this.ysize = ysize;
		this.zsize = zsize;
		
		/* initialize the memory of grid */
		pointsNum = 0;
		grid = new GridDot[2][xsize][ysize][zsize];
		gridptr = grid[gridIndex];
		gridPtrBuff = grid[gridIndexBuff];
		
		// initialize grid dots
//		initGridDots();
	}
	
	@Override
	public boolean init() {
		initGridDots();
		initialized = true;
		return true;
	} 
	
	private void initGridDots() {

		System.out.println("init GridDots...");
		/* --------- random air grids ----------- */
		System.out.print(" init random air grids... memory...");
		long msec = System.currentTimeMillis();
		
		// init memory
		int[][] mass;
		for (int i = 0; i < xsize; i++)
			for (int j = 0; j < ysize; j++)
				for (int k = 0; k < zsize; k++) {
					grid[0][i][j][k] = new GridDot();
					grid[1][i][j][k] = new GridDot();
				}
		System.out.print("ok...");
		// random dots
		for (int i = 1; i < xsize-1; i++) {
			for (int j = 1; j < ysize-1; j++) {
				for (int k = 1; k < zsize-1; k++) {
					if (Math.random()<initAirDensity) {
						mass = gridptr[i][j][k].mass;
						// choose one direction add particle
						mass[(int)Math.floor(Math.random()*3)][(Math.random()>0.5)?1:0] = 1;
						pointsNum++;
						gridptr[i][j][k].count = 1;
						gridptr[i][j][k].value = 1;
					}
				}
			}
		}
		/* ------ process bounds ------- */
		for (int i = 0;i<xsize;i+=xsize-1)
			for (int j = 1; j < ysize-1; j++)
				for (int k = 1; k < zsize-1; k++) {
					grid[0][i][j][k].setUntouchable();
					grid[1][i][j][k].setUntouchable();
				}

		for (int i = 1;i<xsize-1;i++)
			for (int j = 0; j < ysize; j+=ysize-1)
				for (int k = 1; k < zsize-1; k++) {
					grid[0][i][j][k].setUntouchable();
					grid[1][i][j][k].setUntouchable();
				}

		for (int i = 1;i<xsize-1;i++)
			for (int j = 1; j < ysize-1; j++)
				for (int k = 0; k < zsize; k+=zsize-1) {
					grid[0][i][j][k].setUntouchable();
					grid[1][i][j][k].setUntouchable();
				}
		
		System.out.println(" success in " + (System.currentTimeMillis() - msec) + " ms");
		/* --------- density cloud ----------- */
		System.out.print(" init density cloud...");
		msec = System.currentTimeMillis();
		
		for (int i = xsize/2-10; i < xsize/2+10; i++) {
			for (int j = ysize/2-10; j < ysize/2+10; j++) {
				for (int k = zsize/2-10; k < zsize/2+10; k++) {
					if (gridptr[i][j][k].count<=0)
						pointsNum++;
					mass = gridptr[i][j][k].mass;
//					mass[(int)Math.floor(Math.random()*3)][(Math.random()>0.5)?1:0] = 1;
					for (int k2 = 0; k2 < mass.length; k2++) {
						for (int l = 0; l < mass[0].length; l++) {
							mass[k2][l] = 1;
						}
					}
					gridptr[i][j][k].update();
				}
			}
		}
		
		System.out.println(" success in " + (System.currentTimeMillis() - msec) + " ms");

		System.out.println("init GridDots success");
		
	}
	
//	public double getRotXAngle() {
//		return rotXAngle;
//	}
//	
//	public void setRotXAngle(double rotXAngle) {
//		this.rotXAngle = rotXAngle;
//	}
	
	public void updateDots() {
		
		gridIndexBuff = (gridIndex>0)? 0:1;
		gridPtrBuff = grid[gridIndexBuff];
		
		int[][] mass;
		int[]   countLine;
		for (int i = 0; i < xsize; i++) 
		{
			for (int j = 0; j < ysize; j++) 
			{
				for (int k = 0; k < zsize; k++) 
				{
					
					mass = gridptr[i][j][k].mass;
					countLine = gridptr[i][j][k].countLine;
					
					// check gridProperty
					if (processUntouchableGrid(mass, i, j, k))
						;
					else if (gridptr[i][j][k].gridProperty == 0) {
						
						// direction in x axis
						processXDirectionGrid(mass, countLine, i, j, k);
						
						// direction in y axis
						processYDirectionGrid(mass, countLine, i, j, k);
						// direction in z axis
						processZDirectionGrid(mass, countLine, i, j, k);
						
					} // if gridptr[i][j][k].gridProperty == 0
					
				} // k
			} // j
		} // i

		/* -------- update points -------- */
		for (int i = 0; i < xsize; i++) {
			for (int j = 0; j < ysize; j++) {
				for (int k = 0; k < zsize; k++) {
					gridPtrBuff[i][j][k].update();
					gridptr[i][j][k].clear();
				}
			}
		}
		
		gridIndex = gridIndexBuff;
		gridptr = grid[gridIndex];// gridPtrBuff;
		
	}
	
	/**
	 * Check and Process untouchable grid
	 * @param mass
	 * @param i
	 * @param j
	 * @param k
	 * @return if is untouchable, then return true.
	 */
	private boolean processUntouchableGrid(int[][] mass, int i, int j, int k ) {
		if (gridptr[i][j][k].gridProperty < 0) {
			// untouchable, then reflect
			if (mass[0][0] > 0)
				gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][0];
			if (mass[0][1] > 0)
				gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][1];
			if (mass[1][0] > 0)
				gridPtrBuff[i][j+1][k].mass[1][1] = mass[1][0];
			if (mass[1][1] > 0)
				gridPtrBuff[i][j-1][k].mass[1][0] = mass[1][1];
			if (mass[2][0] > 0)
				gridPtrBuff[i][j][k+1].mass[2][1] = mass[2][0];
			if (mass[2][1] > 0)
				gridPtrBuff[i][j][k-1].mass[2][0] = mass[2][1];
			return true;
		} else 
			return false;
	}
	
	private void processXDirectionGrid(int[][] mass, int[] countLine, int i, int j, int k) {
		if (mass[0][0]>0) { // come from +x direction
			if (mass[0][1]>0) { // from -x
				switch (countLine[1]) {
				case 0:
					gridPtrBuff[i][j-1][k].mass[1][0] = mass[0][0];
					gridPtrBuff[i][j+1][k].mass[1][1] = mass[0][1];
					break;
				case 1:
					gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
					gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
					break;
				case 2:
					if (countLine[2]!=1) { // same to case 0
						gridPtrBuff[i][j-1][k].mass[1][0] = mass[0][0];
						gridPtrBuff[i][j+1][k].mass[1][1] = mass[0][1];
					} else { // same to case 1
						gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
						gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
					}
					break;

				default:
					break;
				}
			} else {
				try {
					gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
				} catch (ArrayIndexOutOfBoundsException e) {
					System.out.println(" "+gridptr[i][j][k].gridProperty);
				}
			}
		}
		else if (mass[0][1]>0) { // come / from -x
			gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
		}
	}

	private void processYDirectionGrid(int[][] mass, int[] countLine, int i,
			int j, int k) {
		if (mass[1][0]>0) { // come from +y direction
			if (mass[1][1]>0) { // from -y
				switch (countLine[2]) {
				case 0:
					gridPtrBuff[i][j][k-1].mass[2][0] = mass[1][0];
					gridPtrBuff[i][j][k+1].mass[2][1] = mass[1][1];
					break;
				case 1:
					gridPtrBuff[i][j-1][k].mass[1][0] = mass[1][0];
					gridPtrBuff[i][j+1][k].mass[1][1] = mass[1][1];
					break;
				case 2:
					if (countLine[0]!=1) { // same to case 0
						gridPtrBuff[i][j][k-1].mass[2][0] = mass[1][0];
						gridPtrBuff[i][j][k+1].mass[2][1] = mass[1][1];
					} else { // same to case 1
						gridPtrBuff[i][j-1][k].mass[1][0] = mass[1][0];
						gridPtrBuff[i][j+1][k].mass[1][1] = mass[1][1];
					}
					break;

				default:
					break;
				}
			} else {
				gridPtrBuff[i][j-1][k].mass[1][0] = mass[1][0];
			}
		}
		else if (mass[1][1]>0) { // come from -x
			gridPtrBuff[i][j+1][k].mass[1][1] = mass[1][1];
		}
		
	}
	
	private void processZDirectionGrid(int[][] mass, int[] countLine, int i,
			int j, int k) {
		if (mass[2][0]>0) { // come from +z direction
			if (mass[2][1]>0) { // from -z
				switch (countLine[0]) {
				case 0:
					gridPtrBuff[i-1][j][k].mass[0][0] = mass[2][0];
					gridPtrBuff[i+1][j][k].mass[0][1] = mass[2][1];
					break;
				case 1:
					gridPtrBuff[i][j][k-1].mass[2][0] = mass[2][0];
					gridPtrBuff[i][j][k+1].mass[2][1] = mass[2][1];
					break;
				case 2:
					if (countLine[1]!=1) { // same to case 0
						gridPtrBuff[i-1][j][k].mass[0][0] = mass[2][0];
						gridPtrBuff[i+1][j][k].mass[0][1] = mass[2][1];
					} else { // same to case 1
						gridPtrBuff[i][j][k-1].mass[2][0] = mass[2][0];
						gridPtrBuff[i][j][k+1].mass[2][1] = mass[2][1];
					}
					break;

				default:
					break;
				}
			} else {
				gridPtrBuff[i][j][k-1].mass[2][0] = mass[2][0];
			}
		}
		else if (mass[2][1]>0) { // come from -x
			gridPtrBuff[i][j][k+1].mass[2][1] = mass[2][1];
		}
		
	}
	
	public long getRequiredMemory() {
		return xsize*ysize*zsize*700;
	}
	
	public int getXSize() {
		return xsize;
	}
	
	public int getYSize() {
		return ysize;
	}
	
	public int getZSize() {
		return zsize;
	}

	/**
	 * Get the latest version of gridptr.
	 * @return GridDot[][][] gridptr
	 */
	@Override
	public GridDot[][][] getGridPtr() {
		return gridptr;
	}
	
	/**
	 * Get the generate number of grids which have been occupied.
	 * @return int pointsNum
	 */
	public int getPointsNum() {
		return pointsNum;
	}
	
	/**
	 * Update and return pointsNum
	 * @return int pointsNum
	 */
	public int updatePointsNum() {

		pointsNum = 0;
		for (int i = 0; i < gridptr.length; i++)
			for (int j = 0; j < gridptr[0].length; j++)
				for (int k = 0; k < gridptr[0][0].length; k++)
					if (gridptr[i][j][k].count>0)
						pointsNum++;
		return pointsNum;
		
	}

	@Override
	public boolean update() {
		
		this.updateDots();
		updateCount++;
		return true;
		
	}

	/**
	 * 
	 */
	@Override
	public GridPoints getGridPoints() {
		return null;
	}
	
	@Override
	public boolean initialized() {
//		if (pointsNum==0)
//			return false;
//		else 
//			return true;
		return initialized;
	}

	@Override
	public int getCount() {
		return updateCount;
	}

}

