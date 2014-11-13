package com.cellular3d.dots3d.grid;

import java.io.Serializable;

public class Point4 implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7344955551495718602L;
	
	public float x, y, z;
	public int argb;
	
	public Point4(float x, float y, float z, int argb) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.argb = argb;
	}

}
