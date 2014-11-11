/**
 * 
 */
package com.cellular3d;

import java.applet.Applet;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
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
public class CellularAutomata3D extends Applet implements Runnable, KeyListener {
	
	/* Thread */
	boolean runRotateThread = true;
	boolean stopRotateThread = true;
	boolean runComputThread = true;
	boolean stopComputThread = true;
	boolean colorMod = false; // true, speed mode; false, density mode. 
	Thread rotateThread;
	Thread computThread;
	
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
	
	/* frame parametres */
	public static int canvasWidth = 1400;
	public static int canvasHeight = 900;
	public static int AppletWidth = canvasWidth + 200;
	public static int AppletHeight = canvasHeight;
	
	/* components */
	JTextArea  jTextArea;
	JTextField jTextField;
	
	String[][] usageString = {
			{"a", " - start or stop compute thread."},
			{"s", " - switch color mode (not avaliable now)."},
			{"space", " - start or stop rotation."},
			{"h", " - print help lines.s"}
			};

	/**
	 * 
	 */
	public CellularAutomata3D() {
		
		this.setBackground(Color.black);
		this.setForeground(Color.white);
		
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
		
		System.out.println("* start...");
		
	}
	
	void initCompoents() {
		
		this.setLayout(null);
		
		jTextArea = new JTextArea("* CellularAutomata3D *");
		jTextArea.setBackground(Color.black);
		jTextArea.setForeground(Color.white);
		this.add(jTextArea);
		jTextArea.setBounds(0, 0, this.AppletWidth - this.canvasWidth, this.AppletHeight - 50);
		
		jTextField = new JTextField("Status");
		jTextField.setBackground(Color.black);
		jTextField.setForeground(Color.white);
		this.add(jTextField);
		jTextField.setBounds(0, this.AppletHeight - 50, this.AppletWidth - this.canvasWidth, 50);

		this.add(canvas3d);
		canvas3d.setLocation(this.AppletWidth - this.canvasWidth, 0);
		
	}
	
	public void displayString(String str) {
		jTextArea.append("\n" + str);
	}
	
	public void displayStatus(String str) {
		jTextField.setText(str);
	}
	
	void initObjects() {
		
		/* dots */
		dots3d = new Dots3DShape(boxscale, this);
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
		
		displayMessage();
		
		rotateThread = new Thread(this);
		rotateThread.start();
		rotateThread.suspend();
		computThread = new Thread(this);
		computThread.start();
		computThread.suspend();
	}
	
	private void updateMessage() {
		jTextArea.setText("");
		displayMessage();
	}
	
	private void displayMessage() {
		/* display messages */
		displayString("Key bind:");
		for (String[] strings : usageString) {
			displayString(" " + strings[0]+strings[1]);
		}
		displayString("System Message:");
		displayString("  java.version: " + System.getProperty("java.version"));
		displayString("  os.name: "+System.getProperty("os.name"));
		displayString("  jvm version: "+System.getProperty("java.vm.version"));
		displayString("  user.name: "+System.getProperty("user.name"));
		Runtime runtime = Runtime.getRuntime();
		displayString("Runtime Message:");
		displayString("  totalMemory: " + runtime.totalMemory()/1024/1024+" M");
		displayString("  freeMemory: " + runtime.freeMemory()/1024/1024+" M (" 
				+(100*runtime.freeMemory()/runtime.totalMemory())+"%)");
		displayString("  maxMemory: " + runtime.maxMemory()/1024/1024+" M");
		if (runtime.maxMemory() < dots3d.getRequiredMemory()) {
			displayString("   * WARN: memory is not enough");
			System.out.println("   * WARN: memory is not enough, required for"+
					dots3d.getRequiredMemory()/1024/1024+" M");
		}
		displayString("Grid Message:");
		displayString("  size: "+dots3d.getXSize()+"x"+dots3d.getYSize()+"x"+dots3d.getZSize());
		displayString("  required max memory: "+dots3d.getRequiredMemory()/1024/1024+" M");
	}

	@Override
	public void run() {
		long msec = 0;
		
		if (Thread.currentThread().equals(rotateThread))
			while (runRotateThread) {
					/* rotate */
					dots3d.rotX(.01);
					this.repaint();
					
					try {
						rotateThread.sleep(1000/36);
//						Thread.sleep(1000/24);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
			}
		else if (Thread.currentThread().equals(computThread)) {
			while(runComputThread) {
				msec = System.currentTimeMillis();
				updateDots();
				repaint();
				this.displayStatus(" CA FPS: "+(1000/(System.currentTimeMillis() - msec)));
				this.updateMessage();
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		
		System.out.println("* exit *");
	}
	
	void updateDots() {
		dots3d.updateDots();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()) {
		case ' ': // start or stop animation
			stopRotateThread = !stopRotateThread;
			System.out.println("KEYBOARD: " + ((stopRotateThread)?"stop":"start")+" rotation");
			if (stopRotateThread)
				rotateThread.suspend();
			else
				rotateThread.resume();
			break;
		case 's': // switch color mode between speed and density
			colorMod = !colorMod;
			System.out.println("KEYBOARD: " + "switch colormod(not available now!)");
			break;
		case 'a':
			stopComputThread = !stopComputThread;
			System.out.println("KEYBOARD: " + ((stopComputThread)?"stop":"start")+" computation");
			if (stopComputThread)
				computThread.suspend();
			else
				computThread.resume();
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

}
