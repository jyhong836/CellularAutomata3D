package com.cellular3d.dots3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;

import com.cellular3d.CellularAutomata3DApplet;
import com.cellular3d.dots3d.grid.CAComputationKernel;
import com.cellular3d.dots3d.grid.CellularAutomataGrid;
import com.cellular3d.dots3d.grid.ComputationClient;
import com.cellular3d.dots3d.grid.GridDot;
import com.cellular3d.dots3d.grid.GridPoints;
import com.cellular3d.dots3d.grid.Point4;
import com.sun.j3d.utils.geometry.Box;

/**
 * Dots model object in 3D mode. Every dot is a computation point, which may not include 
 * one particle.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014Äê11ÔÂ10ÈÕ
 *
 */
public class Dots3DShape extends BranchGroup {
	
	boolean colorMod = false; // TODO: Add colorMod change code
	PointArray pla;
	int pointsArrayMod = GeometryArray.COORDINATES | GeometryArray.COLOR_4;
	Shape3D plShape;
	
	TransformGroup objRotate;
	
	private int pointsNum = 100;
	
//	private int size  = 100;
	private int xsize;
	private int ysize;
	private int zsize;
	
	CAComputationKernel caKernel;
//	CellularAutomataGrid cag;
	private GridDot[][][]   gridptr    = null; // when use remote kernel this will be null
	private GridPoints      gridPoints = null; // when use local kernel or socket error, this will be null
	
	private float width  = .8f;
	private float depth  = .8f;
	private float height = .8f;
//	private float initAirDensity = .1f;
	
	Box box;
	Transform3D t3;
	private double rotXAngle = .0f;
	
	CellularAutomata3DApplet parentApplet = null;
	
	/**
	 * constructor
	 * @param scale the width, depth and height will be set to scale*2.
	 * @param parentApplet the Applet will be used to display status.
	 */
	public Dots3DShape(float scale, int size, CellularAutomata3DApplet parentApplet) {
		this(scale*2, scale*2, scale*2, size, parentApplet);
	}

