package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;

import java.util.function.DoubleUnaryOperator;

/**
 * Représente une arête d'un itinéraire.
 *
 * @author Georges Moussalli (316630)
 */
public record Edge
        (int fromNodeId, int toNodeId, PointCh fromPoint, PointCh toPoint,
         double length, DoubleUnaryOperator profile) {


    /**
     * Retourne une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés,
     * les autres étant ceux de l'arête d'identité edgeId dans le graphe Graph
     *
     * @param graph      graphe
     * @param edgeId     identité de l'arête
     * @param fromNodeId identité du noeud de départ
     * @param toNodeId   identité du noeud d'arrivée
     * @return une instance de Edge dont les attributs fromNodeId et toNodeId sont ceux donnés,
     * les autres étant ceux de l'arête d'identité edgeId dans le graphe Graph
     */
    public static Edge of(Graph graph, int edgeId, int fromNodeId, int toNodeId) {


        return new Edge(fromNodeId, toNodeId, graph.nodePoint(fromNodeId),
                graph.nodePoint(toNodeId), graph.edgeLength(edgeId), graph.edgeProfile(edgeId));
    }


    /**
     * Retourne la position le long de l'arête, en mètres, qui se trouve la plus proche du point donné
     *
     * @param point point dont on cherche la position la plus proche le long de l'arête
     * @return la position le long de l'arête, en mètres, qui se trouve la plus proche du point donné,
     */
    public double positionClosestTo(PointCh point) {
        return Math2.projectionLength(fromPoint.e(), fromPoint.n(), toPoint.e(), toPoint.n(), point.e(), point.n());
    }

    /**
     * Retourne le point se trouvant à la position donnée sur l'arête, exprimée en mètres
     *
     * @param position position donnée sur l'arête
     * @return le point se trouvant à la position donnée sur l'arête, exprimée en mètres
     */
    public PointCh pointAt(double position) {
        double ratio = (position / length);
        double e = Math2.interpolate(fromPoint.e(), toPoint.e(), ratio);
        double n = Math2.interpolate(fromPoint.n(), toPoint.n(), ratio);
        return new PointCh(e, n);

    }

    /**
     * Retourne l'altitude, en mètres, à la position donnée sur l'arête
     *
     * @param position
     * @return l'altitude, en mètres, à la position donnée sur l'arête
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }

}
