import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.Timer;

public class Driver extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
	//EVERY ANIMATED DRIVER YOU COULD EVER NEED IS HERE
	//variables here
	int fps = 60;
	int test = 0;
	int[] x = {100,200,300,300};
	int[] y = {200,100,200,500};
	ArrayList<Slider> sliders = new ArrayList<Slider>();
	ArrayList<Circle> circles = new ArrayList<Circle>();
	int score=0;
	Clip clip;
	
	int responseTime = 75;
	int circleSize = 100;
	//keys
	boolean keys[] = new boolean[256];
	int mouseX = 0;
	int mouseY = 0;
	boolean press=false;
	
	int tick = 0;
	
	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		
		// background
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, 2000, 1600);

		g.setColor(new Color(255,255,255));
		g.drawString("score: "+score, 25, 25);
		
		
		if (press) {
			g.setColor(new Color(100,100,100));
		}
    //circles
    for (int i = 0; i < circles.size(); i++) {
			Circle s = circles.get(i);
			//skip premature circles
			if (s.startTime>tick+responseTime) continue;
			if (s.finished) continue;
			
			if (s.startTime-tick>0)
				g.drawOval(s.getX()-circleSize/2-(s.startTime-tick)/2, s.getY()-circleSize/2-(s.startTime-tick)/2, circleSize+s.startTime-tick, circleSize+s.startTime-tick);

			g.drawOval(s.getX()-circleSize/2, s.getY()-circleSize/2, circleSize, circleSize);
		}
    //sliders
		for (int i = 0; i < sliders.size(); i++) {
			Slider s = sliders.get(i);
			//skip premature circles
			if (s.startTime>tick+responseTime) continue;
			if (s.finished) continue;
			
			if (s.startTime-tick>0)
				g.drawOval(s.getX()[0]-circleSize/2-(s.startTime-tick)/2, s.getY()[0]-circleSize/2-(s.startTime-tick)/2, circleSize+s.startTime-tick, circleSize+s.startTime-tick);
			
			for (int j = 0; j < s.getX().length; j++) {
				if (j<s.getX().length-1)
					g.drawLine(s.getX()[j]-circleSize/2, s.getY()[j]-circleSize/2, s.getX()[j+1]-circleSize/2, s.getY()[j+1]-circleSize/2);
				g.drawOval(s.getX()[j]-circleSize/2, s.getY()[j]-circleSize/2, circleSize, circleSize);
			}
			g.drawOval(s.getCircleX()-circleSize/2, s.getCircleY()-circleSize/2, circleSize, circleSize);
		}
		g.drawOval(mouseX-20, mouseY-20, 40, 40);
	}

	public void update() {
		if (tick==98) clip.start();
		//if (s.returnScore()!=-1)System.out.println(s.returnScore());
		score = 0;
		for (int i = 0; i < sliders.size(); i++) {
			score += sliders.get(i).returnScore();
			sliders.get(i).update(tick, mouseX, mouseY, press);
		}
		for (int i = 0; i < circles.size(); i++) {
			score += circles.get(i).returnScore();
			circles.get(i).update(tick, mouseX, mouseY, press);
		}
		//keypress
		if (keys[90]||keys[88]) {
			press = true;
		}
		else {
			press = false;
		}
		
		tick++;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		update();
		repaint();
	}

	public static void main(String[] args) {
		Driver d = new Driver();

	}

	public Driver() {
		JFrame f = new JFrame();
		f.setTitle("Title");
		f.setSize(800, 800);
		f.setBackground(Color.BLACK);
		f.setResizable(false);
		f.addKeyListener(this);
		f.addMouseMotionListener(this);
		f.addMouseListener(this);
		
		f.add(this);

		Timer t = new Timer(20, this);
		t.start();
		f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		f.setVisible(true);

		/*   init stuff    */
	    try {
	        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("Winter Wind x Megalovania shortened.wav").getAbsoluteFile());
	        clip = AudioSystem.getClip();
	        clip.open(audioInputStream);
	        //clip.start();
	    } catch(Exception ex) {
	        System.out.println("Error with playing sound.");
	        ex.printStackTrace();
	    }


		//the song starts at t=100
		//intro
		circles.add(new Circle(400,400,100));
		circles.add(new Circle(500,300,250));
		circles.add(new Circle(400,300,300));
		circles.add(new Circle(500,200,350));
		circles.add(new Circle(100,200,400));
		
		circles.add(new Circle(400,400,475));
		circles.add(new Circle(500,300,525));
		circles.add(new Circle(400,300,575));
		circles.add(new Circle(500,200,625));
		circles.add(new Circle(100,200,675));
		circles.add(new Circle(200,200,725));
		circles.add(new Circle(300,200,775));
		circles.add(new Circle(400,400,825));
		
		//theme 1 part 1
		circles.add(new Circle(100,200,950));
		sliders.add(new Slider(new int[] {250,350}, new int[] {200,200},975));
		circles.add(new Circle(500,200,1025));
		circles.add(new Circle(400,300,1050));
		circles.add(new Circle(500,400,1075));
		circles.add(new Circle(400,500,1100));
		
		circles.add(new Circle(100,600,950+175));
		sliders.add(new Slider(new int[] {250,350}, new int[] {600,600},975+175));
		circles.add(new Circle(500,600,1025+175));
		circles.add(new Circle(400,500,1050+175));
		circles.add(new Circle(500,400,1075+175));
		circles.add(new Circle(400,300,1100+175));
		
		//theme 1 part 2
		circles.add(new Circle(100,200,1350));
		sliders.add(new Slider(new int[] {250,350}, new int[] {200,200},1375));
		circles.add(new Circle(500,200,1425));
		circles.add(new Circle(400,300,1450));
		circles.add(new Circle(500,400,1475));
		circles.add(new Circle(400,500,1500));
		
		
		//TEMPLATES
		//circles.add(new Circle(200,200,950));
		//sliders.add(new Slider(new int[] {100,200,300}, new int[] {200,250,350}, 250));
		
		
	}

	Timer t;
	@Override
	public void mouseClicked(MouseEvent e) {
		press = true;

	}
	@Override
	public void mouseMoved(MouseEvent m) {
		// TODO Auto-generated method stub
		m.translatePoint(-6, -31);
		mouseX =m.getX();
		mouseY =m.getY();
	}
	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}



	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		press = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		press = false;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

}
