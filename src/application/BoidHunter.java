package application;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;

public class BoidHunter {

	double[] xPoints, yPoints;
	ArrayList<Boid> boidInSightList = new ArrayList();
	PVector speed;
	PVector position;
	PVector acceleration = new PVector(0, 0);
	float maxspeed = 300;
	float maxforce = 50;
	float width = 80;
	static float distanceToBoarder = 20;
	int red = 255, green = 0, blue = 0;
	Color col;
	boolean isDead = false;
	float seekAhead;
	float sightDistance = 200;
	float periphery = (float) (Math.PI / 4);

	public BoidHunter() {

	}

	public BoidHunter(PVector position, PVector speed) {

		this.col = Color.rgb(red, green, blue, 1);

		this.speed = speed;
		this.position = position;

		this.xPoints = new double[3];
		this.yPoints = new double[3];

		this.xPoints[0] = -12;
		this.yPoints[0] = -12;
		this.xPoints[1] = 24;
		this.yPoints[1] = 0;
		this.xPoints[2] = -12;
		this.yPoints[2] = 12;
	}

	public void update(double time) {
		this.speed.add(this.acceleration);
		this.speed.limit(maxspeed);
		this.position.add(this.speed.copy().mult((float) time));

		this.acceleration.mult(0);
	}

	public void updateAndDraw(double time) {
		update(time);
		draw();
	}

	public void draw() {

		Main.g.save();
		Main.g.setEffect(Main.ds);
		Main.g.setFill(col);
		Main.g.setStroke(Color.WHITE);
		Main.g.setLineWidth(2);

		Main.g.translate(this.position.x, this.position.y);
		Main.g.rotate(Math.toDegrees(this.speed.heading()));

		Main.g.fillPolygon(this.xPoints, this.yPoints, this.xPoints.length);
		if (Main.debug) Main.g.strokeArc(-sightDistance, -sightDistance, sightDistance * 2, sightDistance * 2, -Math.toDegrees(periphery),
				2 * Math.toDegrees(periphery), ArcType.ROUND);

		Main.g.restore();

	}

	public void applyForce(PVector f) {

		this.acceleration.add(f);
	}

	void seekTarget(ArrayList<Boid> target) {

		PVector sum = new PVector();
		final int[] count = { 0 };
		target.stream().forEach(e -> {
			float d = PVector.dist(e.position, this.position);
			if (d < 10) {

				e.isDead = true;
				if (this.blue < 200)
					this.col = Color.rgb(red, green, blue += 50, 1);

			} else {
				// Calculate vector pointing to target
				PVector diff = PVector.sub(e.position, position);
				diff.normalize();
				diff.div(d); // Weight by distance
				sum.add(diff);
				count[0]++; // Keep track of how many
			}
		});
		// Average -- divide by how many
		if (count[0] > 0) {
			sum.div(count[0]);
			// desired vector is the average scaled to maximum speed
			sum.normalize();
			sum.mult(maxspeed);
			// Implement Reynolds: Steering = Desired - Velocity
			sum.sub(speed);
			sum.limit(maxforce);
		}
		this.applyForce(sum);
	}

	void persuit(ArrayList<Boid> target, float weight) {

		PVector sum = new PVector();
		final int[] count = { 0 };
		target.stream().forEach(e -> {
			float d = PVector.dist(e.position, this.position);
			seekAhead = d / maxspeed;
			PVector futurePosition = e.position.copy().add(e.speed.copy().mult(seekAhead));
			if (d < 10) {

				e.isDead = true;
				if (this.blue < 200)
					this.col = Color.rgb(red, green, blue += 50, 1);

			} else {
				// Calculate vector pointing to target
				PVector diff = PVector.sub(futurePosition, position);
				diff.normalize();
				diff.div(d); // Weight by distance
				sum.add(diff);
				count[0]++; // Keep track of how many
			}
		});
		// Average -- divide by how many
		if (count[0] > 0) {
			sum.div(count[0]);
			// desired vector is the average scaled to maximum speed
			sum.normalize();
			sum.mult(maxspeed);
			// Implement Reynolds: Steering = Desired - Velocity
			sum.sub(speed);
			sum.limit(maxforce);
		}
		this.applyForce(sum.mult(weight));

	}

	void separate(ArrayList<BoidHunter> boidHunters, float weight) {

		float desiredseparation = this.width;
		PVector sum = new PVector();
		final int[] count = { 0 };
		// For every boid in the system, check if it's too close
		boidHunters.stream().forEach(e -> {
			float d = PVector.dist(position, e.position);
			if ((d > 0) && (d < desiredseparation)) {
				// Calculate vector pointing away from neighbor
				PVector diff = PVector.sub(position, e.position);
				diff.normalize();
				diff.div(d); // Weight by distance
				sum.add(diff);
				count[0]++; // Keep track of how many
			}
		});
		// Average -- divide by how many
		if (count[0] > 0) {
			sum.div(count[0]);
			// Our desired vector is the average scaled to maximum speed
			sum.normalize();
			sum.mult(maxspeed);
			// Implement Reynolds: Steering = Desired - Velocity
			sum.sub(speed);
			sum.limit(maxforce);
		}
		this.applyForce(sum.mult(weight));

	}

	// View
	// move laterally away from any boid that blocks the view
	// Right now we are just drawing the view and highlighting boids
	void viewAndPersuit(ArrayList<Boid> boids) {

		// How far can it see? sightDistance / periphery

		for (Boid other : boids) {
			// A vector that points to another boid and that angle
			PVector comparison = PVector.sub(other.position, this.position);

			// How far is it
			float d = PVector.dist(this.position, other.position);

			// What is the angle between the other boid and this one's current
			// direction
			float diff = PVector.angleBetween(comparison, this.speed);

			// If it's within the periphery and close enough to see it
			if (diff < periphery && d > 0 && d < sightDistance) {
				// Just change its color
				other.highlight();
				other.isInSight = true;
				boidInSightList.add(other);
			} else
				other.isInSight = false;
		}
		persuit(boidInSightList,0.8f);
		boidInSightList.clear();
	}

	void boundaries(float canvasWidth, float canvasHeight, float weight) {

		PVector desired = null;

		if (this.position.x < distanceToBoarder) {
			desired = new PVector(maxspeed, this.speed.y);
		} else if (this.position.x > canvasWidth - distanceToBoarder) {
			desired = new PVector(-maxspeed, this.speed.y);
		}

		if (position.y < distanceToBoarder) {
			desired = new PVector(this.speed.x, maxspeed);
		} else if (this.position.y > canvasHeight - distanceToBoarder) {
			desired = new PVector(this.speed.x, -maxspeed);
		}

		if (desired != null) {
			desired.normalize();
			desired.mult(maxspeed);
			PVector steer = PVector.sub(desired, this.speed);
			steer.limit(maxforce);
			applyForce(steer.mult(weight));
		}
	}
}
