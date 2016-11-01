package pr0j3ct;

public class Projector {
    public static final double ZFAR = 1;
    public static final double ZFACT = 1;

    private final int[] dim;
    private final int[] dim2;

    public Projector(int[] dimensions) {
        this.dim = dimensions;
        this.dim2 = new int[]{dim[0] / 2, dim[1] / 2};
    }

    //returns false if the object can be removed.
    public boolean project(ShootTarget o) {
        double f = (ZFAR + ZFACT * o.coords[2]);
        o.realCoords[0] = o.coords[0] / f;
        o.realCoords[1] = o.coords[1] / f;
        o.realRadius = o.radius / f;

        boolean visible = (o.realCoords[0] + o.realRadius >= -dim2[0]
                && o.realCoords[0] - o.realRadius <= dim2[0]
                && o.realCoords[1] + o.realRadius >= -dim2[1]
                && o.realCoords[1] - o.realRadius <= dim2[1]);

        //now make realCoords real
        o.realCoords[0] += dim2[0];
        o.realCoords[1] = dim2[1] - o.realCoords[1];

        boolean obsolete = false;

        if (visible) {
            o.wasVisible = true;
        } else if (o.wasVisible) {
            obsolete = true;
        }

        return !obsolete;
    }
}
