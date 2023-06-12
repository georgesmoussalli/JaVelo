package ch.epfl.javelo.projection;

import java.lang.Math;

/**
 * Permet de convertir entre les coordonnées WGS 84 et les coordonnées suisses.
 *
 * @author Georges Moussalli (316630)
 */
public final class Ch1903 {


    /**
     * Contructeur privé de la classe
     */
    private Ch1903() {
    }

    /**
     * Retourne une coordonnée métrique d'un point
     *
     * @param lon longitude du point
     * @param lat latitude du point
     * @return Coordonnée est du point
     */
    public static double e(double lon, double lat) {
        double lon1 = Math.pow(10.0, -4.0) * (3600 * Math.toDegrees(lon) - 26782.5);
        double lat1 = Math.pow(10.0, -4.0) * (3600 * Math.toDegrees(lat) - 169028.66);

        return 2600072.37 + 211455.93 * lon1 - 10938.51 * lon1 * lat1
                - 0.36 * lon1 * Math.pow(lat1, 2.0) - 44.54 * Math.pow(lon1, 3.0);
    }

    /**
     * Retourne une coordonnée métrique d'un point
     *
     * @param lon longitude du point
     * @param lat latitude du point
     * @return Coordonnée nord du point
     */
    public static double n(double lon, double lat) {
        double lon1 = Math.pow(10.0, -4.0) * (3600 * Math.toDegrees(lon) - 26782.5);
        double lat1 = Math.pow(10.0, -4.0) * (3600 * Math.toDegrees(lat) - 169028.66);

        return 1200147.07 + 308807.95 * lat1 + 3745.25 * Math.pow(lon1, 2.0)
                + 76.63 * Math.pow(lat1, 2.0) - 194.56 * Math.pow(lon1, 2.0) * lat1 +
                119.79 * Math.pow(lat1, 3.0);
    }

    /**
     * retourne la longitude d'un point en radians
     *
     * @param e coordonnée est du point
     * @param n coordonnée nord du point
     * @return la longitude du point en radians
     */
    public static double lon(double e, double n) {
        double x = Math.pow(10.0, -6.0) * (e - 2600000);
        double y = Math.pow(10.0, -6.0) * (n - 1200000);
        double lon0 = 2.6779094 + 4.728982 * x + 0.791484 * x * y
                + 0.1306 * x * Math.pow(y, 2.0) - 0.0436 * Math.pow(x, 3.0);

        return Math.toRadians(100 * lon0 / 36.0);
    }

    /**
     * Retourne la latitude d'un point en radians
     *
     * @param e coordonnée est du point
     * @param n coordonnée nord du point
     * @return la latitude d'un point en radians
     */
    public static double lat(double e, double n) {
        double x = Math.pow(10.0, -6) * (e - 2600000);
        double y = Math.pow(10.0, -6) * (n - 1200000);
        double lat0 = (16.9023892 + 3.238272 * y - 0.270978 * Math.pow(x, 2.0)
                - 0.002528 * Math.pow(y, 2.0) - 0.0447 * Math.pow(x, 2.0) * y
                - 0.0140 * Math.pow(y, 3.0));

        return Math.toRadians(100 * lat0 / 36.0);
    }
}
