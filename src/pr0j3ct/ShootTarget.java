package pr0j3ct;

import java.util.Random;

public class ShootTarget implements Comparable<ShootTarget> {
    public static final int R_MIN = 30;
    public static final int R_DELTA = 20;
    public static final double SPEED_MIN = 1;
    public static final double SPEED_DELTA = 3;
    public static final double EDGE_DIST = 0.2;
    public static final double ZSPEED = 0.004;
    public static final double ANGSPEED_MAX = 0.02;
    public static final double FRICTION = 0.92;
    public static final double GRAVITY = 0.25;

    private final Random rand = new Random();

    public int type;
    public double[] initialCoords;
    public double[] coords;
    public double[] speedVector;
    public double angularSpeed = 0;
    public double angle = 0;
    public double distanceToTravel;
    public int radius;
    public boolean wasVisible = false;
    public boolean shot = false;

    public double[] realCoords = new double[]{0, 0};
    public double realRadius = 0;

    public ShootTarget(int[] dimensions) {
        type = rand.nextInt(Pr0j3ctComponent.NUM_TYPES);
        int coord = rand.nextInt(2);
        int sign = 2 * rand.nextInt(2) - 1;

        radius = R_MIN + rand.nextInt(R_DELTA);
        double edgePercentage = EDGE_DIST + rand.nextDouble() * (1-2*EDGE_DIST);

        coords = new double[]{0, 0, 0};

        coords[coord] = radius + (dimensions[coord] - 2 * radius) * edgePercentage
                - dimensions[coord] / 2;
        coords[1 - coord] = sign * (dimensions[1 - coord] / 2 + radius);

        double speed = SPEED_MIN + rand.nextDouble() * SPEED_DELTA;
        speedVector = new double[]{0, 0, 0};

        speedVector[1 - coord] = -sign * speed;

        initialCoords = new double[]{coords[0], coords[1]};
        distanceToTravel = dimensions[1 - coord];
    }

    public void shoot() {
        shot = true;
        speedVector[2] = ZSPEED;
        angularSpeed = ANGSPEED_MAX - 2*ANGSPEED_MAX*rand.nextDouble();
    }

    public void update() {
        if (shot) {
            speedVector[0] *= FRICTION;
        }

        for (int t = 0; t < 3; t++) {
            coords[t] += speedVector[t];
        }

        angle += angularSpeed;

        if (shot) {
            speedVector[1] -= GRAVITY;
        }
    }

    public boolean isOver() {
        return !shot && (Math.abs(initialCoords[0] - coords[0])
                + Math.abs(initialCoords[1] - coords[1]) > distanceToTravel);
    }

    public double getTravelPercentage() {
        return shot ? 0 : ((Math.abs(initialCoords[0] - coords[0])
                + Math.abs(initialCoords[1] - coords[1])) / distanceToTravel);
    }

    @Override
    public int compareTo(ShootTarget t) {
        int c1 = Double.compare(t.coords[2], this.coords[2]);
        if (c1 == 0) {
            c1 = Double.compare(this.radius, t.radius);
        }
        return c1;
    }
}
