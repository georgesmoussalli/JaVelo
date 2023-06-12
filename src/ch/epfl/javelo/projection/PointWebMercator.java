package ch.epfl.javelo.projection;

import ch.epfl.javelo.Preconditions;

/**
 * Représente un point dans le système Web Mercator.
 *
 * @author Georges Moussalli (316630)
 */
public record PointWebMercator(double x, double y) {

    public final static int ZOOM = 8;


    /**
     * Constructeur de point en coordonnées Web Mercator
     *
     * @param x Coordonnée x en Web Mercator
     * @param y Coordonnée y en Web Mercator
     * @throws IllegalArgumentException si x ou y ne sont pas compris entre 0 et 1
     */
    public PointWebMercator {
        Preconditions.checkArgument(0 <= x && x <= 1 && 0 <= y && y <= 1);
    }

    /**
     * Ramène un point dont le niveau de zoom est connu
     * en coordonnées initiales WebMercator comprises entre 0 et 1
     *
     * @param zoomLevel niveau de zoom
     * @param x         Coordonnée x en Web Mercator
     * @param y         Coordonnée y en Web Mercator
     * @return Point de zoom 1 avec les coordonnées en zoom 1
     */
    public static PointWebMercator of(int zoomLevel, double x, double y) {
        return new PointWebMercator(Math.scalb(x, -(ZOOM + zoomLevel)),
                Math.scalb(y, -(ZOOM + zoomLevel)));
    }

    /**
     * Construit un point de coordonnées WebMercator depuis un point de coordonnées suisses
     *
     * @param pointCh Point en coordonnées suisses
     * @return Point de coordonnées WebMercator correspondant
     */
    public static PointWebMercator ofPointCh(PointCh pointCh) {
        return new PointWebMercator(WebMercator.x(pointCh.lon()),
                WebMercator.y(pointCh.lat()));
    }

    /**
     * Applique un zoom sur la coordonnée x
     *
     * @param zoomLevel niveau de zoom
     * @return x au zoom donné
     */
    public double xAtZoomLevel(int zoomLevel) {
        return Math.scalb(x, ZOOM + zoomLevel);
    }

    /**
     * Applique un zoom sur la coordonnée y
     *
     * @param zoomLevel niveau de zoom
     * @return x au zoom donné
     */
    public double yAtZoomLevel(int zoomLevel) {
        return Math.scalb(y, ZOOM + zoomLevel);
    }

    /**
     * Convertit la coordonnée x en WebMercator en longitude
     *
     * @return la longitude en WGS 84
     */
    public double lon() {
        return WebMercator.lon(x);
    }

    /**
     * Convertit la coordonnée x en WebMercator en latitude
     *
     * @return la latitude en WGS 84
     */
    public double lat() {
        return WebMercator.lat(y);
    }

    /**
     * Retourne le point de coordonnées suisses se trouvant à la même position que le récepteur
     * ou null si ce point n'est pas dans les limites de la Suisse définies par SwissBounds
     *
     * @return le point de coordonnées suisses se trouvant à la même position que le récepteur
     * ou null si ce point n'est pas dans les limites de la Suisse définies par SwissBounds
     */
    public PointCh toPointCh() {
        double e = Ch1903.e(WebMercator.lon(this.x), WebMercator.lat(this.y));
        double n = Ch1903.n(WebMercator.lon(this.x), WebMercator.lat(this.y));
        if (SwissBounds.containsEN(e, n)) {
            return new PointCh(e, n);
        } else {
            return null;
        }

    }

}
