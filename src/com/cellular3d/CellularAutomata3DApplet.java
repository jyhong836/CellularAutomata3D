package com.cellular3d;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Menu;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;

import com.cellular3d.dots3d.Dots3DShape;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;

/**
 * Cellular Automata in 3D mode. This class will control all of the Thread objects.
 * @author jyhong (Junyuan Hong/jyhong836@gmail.com) 2014年11月10日
 *
 */
public class CellularAutomata3DApplet extends Applet implements Runnable, KeyListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/* Thread */
	boolean runRotateThread = true;
	boolean stopRotateThread = true;
	boolean runComputThread = true;
	boolean stopComputThread = true;
	boolean colorMod = false; // true, speed mode; false, density mode. 
	Thread rotateThread;
	Thread computThread;
	int caRunCount; // the count of ca loop times
	
	/* Dots3DShape */
	Dots3DShape dots3d;
	
	/* java3d objects */
	BranchGroup group;
	SimpleUniverse universe;
	Canvas3D canvas3d;
	ViewingPlatform viewingPlatform;
	Transform3D t3;
	TransformGroup boxTransformGroup;
	
	float boxscale = 0.3f;
	int   gridsize = 50;
	
	/* frame parametres */
	public int canvasWidth = 1400;
	public int canvasHeight = 900;
	public int AppletWidth = canvasWidth + 200;
	public int AppletHeight = canvasHeight;
	
	/* components */
	JTextArea  jTextArea;
	JTextField statusField;
	JTextField caStatusField;
	
	String[][] usageString = {
			{"a", " - start or stop compute thread."},
			{"s", " - switch color mode (not avaliable now)."},
			{"space", " - start or stop rotation."},
			{"h", " - print help lines.s"}
			};

	/**
	 * 
	 */
	public CellularAutomata3DApplet() {
		
		this.setBackground(Color.black);
		this.setForeground(Color.white);
		
		
	}
	
	void init3D() {
		/* ----------- java3d canvas init--------- */
		canvas3d = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
		universe = new SimpleUniverse(canvas3d);
		canvas3d.setSize(canvasWidth, canvasHeight);
		
		viewingPlatform = universe.getViewingPlatform();
		viewingPlatform.setNominalViewingTransform();
		
		group = new BranchGroup();
		
		initObjects();  // objects including grid dots
		
		initLight();    // light
		
		initBehavior(); // orbit behavior
		
		universe.addBranchGraph(group);

		canvas3d.addKeyListener(this);
		
		this.add(canvas3d);
		canvas3d.setLocation(this.AppletWidth - this.canvasWidth, 0);
		
	}
	
	void initCompoents() {
		
		this.setLayout(null);
		
		jTextArea = new JTextArea();
		jTextArea.setBackground(Color.black);
		jTextArea.setForeground(Color.white);
		this.add(jTextArea);
		jTextArea.setBounds(0, 0, this.AppletWidth - this.canvasWidth, this.AppletHeight - 100);
		jTextArea.setEditable(false); // make the chars unchangeable by user
//		jTextArea.setIgnoreRepaint(true);
		jTextArea.setAutoscrolls(true);
		jTextArea.setLineWrap(true);
		jTextArea.setTabSize(1);
		
		statusField = new JTextField("Status");
		statusField.setEditable(false);
		statusField.setBackground(Color.black);
		statusField.setForeground(Color.white);
		this.add(statusField);
		statusField.setBounds(0, this.AppletHeight - 100, this.AppletWidth - this.canvasWidth, 50);
		
		caStatusField = new JTextField("CA not run");
		caStatusField.setEditable(false);
		caStatusField.setBackground(Color.black);
		caStatusField.setForeground(Color.white);
		this.add(caStatusField);
		caStatusField.setBounds(0, this.AppletHeight - 50, this.AppletWidth - this.canvasWidth, 50);
		
	}
	
	public void displayString(String str) {
		jTextArea.append("\n" + str);
	}
	
	public void displayStatus(String str) {
		statusField.setText(str);
	}
	
	public void displayCAStatus(String str) {
		caStatusField.setText(str);
	}
	
	void initObjects() {
		
		/* dots */
		dots3d = new Dots3DShape(boxscale, gridsize, this);
		this.group.addChild(dots3d);
	
	}
	
	void initBehavior() {
		OrbitBehavior ob = new OrbitBehavior(canvas3d);
		viewingPlatform.setViewPlatformBehavior(ob);
		ob.setViewingPlatform(viewingPlatform);
		ob.setSchedulingBounds(new BoundingSphere(new Point3d(.0,.0,.0),100.0));
	}
	
	void initLight() {

		Color3f light1Color = new Color3f(1.f, 1f, 1f);
		// 设置光线的颜色
		BoundingSphere bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
		// 设置光线的作用范围
		Vector3f light1Direction = new Vector3f(4.0f, -7.0f, -12.0f);
		// 设置光线的方向
		DirectionalLight light1= new DirectionalLight(light1Color, light1Direction);
		// 指定颜色和方向，产生单向光源
		light1.setInfluencingBounds(bounds);
		// 把光线的作用范围加入光源中
		group.addChild(light1);
		
	}
	
	@Override
	public void init() {
		super.init();
		initCompoents();
		
		init3D();
		
		displayMessage();

		System.out.println("start threads...");
		
		rotateThread = new Thread(this);
		rotateThread.start();
		rotateThread.suspend();
		computThread = new Thread(this);
		computThread.start();
//		computThread.suspend();
		System.out.println("init threads done.");
	}
	
	private void updateMessage() {
		jTextArea.setText("");
		displayMessage();
	}
	
	private void displayMessage() {
		/* display messages */
		displayString("* CellularAutomata3D *");
		displayString("Key bind:");
		for (String[] strings : usageString) {
			displayString("\t" + strings[0]+strings[1]);
		}
		displayString("System Message:");
		displayString("\tjava.version: " + System.getProperty("java.version"));
		displayString("\tos.name: "+System.getProperty("os.name"));
		displayString("\tjvm version: "+System.getProperty("java.vm.version"));
		displayString("\tuser.name: "+System.getProperty("user.name"));
		Runtime runtime = Runtime.getRuntime();
		displayString("Runtime Message:");
		displayString("\ttotalMemory: " + runtime.totalMemory()/1024/1024+" M");
		displayString("\tfreeMemory: " + runtime.freeMemory()/1024/1024+" M (" 
				+(100*runtime.freeMemory()/runtime.totalMemory())+"%)");
		displayString("\tmaxMemory: " + runtime.maxMemory()/1024/1024+" M");
		if (runtime.maxMemory() < dots3d.getRequiredMemory()) {
			displayString("   * WARN: memory is not enough");
			System.out.println("   * WARN: memory is not enough, required for"+
					dots3d.getRequiredMemory()/1024/1024+" M");
		}
		displayString("Grid Message:");
		displayString("\tsize: "+dots3d.getXSize()+"x"+dots3d.getYSize()+"x"+dots3d.getZSize());
		displayString("\trequired max memory: "+dots3d.getRequiredMemory()/1024/1024+" M");
	}

	@Override
	public void run() {
//		long msec = 0;
		
		if (Thread.currentThread().equals(rotateThread))
			while (runRotateThread) {
				rotateThreadMethod();
			}
		else if (Thread.currentThread().equals(computThread)) {
			caRunCount = 0;
			while(runComputThread) {
				computThreadMethod();
			}
		}
	}
	
	private void rotateThreadMethod() {
//		if (!stopRotateThread) {
//			System.out.println("rotating...");
			/* rotate */
			dots3d.rotX(.01);
			this.repaint();
			
			try {
				rotateThread.sleep(1000/36);
//				Thread.sleep(1000/24);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
//		else {
//			try {
////				System.out.println("wait...");
//				this.wait();
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
	}
	
	private synchronized void computThreadMethod() {
		if (!stopComputThread) {
			long msec = System.currentTimeMillis();
			dots3d.updateDots();
			this.displayCAStatus(" CA FPS: "+(1000/(System.currentTimeMillis() - msec))+" COUNT: "+(++caRunCount));
			this.updateMessage();
	//		repaint();
		} else {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		System.out.println("* Exit Applet *");
	}
	
//	void updateDots() {
//		dots3d.updateDots();
//	}

	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
		case ' ':  // start or stop animation
			switchRotateThreadStat();
			break;
		case 's': // switch color mode between speed and density
			colorMod = !colorMod;
			System.out.println("KEYBOARD: " + "switch colormod(not available now!)");
			break;
		case 'a':
			switchComputationThreadStat(); // start or stop CA computation
			break;
		case 'h':
			System.out.println("Usage:");
			for (String[] strings : usageString) {
				System.out.println("  "+strings[0] + strings[1]);
			}
			break;

		default:
			break;
		}
		
	}
	
	private void switchRotateThreadStat() { // start or stop animation
		stopRotateThread = !stopRotateThread;
		System.out.println("KEYBOARD: " + ((stopRotateThread)?"stop":"start")+" rotation");
		if (stopRotateThread)rotateThread.suspend();
//			try {
//				rotateThread.wait();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		else
			rotateThread.resume();
//			notifyAll();
	}
	
	private synchronized void switchComputationThreadStat() { // start or stop CA computation
		stopComputThread = !stopComputThread;
		System.out.println("KEYBOARD: " + ((stopComputThread)?"stop":"start")+" computation");
		if (stopComputThread) {
//			computThread.suspend(); // stop it
			this.displayCAStatus(" CA [STOP] COUNT: "+caRunCount);
		} else {
			if (dots3d.computationReady()) {
				System.out.println("restart compuation...");
//				computThread.resume(); // resume it
				notifyAll();
//				System.out.println("success");
			} else { 
				StringBuffer msg = new StringBuffer("Computation is not ready!");
				if (dots3d.isLocalKernel()) {
					msg.append("\nYou are using Local Kernel, this could be a bug.\nPlease report it to me.");
				} else {
					msg.append("\nYou are using Remote Kernel.\nTry to connect to the server, or contact the Administrator.");
				}
				JOptionPane.showMessageDialog(this, msg);
				stopComputThread = !stopComputThread;
			}
		}
	}
	
	public boolean connectRemoteKernel() {
		if (dots3d.isRemoteKernel()) {
			// REMOTE
			return dots3d.connectRemoteKernel("127.0.0.1", 8000);
		}
		return false;
	}
	
	public synchronized boolean disconnectRemoteKernel() { // 避免被同样是同步方法的方法调用
		/* stop the computThread firstly */
		if (!stopComputThread) {
			stopComputThread = !stopComputThread;
//			computThread.suspend();
			this.displayCAStatus(" CA [STOP] COUNT: "+caRunCount);
		}
		
		if (dots3d.isRemoteKernel()) {
			// REMOTE
			return dots3d.disconnectRemoteKernel();
		}
		return false;
	}
	
	/**
	 * Switch computation kernel of Cellular Automata.
	 * @return true, if switch from local to remote.
	 */
	public synchronized boolean switchKernel() {
		/* stop the computThread firstly */
		if (!stopComputThread) {
			stopComputThread = !stopComputThread;
//			computThread.suspend();
			this.displayCAStatus(" CA [STOP] COUNT: "+caRunCount);
		}
		caRunCount = 0;
//		displayStatus(dots3d.currentKernelString());
		if (dots3d.isLocalKernel()) {
			// LOCAL to REMOTE
			displayStatus("switch from local to remote");
			dots3d.setRemoteKernel();
		} else if (dots3d.isRemoteKernel()) {
			// REMOTE to LOCAL
			displayStatus("switch from remote to local"); // 用同步锁锁定dots3d
			dots3d.disconnectRemoteKernel(); // ATTENTION: should close the socket firstly
			dots3d.setLocalKernel(); // FIXME 使用线程的suspend方法会导致从预料之外的地方执行，使得切换失败
			return false;
		}
		return true;
	}

}