	/**
	 * constructor
	 * @param width
	 * @param depth
	 * @param height
	 * @param size the xsize, ysize and zsize will be set the same as size.
	 * @param parentApplet the Applet will be used to display status.
	 */
	public Dots3DShape(float width, float depth, float height, 
			int size, CellularAutomata3DApplet parentApplet) {
		
		this.parentApplet = parentApplet;
		
		this.width = width;
		this.depth = depth;
		this.height = height;
		this.xsize = size;
		this.ysize = size;
		this.zsize = size;

		/* box */
		Appearance boxapp = new Appearance();
		if (boxapp!=null)
			boxapp.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_BACK,0));
		box = new Box(width/2, depth/2, height/2, boxapp);
		
		/* default use local kernel */
		this.setLocalKernel(); // TODO if change to remote kernel, should init the gridPoints
		
		/* initialize 3D Shape */
		System.out.println("init 3D shape and points...");
		long msec = System.currentTimeMillis();

		/* initialize Appearance and set color mode */
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0f, 1.0f, 1.0f), ColoringAttributes.FASTEST);
        app.setColoringAttributes(ca);

        System.out.println(" points number: "+ pointsNum);

        /* create Shape3D and add to BranchGroup */
        plShape = new Shape3D(null, app);
        plShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        
		t3 = new Transform3D(); // init t3
       	objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.addChild(plShape);
        objRotate.addChild(box);
        this.addChild(objRotate);
        
        /* update points */
        this.updatePoints();
        
        System.out.println("success in " + (System.currentTimeMillis() - msec) + " ms");
	}
	
	
	public void rotX(double angle) {
		rotXAngle += angle;
		t3.rotX(rotXAngle);
		objRotate.setTransform(t3);
	}
	
	public double getRotXAngle() {
		return rotXAngle;
	}
	
	public void setRotXAngle(double rotXAngle) {
		this.rotXAngle = rotXAngle;
	}
	
	public String currentKernelString() {
		return caKernel.getClass().getSimpleName();
	}
	
	/**
	 * Use the local computation kernel 
	 * ({@linkplain com.cellular3d.dots3d.grid.ComputationClient.CellularAutomataGrid CellularAutomataGrid}
	 * ) as the CAComputationKernel.
	 */
	public void setLocalKernel() {

		this.displayStatus("init local kernel...");
		System.out.println("* init local kernel...");
		/* intialize CellularAutomataGrid */
		caKernel = new CellularAutomataGrid(xsize, ysize, zsize);
		caKernel.init();
		gridptr = caKernel.getGridPtr();
		this.pointsNum = caKernel.getPointsNum();
		gridPoints = null;

		System.out.println("* local kernel ready");
		this.displayStatus("local kernel ready");
		
	}
	
	/**
	 * Use the remote computation kernel 
	 * ({@linkplain com.cellular3d.dots3d.grid.ComputationClient.ComputationClient ComputationClient}
	 * ) as the CAComputationKernel.
	 */
	public void setRemoteKernel() {
		this.caKernel = new ComputationClient(this.xsize, this.ysize, this.zsize,
				this.width, this.depth, this.height);
		gridPoints = caKernel.getGridPoints();
		this.pointsNum = caKernel.getPointsNum();
		gridptr = null;
		
		displayStatus("use remote kernel, not init");
	}
	
	public boolean connectRemoteKernel(String host, int port) {
		caKernel.setSocket(host, port);
		if (caKernel.init()) {
			displayStatus("connect remote kernel success");
			return true;
		} else {
			displayStatus("connect remote kernel failed");
			return false;
		}
	}
	
	public boolean disconnectRemoteKernel() {
//		caKernel.setSocket(host, port);
		if (caKernel.closeSocket()) {
			displayStatus("disconnect remote kernel success");
			return true;
		} else {
			displayStatus("disconnect remote kernel failed");
			return false;
		}
	}
	
	/**
	 * <p>Update dots in CA grid with the method of CellularAutomataGrid. And update points
	 * of 3D Shape at the same time.</p>
	 * <p>This method should be called every time it's need to update dots in the 3D box.</p>
	 */
	public void updateDots() {
		if (caKernel.update()) {
			this.updatePoints();
		} else {
			
		}
			
	}

	/**
	 * This can be used for both getGridPtr and getGridPoints
	 */
	public void updatePoints() {
		
		// update gridptr
		gridptr = caKernel.getGridPtr();
		
		pointsNum = caKernel.updatePointsNum();
		
        Point3f[] plaPts = new Point3f[pointsNum];
        Color4f[] plaCls = new Color4f[pointsNum];

        if (gridptr!=null) {
	        int count = 0;
	        float color = 0f;
			for (int i = 0; i < gridptr.length; i++)
				for (int j = 0; j < gridptr[0].length; j++)
					for (int k = 0; k < gridptr[0][0].length; k++) {
						if (gridptr[i][j][k].count>0) {
							plaPts[count] = new Point3f(width * i/xsize - width/2 , depth * j/ysize - depth/2 , height * k/zsize - height/2);
							color = gridptr[i][j][k].value/4.f;
							plaCls[count] = new Color4f(0, color, 0, 1.f);
							count++;
						}
					}
        } else { // gridptr is null, then use GridPoints
        	
        	gridPoints = caKernel.getGridPoints();
        	
        	Point4[] points = gridPoints.points;
        	float color = 0f;
        	for (int i = 0; i < points.length; i++) {
				plaPts[i] = new Point3f(points[i].x, points[i].y, points[i].z);
				color = (points[i].argb & 0xff)/255f; // TODO This should be change to (a r g b) colors, not only one color.
				plaCls[i] = new Color4f(0,color,0,1f);
			}
        }
		
        pla = new PointArray(pointsNum, pointsArrayMod);
		pla.setCoordinates(0, plaPts);
        pla.setColors(0, plaCls);
        plShape.setGeometry(pla);
        
	}
	
	void displayStatus(String str) {
		parentApplet.displayStatus(str);
	}
	
	void displayCAStatus(String str) {
		parentApplet.displayCAStatus(str);
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
	
	public boolean computationReady() {
		return caKernel.initialized();
	}
	
	public boolean isLocalKernel() {
		return caKernel instanceof CellularAutomataGrid;
	}
	
	public boolean isRemoteKernel() {
		return caKernel instanceof ComputationClient;
	}

}



