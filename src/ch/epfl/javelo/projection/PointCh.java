package ch.epfl.javelo.projection;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

/**
 * Représente un point dans le système de coordonnées suisse
 *
 * @author Georges Moussalli (316630)
 */

public record PointCh(double e, double n) {

    /**
     * Constructeur d'un point
     *
     * @param e Coordonnée est du point
     * @param n Coordonnée nord du point
     * @throws IllegalArgumentException si les coordonnées du point sont hors du champ
     */

    public PointCh {
        Preconditions.checkArgument(SwissBounds.containsEN(e, n));
    }

    /**
     * Retourne la distance au carré séparant deux points
     *
     * @param that point dont évalue la distance par rapport
     * @return La distance au carré
     */

    public double squaredDistanceTo(PointCh that) {
        return Math2.squaredNorm(that.e - this.e, that.n - this.n);
    }

    /**
     * Retourne la distance séparant deux points
     *
     * @param that point dont évalue la distance par rapport
     * @return la distance
     */

    public double distanceTo(PointCh that) {
        return Math2.norm(that.e - this.e, that.n - this.n);
    }

    /**
     * Retourne la longitude d'un point aux coordonnées métriques connues en WGS84
     *
     * @return la longitude d'un point aux coordonnées métriques connues en WGS84
     */

    public double lon() {

        return Ch1903.lon(e, n);
    }

    /**
     * Retourne la latitude d'un point aux coordonnées métriques connues en WGS84
     *
     * @return la latitude d'un point aux coordonnées métriques connues en WGS84
     */

    public double lat() {

        return Ch1903.lat(e, n);
    }

}
