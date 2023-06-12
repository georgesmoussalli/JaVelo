package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

/**
 * Représente le point d'un itinéraire le plus proche d'un point de référence donné,
 * qui se trouve dans le voisinage de l'itinéraire
 * @Constant NONE qui représente un point inexistant
 *
 * @author Georges Moussalli (316630)
 */
public record RoutePoint(PointCh point, double position, double distanceToReference) {

    public static final RoutePoint NONE = new RoutePoint(null, NaN, POSITIVE_INFINITY);

    /**
     * Retourne un point identique au récepteur (this) mais dont la position est décalée de la différence donnée,
     * qui peut être positive ou négative
     *
     * @param positionDifference décalage donnée
     * @return un point identique au récepteur (this) mais dont la position est décalée de la différence donnée,
     * qui peut être positive ou négative,
     */
    public RoutePoint withPositionShiftedBy(double positionDifference) {
        if (positionDifference == 0) return this;
        else return new RoutePoint(this.point, position + positionDifference, distanceToReference);
    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     *
     * @param that point de distance à la référence minimale
     * @return this si sa distance à la référence est inférieure ou égale à celle de that, et that sinon
     */
    public RoutePoint min(RoutePoint that) {
       return this.distanceToReference <= that.distanceToReference ? this : that;

    }

    /**
     * Retourne this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     *
     * @param thatPoint               point de distance à la référence thatDistanceToReference
     * @param thatPosition            position du point de distance à la référence thatDistanceToReference
     * @param thatDistanceToReference distance à la référence du point en argument
     * @return this si sa distance à la référence est inférieure ou égale à thatDistanceToReference,
     * et une nouvelle instance de RoutePoint dont les attributs sont les arguments passés à min sinon
     */
    public RoutePoint min(PointCh thatPoint, double thatPosition, double thatDistanceToReference) {
        return (this.distanceToReference <= thatDistanceToReference) ?
                this : new RoutePoint(thatPoint, thatPosition, thatDistanceToReference);
    }


}
