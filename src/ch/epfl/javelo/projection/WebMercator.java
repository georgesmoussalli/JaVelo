package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;

import java.lang.Math;

/**
 * Permet de convertir entre les coordonnées WGS 84 et les coordonnées WebMercator.
 *
 * @author Georges Moussalli (316630)
 */

public final class WebMercator {

    /**
     * Constructeur privé de la classe qui est donc non instanciable
     */
    private WebMercator() {
    }

    /**
     * Convertit la longitude en coordonnée WebMercator
     *
     * @param lon longitude à convertir
     * @return la coordonnée x en WebMercator
     */
    public static double x(double lon) {
        return (1.0 / 2.0) * (1.0 / Math.PI) * (lon + Math.PI);
    }

    /**
     * Convertit la lat en coordonnée WebMercator
     *
     * @param lat longitude à convertir
     * @return la coordonnée y en WebMercator
     */
    public static double y(double lat) {
        return (1.0 / 2.0) * (1.0 / Math.PI) * (Math.PI - Math2.asinh(Math.tan(lat)));
    }

    /**
     * Convertit la coordonnée x en WebMercator en longitude
     *
     * @param x coordonnée WebMercator à convertir
     * @return la longitude en WGS 84
     */
    public static double lon(double x) {
        return 2.0 * Math.PI * x - Math.PI;
    }

    /**
     * Convertit la coordonnée x en WebMercator en longitude
     *
     * @param y coordonnée WebMercator à convertir
     * @return la latitude en WGS 84
     */
    public static double lat(double y) {
        return Math.atan(Math.sinh(Math.PI - 2.0 * Math.PI * y));
    }
}
