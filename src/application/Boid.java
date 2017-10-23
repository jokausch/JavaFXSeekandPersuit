package application;

import javafx.scene.paint.Color;
import java.util.ArrayList;

public class Boid {

    double[] xPoints, yPoints;
    PVector speed;
    PVector position;
    PVector acceleration = new PVector(0, 0);
    float maxspeed = 250;
    float maxforce = 10;
    float width = 40;
    static float distanceToBoarder = 20;
    int red = 0, green = 0, blue = 0;
    Color col;
    boolean isDead = false;
    float seekAhead;
    boolean isInSight=false;

    public Boid() {


    }

    public Boid(PVector position, PVector speed) {

        this.col = Color.rgb(red, green, blue, 1);

        this.speed = speed;
        this.position = position;

        this.xPoints = new double[3];
        this.yPoints = new double[3];

        this.xPoints[0] = -6;
        this.yPoints[0] = -6;
        this.xPoints[1] = 12;
        this.yPoints[1] = 0;
        this.xPoints[2] = -6;
        this.yPoints[2] = 6;
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
        //Main.g.setEffect(Main.ds);
        Main.g.setFill(col);

        Main.g.setLineWidth(2);
        Main.g.translate(this.position.x, this.position.y);
        Main.g.rotate(Math.toDegrees(this.speed.heading()));

        Main.g.fillPolygon(this.xPoints, this.yPoints, this.xPoints.length);
        Main.g.restore();

    }


    public void applyForce(PVector f) {

        this.acceleration.add(f);
    }



    void flock(ArrayList<Boid> target, float weight) {

        separate(target, weight);
        align(target,weight);
        cohesion(target,weight);
    }


    void separate(ArrayList<Boid> boids, float weight) {

        float desiredseparation = this.width;
        PVector sum = new PVector();
        final int[] count = {0};
        // For every boid in the system, check if it's too close
        boids.stream().forEach(e -> {
            float d = PVector.dist(position, e.position);
            if ((d > 0) && (d < desiredseparation)) {
                // Calculate vector pointing away from neighbor
                PVector diff = PVector.sub(position, e.position);
                diff.normalize();
                diff.div(d);        // Weight by distance
                sum.add(diff);
                count[0]++;            // Keep track of how many
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

    void fleeHunter(ArrayList<BoidHunter> boids, float weight) {

        float desiredseparation = 80;
        PVector sum = new PVector();
        final int[] count = {0};
        // For every boidHunter in the system, check if it's too close
        boids.stream().forEach(e -> {
            float d = PVector.dist(position, e.position);
            seekAhead= d / maxspeed;
            PVector futurePosition = e.position.copy().add(e.speed.copy().mult(seekAhead));
            if ((d > 0) && (d < desiredseparation)) {
                // Calculate vector pointing away from hunter
                PVector diff = PVector.sub(position, futurePosition);
                diff.normalize();
                diff.div(d);        // Weight by distance
                sum.add(diff);
                count[0]++;            // Keep track of how many
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

    // Alignment
    // For every nearby boid in the system, calculate the average velocity
    void align (ArrayList<Boid> boids, float weight) {
        float neighbordist = 40;
        PVector sum = new PVector(0,0);
        int count = 0;
        for (Boid other : boids) {
            float d = PVector.dist(position,other.position);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.speed);
                count++;
            }
        }
        if (count > 0) {
            sum.div((float)count);
            sum.normalize();
            sum.mult(maxspeed);
            PVector steer = PVector.sub(sum,speed);
            steer.limit(maxforce);
        }
        this.applyForce(sum.mult(weight));
    }

    // Cohesion
    // For the average position (i.e. center) of all nearby boids, calculate steering vector towards that position
    void cohesion (ArrayList<Boid> boids, float weight) {
        float neighbordist = 50;
        PVector sum = new PVector(0,0);   // Start with empty vector to accumulate all positions
        int count = 0;
        for (Boid other : boids) {
            float d = PVector.dist(position,other.position);
            if ((d > 0) && (d < neighbordist)) {
                sum.add(other.position); // Add position
                count++;
            }
        }
        if (count > 0) {
            sum.div(count);
            this.applyForce(seek(sum).mult(weight));  // Steer towards the position
        }
    }

    PVector seek(PVector target) {
        PVector desired = PVector.sub(target,position);  // A vector pointing from the position to the target
        // Normalize desired and scale to maximum speed
        desired.normalize();
        desired.mult(maxspeed);
        // Steering = Desired minus Velocity
        PVector steer = PVector.sub(desired,speed);
        steer.limit(maxforce);  // Limit to maximum steering force
        return steer;
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

	public void highlight() {
		  this.col = Color.rgb(255, 0, 0, 1);
		
	}
}
