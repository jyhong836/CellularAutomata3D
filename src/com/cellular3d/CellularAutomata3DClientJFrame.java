package com.cellular3d;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

/**
 * JFrame contain Applet and MenuBar.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014Äê11ÔÂ10ÈÕ
 *
 */
public class CellularAutomata3DClientJFrame extends JFrame implements WindowListener, ActionListener {

	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	Applet applet;
	
	/* Menu Items */
	JMenuItem connectItem;

	public CellularAutomata3DClientJFrame(Applet app, int width, int height) {
		this.addWindowListener(this);
		this.setTitle("Cellular Antomata Client");
		this.setBounds(screenSize.width/2 - width/2 - 10,
				screenSize.height/2 - height/2 - 55, 
				width+20, 
				height+70);
		this.setLayout(new FlowLayout());
		
		/* Applet */
		this.applet = app;
		this.add(app);
		app.setSize(width, height);
		app.init();
		app.start();
		
		/* Menu */
		JMenuBar mb = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		connectItem = new JMenuItem("Connect");
		mb.add(menu);
		menu.add(connectItem);
		this.setJMenuBar(mb);
		
		connectItem.addActionListener(this);
		
		this.setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		applet.destroy();
		
		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		applet.destroy();
		
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {}

	@Override
	public void windowDeiconified(WindowEvent e) {}

	@Override
	public void windowIconified(WindowEvent e) {}

	@Override
	public void windowOpened(WindowEvent e) {}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(connectItem)) {
			System.out.println("connect to freeshell...");
			// TODO realize the method of connect to freeshell
		}
		
	}

}
