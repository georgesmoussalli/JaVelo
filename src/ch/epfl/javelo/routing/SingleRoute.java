package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Classe immuable qui représente un itinéraire simple, c.-à-d. reliant un point de départ à un point d'arrivée,`
 * sans point de passage intermédiaire. Elle implémente l'interface Route
 *
 * @author Georges Moussalli (316630)
 */
public final class SingleRoute implements Route {

    private final List<Edge> edges;
    private double length;
    private final List<PointCh> points;
    private final double[] list;


    /**
     * Retourne l'itinéraire simple composé des arêtes données
     * @throws  IllegalArgumentException si la liste d'arêtes est vide
     *
     * @param edges liste des arêtes données
     */
    public SingleRoute(List<Edge> edges) {
        Preconditions.checkArgument(edges.size() > 0);
        this.edges = List.copyOf(edges);
        length = 0;
        List<PointCh> temp = new ArrayList<>();

        for (Edge edge : edges) {
            temp.add(edge.fromPoint());
            length += edge.length();
        }
        temp.add(edges.get(edges().size() - 1).toPoint());
        points = List.copyOf(temp);

        list = new double[edges.size() + 1];
        list[0] = 0;
        double maxLength = 0;
        for (int i = 1; i <= edges().size(); i++) {
            maxLength += edges.get(i - 1).length();
            list[i] = maxLength;
        }
    }

    /**
     * Retourne l'index du segment de l'itinéraire contenant la position donnée,
     * qui vaut toujours 0 dans le cas d'un itinéraire simple
     *
     * @param position position du segment
     * @return l'index du segment de l'itinéraire contenant la position donnée,
     * qui vaut toujours 0 dans le cas d'un itinéraire simple
     */

    @Override
    public int indexOfSegmentAt(double position) {
        return 0;
    }

    /**
     * Retourne la longueur de l'itinéraire, en mètres
     *
     * @return la longueur de l'itinéraire, en mètres,
     */

    @Override
    public double length() {
        return length;
    }

    /**
     * retourne la totalité des arêtes de l'itinéraire
     *
     * @return la totalité des arêtes de l'itinéraire
     */

    @Override
    public List<Edge> edges() {
        return edges;
    }

    /**
     * Retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire
     *
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire
     */

    @Override
    public List<PointCh> points() {
        return points;
    }

    /**
     * Retourne le point se trouvant à la position donnée le long de l'itinéraire
     *
     * @param position position le long de l'itinéraire où l'on cherche un point
     * @return le point se trouvant à la position donnée le long de l'itinéraire
     */

    @Override
    public PointCh pointAt(double position) {


        int index = Arrays.binarySearch(list, position);

        if (index == -1) {
            return edges.get(0).fromPoint();
        }
        if (-index - 2 >= edges.size()) {
            return edges.get(edges.size() - 1).toPoint();
        }

        if (index <= 0) {
            if (index <= -2) {
                index = - index - 2;
            }
            return edges.get(index).pointAt(position - list[index]);
        } else {
            return edges.get(index - 1).pointAt(edges.get(index - 1).length());
        }
    }


    /**
     * Retourne l'altitude à la position donnée le long de l'itinéraire,
     * qui peut valoir NaN si l'arête contenant cette position n'a pas de profil
     *
     * @param position position le long de l'itinéraire dont on cherche l'altitude
     * @return l'altitude à la position donnée le long de l'itinéraire,
     * qui peut valoir NaN si l'arête contenant cette position n'a pas de profil
     */

    @Override
    public double elevationAt(double position) {

        double[] list = new double[edges.size() + 1];

        list[0] = 0;
        double maxLength = 0;

        for (int i = 1; i <= edges().size(); i++) {

            maxLength += edges.get(i - 1).length();
            list[i] = maxLength;
        }
        int index = Arrays.binarySearch(list, position);

        if (-index - 2 >= edges.size()) {
            return edges.get(edges.size() - 1).elevationAt(list[edges.size() - 3]);
        }
        if (index == -1) {
            return edges.get(0).elevationAt(0);
        }
        if (index <= 0) {
            if (index <= -2) {
                return edges.get(-index - 2).elevationAt(position - list[-index - 2]);
            }
            return edges.get(0).elevationAt(position);
        } else return edges.get(index - 1).elevationAt(position - list[index - 1]);
    }


    /**
     * Retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     *
     * @param position position le long de l'itinéraire dont on cherche le noeud le plus proche
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     */

    @Override
    public int nodeClosestTo(double position) {

        double[] list = new double[edges.size() + 1];

        list[0] = 0;
        double maxLength = 0;

        for (int i = 1; i <= edges().size(); i++) {
            maxLength += edges.get(i - 1).length();
            list[i] = maxLength;
        }
        int index = Arrays.binarySearch(list, position);

        if (-index - 2 >= edges.size() || index == edges.size()) {
            return edges.get(edges.size() - 1).toNodeId();
        }
        if (index == -1 || index == 0) {
            return edges.get(0).fromNodeId();
        }
        if (index <= -2) {
            if (position - list[-index - 2] <= (edges.get(-index - 2).length()) * 0.5) {
                return edges.get(-index - 2).fromNodeId();
            } else {
                return edges.get(-index - 2).toNodeId();
            }
        } else return edges.get(index - 1).toNodeId();
    }


    /**
     * Retourne le point de l'itinéraire se trouvant le plus proche du point de référence donné
     *
     * @param point point de référence dont on cherche le point de l'itineéraire le pljus proche
     * @return le point de l'itinéraire se trouvant le plus proche du point de référence donné
     */

    @Override
    public RoutePoint pointClosestTo(PointCh point) {

        RoutePoint routePoint = RoutePoint.NONE;
        double somme = 0;

        for (Edge edge : edges) {
            double position = Math2.clamp(0, edge.positionClosestTo(point), edge.length());
            routePoint = routePoint.min(edge.pointAt(position), somme + position, edge.pointAt(position).distanceTo(point));
            somme += edge.length();
        }

        return routePoint;
    }
}