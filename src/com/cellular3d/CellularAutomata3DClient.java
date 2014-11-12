package com.cellular3d;

public class CellularAutomata3DClient {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("* start CellularAutomata3D in MainFrame *\n");
		CellularAutomata3DApplet ca3d = new CellularAutomata3DApplet(); // Applet
		new CA3DClientJFrame(ca3d, ca3d.AppletWidth, ca3d.AppletHeight);

	}

}
