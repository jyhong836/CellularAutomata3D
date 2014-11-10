package com.cellular3d.dots3d;

import java.applet.Applet;
import java.util.Arrays;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.naming.directory.DirContext;
import javax.vecmath.Color3f;
import javax.vecmath.Color4f;
import javax.vecmath.Point3f;

import com.cellular3d.CellularAutomata3D;
import com.cellular3d.dots3d.grid.GridDot;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

/**
 * Dots in 3D mode. Every dot is a computation point, which may not include 
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
	
	private int size  = 58;
	private int xsize = size;
	private int ysize = size;
	private int zsize = size;
	
	private GridDot[][][][] grid; 
	private int             gridIndex = 0;
	private GridDot[][][]   gridptr;
	private int             gridIndexBuff = 1;
	private GridDot[][][]   gridPtrBuff;
	
	private float width  = .8f;
	private float depth  = .8f;
	private float height = .8f;
	private float initAirDensity = .04f;
	
	Transform3D t3;
	private double rotXAngle = .0f;
	
	CellularAutomata3D parentApplet = null;
	
	/**
	 * constructor
	 * @param scale
	 */
	public Dots3DShape(float scale, CellularAutomata3D parentApplet) {
		this(scale*2, scale*2, scale*2);
		this.parentApplet = parentApplet;
	}

	/**
	 * constructor
	 * @param width
	 * @param depth
	 * @param height
	 */
	public Dots3DShape(float width, float depth, float height) {
		
		this.width = width;
		this.depth = depth;
		this.height = height;
		
		pointsNum = 0;
		grid = new GridDot[2][xsize][ysize][zsize];
		gridptr = grid[gridIndex];
		gridPtrBuff = grid[gridIndexBuff];
		
		// init grid dots
		initGridDots();
		
		System.out.println("init 3D shape points...");
		long msec = System.currentTimeMillis();

        Appearance app = new Appearance();
        ColoringAttributes ca = new ColoringAttributes(new Color3f(0f, 1.0f, 1.0f), ColoringAttributes.FASTEST);
        app.setColoringAttributes(ca);

        Point3f[] plaPts = new Point3f[pointsNum];
        Color4f[] plaCls = new Color4f[pointsNum];

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
        
        System.out.println(" points number: "+ pointsNum);
        
        pla = new PointArray(pointsNum, pointsArrayMod);
        pla.setCoordinates(0, plaPts);
        pla.setColors(0, plaCls);

        plShape = new Shape3D(pla, app);
        plShape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		t3 = new Transform3D();
       	objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.addChild(plShape);
        this.addChild(objRotate);
        
        System.out.println("success in " + (System.currentTimeMillis() - msec) + " ms");
	}
	
	private void initGridDots() {

		System.out.println("init GridDots...");
		/* --------- random air grids ----------- */
		System.out.print(" init random air grids... memory...");
		long msec = System.currentTimeMillis();
		
		double prob = 1.0;
		int[][] mass;
		for (int i = 0; i < xsize; i++) {
			for (int j = 0; j < ysize; j++)
				for (int k = 0; k < zsize; k++)
					gridptr[i][j][k] = new GridDot();
//			if (i%10==0) {
//				System.out.println(i+" ");
////				System.out.flush();
//			}
		}
		System.out.print("ok...");
		for (int i = 1; i < xsize-1; i++) {
			for (int j = 1; j < ysize-1; j++) {
				for (int k = 1; k < zsize-1; k++) {
					if (Math.random()<initAirDensity){
						mass = gridptr[i][j][k].mass;
						// choose one direction add particle
						mass[(int)Math.floor(Math.random()*3)][(Math.random()>0.5)?1:0] = 1;
						pointsNum++;
						gridptr[i][j][k].count = 1;
						gridptr[i][j][k].value = 1;
					}
				}
			}
//			if (i%10==0) {
//				System.out.print("\r"+i+" ");
//				System.out.flush();
//			}
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

		for (int i = 0; i < gridPtrBuff.length; i++) {
			for (int j = 0; j < gridPtrBuff[0].length; j++) {
				for (int k = 0; k < gridPtrBuff[0][0].length; k++) {
					gridPtrBuff[i][j][k] = new GridDot();
				}
			}
		}
		System.out.println("init GridDots success");
		
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
	
	public void updateDots() {
		
		long msec = System.currentTimeMillis();
		
		gridIndexBuff = (gridIndex>0)? 0:1;
		gridPtrBuff = grid[gridIndexBuff];
		
		int[][] mass;
		int[]   countLine;
		for (int i = 1; i < xsize-1; i++) {
			for (int j = 1; j < ysize-1; j++) {
				for (int k = 1; k < zsize-1; k++) {
					
					mass = gridptr[i][j][k].mass;
					countLine = gridptr[i][j][k].countLine;
					// direction in x axis
					if (mass[0][0]>0) { // come from +x direction
						if (mass[0][1]>0) { // from -x
							switch (countLine[1]) {
							case 0:
								gridPtrBuff[i][j-1][k].mass[1][0] = mass[0][0];
								gridPtrBuff[i][j+1][k].mass[1][1] = mass[0][1];
								break;
							case 1:
								gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
//								if (i==98)
//									System.out.print(false);;
								gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
								break;
							case 2:
								if (countLine[2]!=1) { // same to case 0
									gridPtrBuff[i][j-1][k].mass[1][0] = mass[0][0];
									gridPtrBuff[i][j+1][k].mass[1][1] = mass[0][1];
								} else { // same to case 1
									gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
//									if (i==98)
//										System.out.print(false);;
									gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
								}
								break;

							default:
								break;
							}
						} else {
							gridPtrBuff[i-1][j][k].mass[0][0] = mass[0][0];
						}
					}
					else if (mass[0][1]>0) { // come / from -x
						gridPtrBuff[i+1][j][k].mass[0][1] = mass[0][1];
					}
					// direction in y axis
					if (mass[1][0]>0) { // come from +x direction
						if (mass[1][1]>0) { // from -x
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
					// direction in z axis
					if (mass[2][0]>0) { // come from +x direction
						if (mass[2][1]>0) { // from -x
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
					
				} // k
			} // j
		} // i
		
		/* ------ process bounds ------- */
		for (int i = 0;i<xsize;i+=xsize-1)
			for (int j = 1; j < ysize-1; j++)
				for (int k = 1; k < zsize-1; k++) {
					if (gridptr[i][j][k].mass[0][0]>0) {
						gridPtrBuff[i+1][j][k].mass[0][1] = gridptr[i][j][k].mass[0][0];
					} else if (gridptr[i][j][k].mass[0][1]>0) {
						gridPtrBuff[i-1][j][k].mass[0][0] = gridptr[i][j][k].mass[0][1];
					}
				}

		for (int i = 1;i<xsize-1;i++)
			for (int j = 0; j < ysize; j+=ysize-1)
				for (int k = 1; k < zsize-1; k++) {
					if (gridptr[i][j][k].mass[1][0]>0) {
						gridPtrBuff[i][j+1][k].mass[1][1] = gridptr[i][j][k].mass[1][0];
					} else if (gridptr[i][j][k].mass[1][1]>0) {
						gridPtrBuff[i][j-1][k].mass[1][0] = gridptr[i][j][k].mass[1][1];
					}
				}

		for (int i = 1;i<xsize-1;i++)
			for (int j = 1; j < ysize-1; j++)
				for (int k = 0; k < zsize; k+=zsize-1) {
					if (gridptr[i][j][k].mass[2][0]>0) {
						gridPtrBuff[i][j][k+1].mass[2][1] = gridptr[i][j][k].mass[2][0];
					} else if (gridptr[i][j][k].mass[2][1]>0) {
						gridPtrBuff[i][j][k-1].mass[2][0] = gridptr[i][j][k].mass[2][1];
					}
				}

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
		
		updatePoints();

		msec -= System.currentTimeMillis();
//		System.out.println("used time: "+(0-msec)+"ms");
		
	}
	
	public void updatePoints() {
		
//		long msec = System.currentTimeMillis();
		
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

}



