package com.cellular3d;

import com.sun.j3d.utils.applet.MainFrame;

/**
 * open the MainFrame contain CellularAutomata3D Applet
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014Äê11ÔÂ10ÈÕ
 *
 */
public class Main {

	public Main() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		System.out.println("* start CellularAutomata3D in MainFrame *\n");
		new MainFrame(new CellularAutomata3D(), CellularAutomata3D.AppletWidth, CellularAutomata3D.AppletHeight);

	}

}
