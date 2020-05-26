import java.util.ArrayList;

public class Slider {
	int[] x;
	int[] y;
	int startTime;
	int size;
	int pointsDone;
	int pointsMissed;
	boolean checkForMiss;
	int circleX;
	int circleY;
	int speed;
	boolean active;
	
	public Slider(int[] x, int[] y, int Time) {
		this.x = x;
		this.y = y;
		
		circleX = x[0];
		circleY = y[0];
		pointsDone = 1;
		speed = 5;
		startTime = Time;
		checkForMiss = true;
		pointsMissed = 0;
	}
	
	public void move() {
		int tarX = x[pointsDone];
		int tarY = y[pointsDone];
		double angle = Math.atan2(tarY-y[pointsDone-1], tarX-x[pointsDone-1]);
		circleX += (int)(speed*Math.cos(angle));
		circleY += (int)(speed*Math.sin(angle));
		
		if (Math.sqrt((circleX-tarX)*(circleX-tarX)+(circleY-tarY)*(circleY-tarY))<speed) {
			pointsDone++;
			checkForMiss = true;
		}

	}
	
	public void mouse(int mx, int my, boolean press) {
		if (!checkForMiss) return;
		if (!press||Math.sqrt((circleX-mx)*(circleX-mx)+(circleY-my)*(circleY-my))>100) {
			//System.out.println("DIST: "+Math.sqrt((circleX-mx)*(circleX-mx)+(circleY-my)*(circleY-my)));
			checkForMiss=false;
			pointsMissed++;
		}
	}
	
	public void update(int t, int mx, int my, boolean press) {
		if (t > startTime) active = true;
		if (pointsDone>=x.length) active = false;
		
		if (active) {
			move();
			mouse(mx,my,press);
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public int[] getX() {
		return x;
	}
	public int[] getY() {
		return y;
	}
	public int returnScore() {
		System.out.println("p missed "+pointsMissed+"/"+pointsDone);
		if (active) return -1;
		if (pointsMissed==0) return 300;
		else if (1-pointsMissed/pointsDone>=0.66) return 100;
		else if (1-pointsMissed/pointsDone>=0.33) return 50;
		else return 0;
	}
	

}
