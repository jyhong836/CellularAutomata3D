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

import com.cellular3d.CellularAutomata3D;
import com.cellular3d.dots3d.grid.CellularAutomataGrid;
import com.cellular3d.dots3d.grid.GridDot;
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
	
	private int size  = 100;
	private int xsize = size;
	private int ysize = size;
	private int zsize = size;
	
	CellularAutomataGrid cag;
	private GridDot[][][]   gridptr;
	
	private float width  = .8f;
	private float depth  = .8f;
	private float height = .8f;
//	private float initAirDensity = .1f;
	
	Box box;
	Transform3D t3;
	private double rotXAngle = .0f;
	
	CellularAutomata3D parentApplet = null;
	
	/**
	 * constructor
	 * @param scale the width, depth and height will be set to scale*2.
	 * @param parentApplet the Applet will be used to display status.
	 */
	public Dots3DShape(float scale, CellularAutomata3D parentApplet) {
		this(scale*2, scale*2, scale*2, parentApplet);
	}

	/**
	 * constructor
	 * @param width
	 * @param depth
	 * @param height
	 * @param parentApplet the Applet will be used to display status.
	 */
	public Dots3DShape(float width, float depth, float height, CellularAutomata3D parentApplet) {
		
		this.parentApplet = parentApplet;
		
		this.width = width;
		this.depth = depth;
		this.height = height;

		/* box */
		Appearance boxapp = new Appearance();
		if (boxapp!=null)
			boxapp.setPolygonAttributes(new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_BACK,0));
		box = new Box(width/2, depth/2, height/2, boxapp);
		
		/* intialize CellularAutomataGrid */
		cag = new CellularAutomataGrid(width, depth, height);
		gridptr = cag.getGridPtr();
		this.pointsNum = cag.getPointsNum();
		
		System.out.println("init 3D shape points...");
		long msec = System.currentTimeMillis();

		// initialize Appearance and set color mode
        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0f, 1.0f, 1.0f), ColoringAttributes.FASTEST);
        app.setColoringAttributes(ca);

        System.out.println(" points number: "+ pointsNum);
        Point3f[] plaPts = new Point3f[pointsNum];
        Color4f[] plaCls = new Color4f[pointsNum];

        /* set points */
        int count = 0;
        float color = 0f;
        for (int i = 0; i < xsize; i++) 
        	for (int j = 0; j < ysize; j++) 
				for (int k = 0; k < zsize; k++) 
					if (gridptr[i][j][k].value>0) {
						plaPts[count] = new Point3f(width * i/xsize - width/2 , depth * j/ysize - depth/2 , height * k/zsize - height/2);
						color = gridptr[i][j][k].value/4.f;
						plaCls[count] = new Color4f(0, color, 0, 1.f);
						count++;
					}
        
        /* create PointArray */
        pla = new PointArray(pointsNum, pointsArrayMod);
        pla.setCoordinates(0, plaPts);
        pla.setColors(0, plaCls);

        /* create Shape3D and add to BranchGroup */
        plShape = new Shape3D(pla, app);
        plShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
        
		t3 = new Transform3D(); // init t3
       	objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.addChild(plShape);
        objRotate.addChild(box);
        this.addChild(objRotate);
        
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
	
	/**
	 * Update dots in CA grid with the method of CellularAutomataGrid. And update points
	 * of 3D Shape at the same time.
	 */
	public void updateDots() {
		cag.updateDots();
		this.updatePoints();
	}

	public void updatePoints() {
		
//		long msec = System.currentTimeMillis();
		
		// update gridptr
		gridptr = cag.getGridPtr();
		
		pointsNum = 0;
		for (int i = 0; i < gridptr.length; i++)
			for (int j = 0; j < gridptr[0].length; j++)
				for (int k = 0; k < gridptr[0][0].length; k++) {
					if (gridptr[i][j][k].count>0) {
						pointsNum++;
					}
				}
        Point3f[] plaPts = new Point3f[pointsNum];
        Color4f[] plaCls = new Color4f[pointsNum];

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
        pla = new PointArray(pointsNum, pointsArrayMod);
		pla.setCoordinates(0, plaPts);
        pla.setColors(0, plaCls);
        plShape.setGeometry(pla);
        
//        displayStatus("FPS: "+(1000/(System.currentTimeMillis() - msec)));
        
	}
	
	void displayStatus(String str) {
		parentApplet.displayStatus(str);
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

}



