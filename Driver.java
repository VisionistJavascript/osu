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
	/*INSTRUCTIONS
	 * Press 'z' or 'x' or click to interact with objects
	 * There are two types of objects: circles and sliders
	 * 
	 * Click on the circles when the outer ring reaches the inner ring
	 * The circles are numbered in order of completion
	 * 
	 * Sliders are a series of circles connected by a line.  When a slider starts, a moving circle will travel between the numbered circles
	 * Move your mouse over the moving circle while clicking or holding 'x' or 'z'.
	 * 
	 * The rhythm of the song will help you time your clicks
	 * 
	 * The more accurate you are with your timing, the more points you get!
	 * Your accuracy is determined by the points you got/total points.  Get the highest accuracy you can!
	 * 
	 * NOTICE: The game sometimes takes a few runs to warm up, so just restart it if the sound is off too much.
	 * If the game isn't timed correctly after a couple test runs, change the lag multiplier below or restart your computer.
	 */
	
	//Increase this number if the circles are spawning in too late, decrease if spawning too early
	double lagMultiplier = 0.98;//default is 0.98
	//^^^^^^^^^^^^^^^^^^^^^^^^^
	
	//variables here
	ArrayList<Slider> sliders = new ArrayList<Slider>();
	ArrayList<Circle> circles = new ArrayList<Circle>();
	ArrayList<Particle> particles = new ArrayList<Particle>();
	ArrayList<Msg> messages = new ArrayList<Msg>();
	int score = 0;
	int totalScore = 0;
	int[] results = {0,0,0,0};//[misses, 50, 100, 300]
	
	
	Clip clip;
	
	int responseTime = 50;
	int circleSize = 100;
	
	Font numberFont = new Font("Arial",0,50);
	Font scoreFont = new Font("Arial",0,20);
	//colors
	ArrayList<Integer[]> colorChanges = new ArrayList<Integer[]>();//[tick, r, g, b]
	int colorChangeIndex = 0;
	ArrayList<Integer> pulses = new ArrayList<Integer>();//tick
	int pulseIndex = 0;
	int[] color = {0, 0, 0};
	int[] targetColor = {255, 255, 255};
	int[] borderColor = {0, 0, 0};
	//mouse trail
	int[] trailX = new int[15];
	int[] trailY = new int[15];
	
	//keys
	boolean keys[] = new boolean[256];
	int mouseX = 0;
	int mouseY = 0;
	boolean press=false;
	boolean mouseDown = false;
	
	int tick = 0;
	double fulltick = 0;
	
	
	@Override
	public void paint(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(borderColor[0],borderColor[1],borderColor[2]));
		g.fillRect(0, 0, 801, 801);
		
		g.setFont(scoreFont);
		g.setColor(new Color(color[0],color[1],color[2]));
		g.drawString("score: "+score, 25, 25);
		g.drawString("acc: "+Math.round(100*score/(totalScore+0.1))+"%", 25, 50);
	
		g.setFont(numberFont);
		
		//circles
	    for (int i = 0; i < circles.size(); i++) {
				Circle s = circles.get(i);
				//skip premature and finished circles
				if (s.startTime>tick+responseTime) continue;
				if (s.finished) continue;
				
				if (s.startTime-tick>0)
					g.drawOval(s.getX()-circleSize/2-(s.startTime-tick), s.getY()-circleSize/2-(s.startTime-tick), circleSize+(s.startTime-tick)*2, circleSize+(s.startTime-tick)*2);
	
				g.drawOval(s.getX()-circleSize/2, s.getY()-circleSize/2, circleSize, circleSize);
				g.drawString(""+s.number, s.getX()-12, s.getY()+12);
		}
	    //sliders
		for (int i = 0; i < sliders.size(); i++) {
			Slider s = sliders.get(i);
			//skip premature circles
			if (s.startTime>tick+responseTime) continue;
			if (s.finished) continue;
			
			if (s.startTime-tick>0)
				g.drawOval(s.getX()[0]-circleSize/2-(s.startTime-tick), s.getY()[0]-circleSize/2-(s.startTime-tick), circleSize+(s.startTime-tick)*2, circleSize+(s.startTime-tick)*2);
						
			for (int j = 0; j < s.getX().length; j++) {
				if (j<s.getX().length-1)
					g.drawLine(s.getX()[j], s.getY()[j], s.getX()[j+1], s.getY()[j+1]);
				//g.setColor(new Color(0,0,0));
				//g.fillOval(s.getX()[j]-circleSize/2, s.getY()[j]-circleSize/2, circleSize, circleSize);
				//g.setColor(new Color(255,255,255));
				g.drawOval(s.getX()[j]-circleSize/2, s.getY()[j]-circleSize/2, circleSize, circleSize);
				g.drawString(""+(s.number+j), s.getX()[j]-12, s.getY()[j]+12);
			}
			g.drawOval(s.getCircleX()-circleSize/2, s.getCircleY()-circleSize/2, circleSize, circleSize);
		}
		//end screen
		if (tick>3600) {
			endScreen(g);
		}
		//particles
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			drawParticle(g,p.x,p.y,p.angle);
		}
		//messages
		g.setFont(numberFont);
		for (int i = 0; i < messages.size(); i++) {
			Msg m = messages.get(i);
			g.setColor(m.getColor());
			g.drawString(m.text, m.x, m.y);
		}

		//mouse trail
		for (int i = 0; i < trailX.length-1; i++) {
			g.setColor(new Color(255-(255/trailX.length)*i,255-(255/trailX.length)*i,255-(255/trailX.length)*i));
			g.drawLine(trailX[i], trailY[i], trailX[i+1], trailY[i+1]);
		}
		
		//mouse
		g.setColor(new Color(255, 255, 255));
		if (press) {
			g.setColor(new Color(175, 175, 175));
		}
		g.drawOval(mouseX-10, mouseY-10, 20, 20);
	}

	public void update() {
		if (tick==100) clip.start();
		//score calculation
		totalScore = 0;
		score = 0;
		for (int i = 0; i < sliders.size(); i++) {
			Slider s = sliders.get(i);
			if (s.finished) totalScore+=300;
			score += s.returnScore();
			s.update(tick, mouseX, mouseY, press);
			
			//effects
			if (s.completionFrame) {
				int[] co = {0, 0, 0};
				String m = ""+s.returnScore();
				if (s.returnScore()==0) {
					m = "X";
					co[0]=255;
				}
				if (s.returnScore()==50) {
					co[0]=200;
					co[1]=100;
				}
				if (s.returnScore()==100) {
					co[1]=255;
				}
				if (s.returnScore()==300) {
					co[1]=200;
					co[2]=255;
					for (int p = 0; p < 3; p++) {
						spawnParticles(2,75+30*p,25,1);
					}
				}
				messages.add(new Msg(s.getCircleX(), s.getCircleY(), m, co));
			}
		}
		for (int i = 0; i < circles.size(); i++) {
			Circle c = circles.get(i);
			if (c.finished) totalScore+=300;
			score += c.returnScore();
			c.update(tick, mouseX, mouseY, press);
			
			//effects
			if (c.completionFrame) {
				int[] co = {0, 0, 0};
				String m = ""+c.returnScore();
				if (c.returnScore()==0) {
					m = "X";
					co[0]=255;
				}
				if (c.returnScore()==50) {
					co[0]=200;
					co[1]=100;
				}
				if (c.returnScore()==100) {
					co[1]=255;
				}
				if (c.returnScore()==300) {
					co[1]=200;
					co[2]=255;
					spawnParticles(3,mouseX,mouseY,1);
					for (int p = 0; p < 3; p++) {
						spawnParticles(2,75+30*p,25,1);
					}
				}
				messages.add(new Msg(c.getX(), c.getY(), m, co));
			}
		}
		//particles
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).update();
			if (!particles.get(i).isActive()) {
				particles.remove(i);
				i--;
			}
		}
		//score
		for (int i = 0; i < messages.size(); i++) {
			messages.get(i).update();
			if (!messages.get(i).isActive()) {
				messages.remove(i);
				i--;
			}
		}
		//update colors
		if (colorChangeIndex<colorChanges.size())
			if (tick == colorChanges.get(colorChangeIndex)[0]) {
				targetColor[0] =  colorChanges.get(colorChangeIndex)[1];
				targetColor[1] =  colorChanges.get(colorChangeIndex)[2];
				targetColor[2] =  colorChanges.get(colorChangeIndex)[3];
				colorChangeIndex++;
			}
		
		if (pulseIndex<pulses.size())
			if (tick == pulses.get(pulseIndex)) {
				color[0] =  Math.min(color[0]+100, 255);
				color[1] =  Math.min(color[1]+100, 255);
				color[2] =  Math.min(color[2]+100, 255);
				pulseIndex++;
			}
		
		for (int i = 0; i < 3; i++) {
			if (Math.abs(color[i]-targetColor[i])>2) {
				color[i] = color[i]+(targetColor[i]-color[i])/20;
			}
		}
		
		//keypress
		if (keys[90]||keys[88]||mouseDown) {
			press = true;
		}
		else {
			press = false;
		}
		if (press&&tick%7==0) spawnParticles(1,mouseX,mouseY,4);
		
		//mouse trail
		if (tick%2==0) {
			trailX[0] = mouseX;
			trailY[0] = mouseY;
			for (int i = trailX.length-1; i > 0; i--) {
				trailX[i] = trailX[i-1];
				trailY[i] = trailY[i-1];
			}
		}
		
		//ending
		if (tick==3600) {
			spawnParticles(5,100,150,1);
			spawnParticles(5,100,650,1);
			spawnParticles(5,700,650,1);
			spawnParticles(5,700,150,1);
			for (int i = 0; i < circles.size(); i++) {
				if (circles.get(i).returnScore()==0) results[0]++;
				if (circles.get(i).returnScore()==50) results[1]++;
				if (circles.get(i).returnScore()==100) results[2]++;
				if (circles.get(i).returnScore()==300) results[3]++;
			}
		}
		
		//update tick
		fulltick+=lagMultiplier;
		tick = (int)fulltick;
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
		circles.add(new Circle(400,400,100,1));
		circles.add(new Circle(500,300,250-13,2));
		circles.add(new Circle(400,300,300-13,3));
		circles.add(new Circle(500,200,350-13,4));
		circles.add(new Circle(100,200,400-13,5));
		
		circles.add(new Circle(400,400,475-3,1));
		circles.add(new Circle(500,300,525-3,2));
		circles.add(new Circle(400,300,575-3,3));
		circles.add(new Circle(500,200,625-3,4));
		circles.add(new Circle(100,200,675-3,5));
		circles.add(new Circle(200,200,725-5,6));
		circles.add(new Circle(300,200,775-7,7));
		circles.add(new Circle(400,400,825-10,8));
		
		
		//theme 1 part 1
		colorChanges.add(new Integer[] {950, 100, 255, 255});
		circles.add(new Circle(100,200,950-5,1));
		sliders.add(new Slider(new int[] {250,350}, new int[] {200,200},975-5, 2));
		circles.add(new Circle(500,200,1025-5,4));
		circles.add(new Circle(400,300,1050-5,5));
		circles.add(new Circle(500,400,1075-5,6));
		circles.add(new Circle(400,500,1100-5,7));
		
		circles.add(new Circle(100,600,1146,1));pulses.add(1150-12);
		sliders.add(new Slider(new int[] {250,350}, new int[] {600,600},1175-5,2));
		circles.add(new Circle(500,600,1225-12,4));
		circles.add(new Circle(400,500,1250-12,5));
		circles.add(new Circle(500,400,1275-12,6));
		circles.add(new Circle(400,300,1300-12,7));
		
		//theme 1 part 2
		colorChanges.add(new Integer[] {1325, 255, 255, 0});
		circles.add(new Circle(100,500,1315,1));pulses.add(1325);
		circles.add(new Circle(100,200,1340,2));pulses.add(1350);
		sliders.add(new Slider(new int[] {250,325}, new int[] {200,200},1360,3));
		circles.add(new Circle(500,200,1407,5));pulses.add(1425);
		circles.add(new Circle(500,400,1425,6));pulses.add(1450);
		sliders.add(new Slider(new int[] {350,250}, new int[] {400,400},1445,7));
		circles.add(new Circle(100,400,1497,9));
		
		circles.add(new Circle(100,300,1550-25,1));pulses.add(1550-6);
		circles.add(new Circle(100,200,1575-25,2));
		sliders.add(new Slider(new int[] {250,450}, new int[] {200,200},1600-25,3));
		circles.add(new Circle(600,200,1660,5));pulses.add(1650+6);
		
		//theme 1 part 1 (again)
		colorChanges.add(new Integer[] {1700, 100, 255, 255});
		int o1 = 750;
		circles.add(new Circle(100,200,950+o1+15,1));pulses.add(950+o1);
		sliders.add(new Slider(new int[] {250,325}, new int[] {200,200},982+o1, 2));
		circles.add(new Circle(500,200,1032+o1,4));
		circles.add(new Circle(400,300,1057+o1,5));pulses.add(1050+o1);
		circles.add(new Circle(500,400,1082+o1,6));
		circles.add(new Circle(400,500,1107+o1,7));pulses.add(1100+o1);
		
		circles.add(new Circle(100,600,1150+o1,1));pulses.add(1150-12+o1);
		sliders.add(new Slider(new int[] {250,325}, new int[] {600,600},1174+o1,2));
		circles.add(new Circle(500,600,1217+o1,4));
		circles.add(new Circle(400,500,1242+o1,5));
		circles.add(new Circle(500,400,1267+o1,6));pulses.add(1275-21+o1);
		circles.add(new Circle(400,300,1292+o1,7));
		circles.add(new Circle(200,350,1317+o1,8));pulses.add(1325-12+o1);
		
		//theme 1 part 3
		colorChanges.add(new Integer[] {2100-12, 255, 255, 0});
		circles.add(new Circle(150,300,2100-12,1));
		circles.add(new Circle(200,250,2125-12,2));
		circles.add(new Circle(250,200,2150-12,3));
		circles.add(new Circle(200,400,2175-12,4));
		
		circles.add(new Circle(300,350,2225,1));pulses.add(2225);
		circles.add(new Circle(400,450,2250,2));
		circles.add(new Circle(400,550,2275,3));
		
		sliders.add(new Slider(new int[] {150,300,225},new int[] {500,500,700},2300,1));
		circles.add(new Circle(300,650,2425,2));pulses.add(2425);
		
		//transition
		colorChanges.add(new Integer[] {2450, 255, 255, 255});
		circles.add(new Circle(300,400,2450,1));
		sliders.add(new Slider(new int[] {400,450}, new int[] {700,500}, 2475, 1));
		circles.add(new Circle(500,650,2550,3));
		sliders.add(new Slider(new int[] {450,400}, new int[] {700,500}, 2575, 4));
		
		circles.add(new Circle(600,600,2660,1));pulses.add(2650);
		circles.add(new Circle(200,200,2685,2));pulses.add(2675);
		circles.add(new Circle(200,600,2710,3));pulses.add(2700);
		circles.add(new Circle(600,200,2735,4));pulses.add(2725);
		circles.add(new Circle(600,600,2760,5));pulses.add(2750);
		circles.add(new Circle(200,200,2785,6));pulses.add(2775);
		circles.add(new Circle(200,600,2810,7));pulses.add(2800);
		circles.add(new Circle(600,200,2835,8));pulses.add(2825);
		
		colorChanges.add(new Integer[] {2862, 255, 255, 0});
		circles.add(new Circle(500,200,2862,1));
		circles.add(new Circle(400,200,2887,2));
		circles.add(new Circle(300,200,2912,3));
		circles.add(new Circle(200,200,2937,4));
		
		circles.add(new Circle(100,200,2962,5));
		circles.add(new Circle(100,300,2987,6));
		circles.add(new Circle(100,400,3012,7));
		circles.add(new Circle(100,500,3025,8));
		
		colorChanges.add(new Integer[] {3050, 100, 255, 255});
		circles.add(new Circle(200,500,3050,1));
		circles.add(new Circle(200,400,3075,2));
		circles.add(new Circle(200,300,3100,3));
		circles.add(new Circle(200,200,3125,4));
		
		circles.add(new Circle(200,100,3150,5));
		circles.add(new Circle(300,100,3175,6));
		circles.add(new Circle(400,100,3200,7));
		circles.add(new Circle(500,100,3225,8));
		
		//ending
		colorChanges.add(new Integer[] {2100-12, 255, 255, 255});
		circles.add(new Circle(600,600,2500+50*16+12+13,1));pulses.add(2500+50*16+12);
		circles.add(new Circle(400,600,2500+50*17+12+6,2));pulses.add(2500+50*17+12);
		circles.add(new Circle(200,600,2500+50*18+12+6,3));pulses.add(2500+50*18+12);
		circles.add(new Circle(400,400,3513,1));pulses.add(3500);
		
		
		//TEMPLATES
		//circles.add(new Circle(200,200,950));
		//sliders.add(new Slider(new int[] {100,200,300}, new int[] {200,250,350}, 250));
		
		
	}
	
	public void drawParticle(Graphics g, int x, int y, double angle) {
		int[] xPoints = new int[10];
		int[] yPoints = new int[10];
		int r = 9;
		for (int i = 0; i < 5; i++) {
			xPoints[i*2] = x + (int)(r*Math.cos(angle+i*0.4*Math.PI));
			yPoints[i*2] = y + (int)(r*Math.sin(angle+i*0.4*Math.PI));
			
			xPoints[i*2+1] = x + (int)(r/2*Math.cos(angle+(i+0.5)*0.4*Math.PI));
			yPoints[i*2+1] = y + (int)(r/2*Math.sin(angle+(i+0.5)*0.4*Math.PI));
		}
		g.drawPolygon(xPoints, yPoints, 10);
	}
	
	public void spawnParticles(int num, int x, int y, int grav) {
		for (int i = 0; i < num; i++) {
			particles.add(new Particle(x,y,grav));
		}
	}
	
	public void endScreen(Graphics g) {
		g.setFont(numberFont);
		g.setColor(new Color(100,100,100));
		g.fillRoundRect(100, 200, 600, 400, 100, 100);
		
		g.setColor(new Color(100, 255, 255));
		g.drawString("300 ",150,275);
		g.setColor(new Color(0, 255, 0));
		g.drawString("100 ",150,375);
		g.setColor(new Color(200, 100, 0));
		g.drawString("50 ",150,475);
		g.setColor(new Color(255, 0, 0));
		g.drawString("X ",150,575);
		
		g.setColor(new Color(255, 255, 255));
		for (int i = 0; i < results.length; i++) {
			g.drawString("x"+results[i],275,575-100*i);
		}
		
		g.setColor(new Color(255, 255, 255));
		int percent = (int) Math.round(100*score/(totalScore+0.1));
		g.drawString(percent+"%", 500, 575);
		
		g.setFont(new Font("Arial",0,250));
		String grade = percent > 95 ? "S" : percent > 85 ? "A" : percent > 75 ? "B" : percent > 67 ? "C" : percent > 55 ? "D" : "F";
		g.drawString(grade, 450, 475);
	}
	
	Timer t;
	@Override
	public void mouseClicked(MouseEvent e) {
		//mouseDown = true;
		
	}
	@Override
	public void mouseMoved(MouseEvent m) {
		m.translatePoint(-6, -31);
		mouseX = m.getX();
		mouseY = m.getY();
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode()<=256) keys[e.getKeyCode()] = true;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode()<=256) keys[e.getKeyCode()] = false;
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
		//System.out.println("Tick: "+tick);
		mouseDown = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mouseDown = false;
	}

	@Override
	public void mouseDragged(MouseEvent m) {
		//mouseDown = true;
		m.translatePoint(-6, -31);
		mouseX =m.getX();
		mouseY =m.getY();
	}

}
