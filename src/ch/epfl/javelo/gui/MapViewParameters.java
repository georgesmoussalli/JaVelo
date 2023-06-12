package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointWebMercator;
import javafx.geometry.Point2D;

/**
 * Représente les paramètres du fond de carte présenté dans l'interface graphique
 *
 * @author Georges Moussalli (316630)
 */
public record MapViewParameters(int zoomLevel, double x, double y) {


    /**
     * @return qui retourne les coordonnées du coin haut-gauche sous la forme d'un objet de type Point2D
     * le type utilisé par JavaFX pour représenter les points
     */
    public Point2D topLeft() {
        return new Point2D(x, y);
    }

    /**
     * Retourne une instance de MapViewParameters identique au récepteur,
     * si ce n'est que les coordonnées du coin haut-gauche sont celles passées en arguments à la méthode
     * @param x cordoonée x
     * @param y coordonnée y
     * @return retourne une instance de MapViewParameters identique au récepteur,
     * si ce n'est que les coordonnées du coin haut-gauche sont celles passées en arguments à la méthode
     */
    public MapViewParameters withMinXY(double x, double y) {
        return new MapViewParameters(zoomLevel, x, y);
    }

    /**
     *  Prend en arguments les coordonnées x et y d'un point,
     *  exprimées par rapport au coin haut-gauche de la portion de carte affichée à l'écran,
     *  et retourne ce point sous la forme d'une instance de PointWebMercator
     * @param a la coordonnée x d'un point, exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran
     * @param b la coordonnée y d'un point, exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran
     * @return Retourne ce point sous la forme d'une instance de PointWebMercator
     */
    public PointWebMercator pointAt(double a, double b) {
        return PointWebMercator.of(zoomLevel, x + a, y + b);
    }

    /**
     * Prend en argument un point Web Mercator et retourne la position x correspondante,
     * exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran
     * @param point point Web Mercator
     * @return
     */
    public double viewX(PointWebMercator point) {
        return point.xAtZoomLevel(zoomLevel) - x;
    }

    /**
     * Prend en argument un point Web Mercator et retourne la position y correspondante,
     * exprimée par rapport au coin haut-gauche de la portion de carte affichée à l'écran
     * @param point point Web Mercator
     * @return
     */
    public double viewY(PointWebMercator point) {
        return point.yAtZoomLevel(zoomLevel) - y;
    }
}
