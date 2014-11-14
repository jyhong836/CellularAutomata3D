package com.cellular3d;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * JFrame contain Applet and MenuBar.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014年11月10日
 *
 */
public class CA3DClientJFrame extends JFrame implements WindowListener, ActionListener {

	/**
	 * default serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	
	private CellularAutomata3DApplet caApplet;
	
	/* Menu Items */
	private Menu menu;
	private MenuItem kernelSwitchItem;
	private MenuItem connectItem;
	private MenuItem testConnectItem;
	private MenuItem setItem; 
	
	private Menu     serverMenu;
	private MenuItem localhostItem;
	private MenuItem freeshellItem;

	public CA3DClientJFrame(CellularAutomata3DApplet app, int width, int height) {
		this.addWindowListener(this);
		this.setTitle("Cellular Antomata Client");
		this.setBounds(screenSize.width/2 - width/2 - 10,
				screenSize.height/2 - height/2 - 55, 
				width+20, 
				height+70);
		this.setLayout(new FlowLayout());
		
		/* Cellular Automata Applet */
		this.caApplet = app;
		this.add(app);
		app.setSize(width, height);
		app.init();
		app.start();
		
		/* Menu */
		MenuBar mb = new MenuBar();
		menu = new Menu("Kernel(LOCAL)");
		kernelSwitchItem = new MenuItem("Switch Kernel");
		connectItem = new MenuItem("Connect");
		connectItem.setEnabled(false);
		testConnectItem = new MenuItem("Test connect");
		testConnectItem.setEnabled(false);
		setItem = new MenuItem("Configure");
		serverMenu = new Menu("Server");
		localhostItem = new MenuItem("localhost");
		localhostItem.setEnabled(false);
		freeshellItem = new MenuItem("freeshell");
		mb.add(menu);
		menu.add(kernelSwitchItem);
		menu.add(connectItem);
		menu.add(testConnectItem);
		menu.add(setItem);
		menu.add(serverMenu);
		
		serverMenu.add(localhostItem);
		serverMenu.add(freeshellItem);

		this.setMenuBar(mb);
		
		kernelSwitchItem.addActionListener(this);
		connectItem.addActionListener(this);
		testConnectItem.addActionListener(this);
		setItem.addActionListener(this);
		
		localhostItem.addActionListener(this);
		freeshellItem.addActionListener(this);
		
		this.setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent e) {}

	@Override
	public void windowClosed(WindowEvent e) {
		caApplet.destroy();
		
		System.exit(0);
	}

	@Override
	public void windowClosing(WindowEvent e) {
		caApplet.destroy();
		
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
		if (e.getActionCommand().equals("Connect")) {
			
			System.out.println("* connect to freeshell...");
//			JOptionPane.showMessageDialog(this, "connect to freeshell...");
			// FIXME 在连接过程中发生其他操作可能会导致问题
			// FIXME 连接时间过长会导致无法操作
//			caApplet.displayStatus("");
			if (caApplet.connectRemoteKernel()) {
				this.connectItem.setLabel("Disconnect");
				System.out.println("* connect to freeshell...success");
			} else 
				System.out.println("* connect to freeshell...failed");
			
		} else if(e.getActionCommand().equals("Disconnect")) {
			
			System.out.println("* disconnect to freeshell...");
			if (caApplet.disconnectRemoteKernel()) {
				this.connectItem.setLabel("Connect");
				System.out.println("* disconnect to freeshell...ok");
			}
			
		} else if (e.getSource().equals(testConnectItem)) {
			
			System.out.println(" test connect to freeshell...");
			// TODO realize the test method of connect to freeshell
			
		} else if (e.getSource().equals(kernelSwitchItem)) {
			
			System.out.println(" switch kernel");
			if (caApplet.switchKernel()) { // to remote
				this.connectItem.setEnabled(true);
				this.testConnectItem.setEnabled(true);
				this.menu.setLabel("Kernel(REMOTE)");
			} else { // to local
				this.connectItem.setEnabled(false);
				this.testConnectItem.setEnabled(false);
				this.menu.setLabel("Kernel(LOCAL)");
			}
//			connectItem.setEnabled(true);
			
		} else if (e.getSource().equals(setItem)) {
			// TODO create code for config dialog
			new CA3DClientConfigureJDialog();
			
		} else if (e.getSource().equals(localhostItem)) {
			caApplet.setServer("localhost", 8000);
			localhostItem.setEnabled(false);
			freeshellItem.setEnabled(true);
		} else if (e.getSource().equals(freeshellItem)) {
			caApplet.setServer("ssh.freeshell.ustc.edu.cn", 48912);
			freeshellItem.setEnabled(false);
			localhostItem.setEnabled(true);
		}
		
	}

}
