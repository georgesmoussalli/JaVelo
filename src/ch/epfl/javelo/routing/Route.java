package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;

import java.util.List;

/**
 * Représente un itinéraire
 *
 * @author Georges Moussalli (316630)
 */
public interface Route {

    /**
     * Retourne l'index du segment à la position donnée (en mètres)
     *
     * @param position position du segment
     * @return l'index du segment à la position donnée (en mètres)
     */
     int indexOfSegmentAt(double position);

    /**
     * Retourne la longueur de l'itinéraire, en mètres
     *
     * @return la longueur de l'itinéraire, en mètres
     */
     double length();

    /**
     * Retourne la totalité des arêtes de l'itinéraire
     *
     * @return la totalité des arêtes de l'itinéraire
     */
     List<Edge> edges();

    /**
     * Retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire
     *
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */
     List<PointCh> points();

    /**
     * Retourne le point se trouvant à la position donnée le long de l'itinéraire
     *
     * @param position position le long de l'itinéraire où l'on cherche un point
     * @return le point se trouvant à la position donnée le long de l'itinéraire
     */
     PointCh pointAt(double position);

    /**
     * Retourne l'altitude à la position donnée le long de l'itinéraire
     *
     * @param position position le long de l'itinéraire dont on cherche l'altitude
     * @return l'altitude à la position donnée le long de l'itinéraire
     */
     double elevationAt(double position);

    /**
     * Retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     *
     * @param position position le long de l'itinéraire dont on cherche le noeud le plus proche
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     */
     int nodeClosestTo(double position);

    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donné
     *
     * @param point point de référence dont on cherche le point de l'itineéraire le pljus proche
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné.
     */
     RoutePoint pointClosestTo(PointCh point);
}
