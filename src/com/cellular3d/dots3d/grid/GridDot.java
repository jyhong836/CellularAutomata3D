package com.cellular3d.dots3d.grid;

/**
 * Dot of grid, to package data of dot.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014Äê11ÔÂ10ÈÕ
 *
 */
public class GridDot {
	
	private float prob[][];
	public int   mass[][];
	public int value;
	public int count;
	public int countLine[];

	public GridDot() {
		prob = new float[3][2];
		mass = new int[3][2];
		countLine = new int[3];
		value = 0;
		count = 0;
	}
	
	/**
	 * use to update values which can not be set directly
	 */
	public void update() {
		value = 0;
		count = 0;
		int ii = 0;
		for (int i = 0; i < mass.length; i++) {
			for (int j = 0; j < mass[0].length; j++) {
				ii = mass[i][j];
				value += ii;
				count += (ii>0)?1:0;
				countLine[i] += (ii>0)?1:0;
			}
		}
	}
	
	public void clear() {
		value = 0;
		count = 0;
		for (int i = 0; i < mass.length; i++) {
			for (int j = 0; j < mass[0].length; j++) {
				prob[i][j] = 0;
				mass[i][j] = 0;
			}
			countLine[i] = 0;
		}
	}

}
