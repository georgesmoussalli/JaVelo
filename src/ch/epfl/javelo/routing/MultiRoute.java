package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.projection.PointCh;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente un itinéraire multiple, c.-à-d. composé d'une séquence d'itinéraires contigus nommés segments.
 * Elle implémente l'interface Route
 *
 * @author Georges Moussalli (316630)
 */

public final class MultiRoute implements Route {

    private final List<Route> segments;
    private final Route lastRoute;

    /**
     * Construit un itinéraire multiple composé des segments donnés,
     * ou lève IllegalArgumentException si la liste des segments est vide
     *
     * @param segments séquence d'itinéraires contigus
     */
    public MultiRoute(List<Route> segments) {
        Preconditions.checkArgument(!segments.isEmpty());
        this.segments = List.copyOf(segments);
        this.lastRoute = segments.get(segments.size()-1);

    }

    /**
     * Retourne l'index du segment de l'itinéraire contenant la position donnée
     *
     * @param position position du segment
     * @return l'index du segment de l'itinéraire contenant la position donnée
     */
    @Override
    public int indexOfSegmentAt(double position) {
        double length = 0;
        int iteration = 0;
        int indexSegment = 0;
        double clampedDiff = Math2.clamp(0, position, this.length()) - length;


        while (clampedDiff > 0) {
            if (clampedDiff <= segments.get(iteration).length()) {
                indexSegment += segments.get(iteration).indexOfSegmentAt
                        (clampedDiff);
            } else {
                indexSegment += segments.get(iteration).indexOfSegmentAt(segments.get(iteration).length()) + 1;
            }
            clampedDiff -= segments.get(iteration).length();
            iteration++;
        }
        return indexSegment;
    }

    /**
     * Retourne la longueur de l'itinéraire, en mètres
     *
     * @return la longueur de l'itinéraire, en mètres
     */
    @Override
    public double length() {
        double length = 0;
        for (Route r : segments) {
            length += r.length();
        }
        return length;
    }

    /**
     * Retourne la totalité des arêtes de l'itinéraire
     *
     * @return la totalité des arêtes de l'itinéraire
     */
    @Override
    public List<Edge> edges() {
        ArrayList<Edge> edges = new ArrayList<>();
        for (Route r : segments) {
            edges.addAll(r.edges());
        }
        return edges;
    }

    /**
     * Retourne la totalité des points situés aux extrémités des arêtes de l'itinéraire, sans doublons
     *
     * @return la totalité des points situés aux extrémités des arêtes de l'itinéraire, sans doublons
     */
    @Override
    public List<PointCh> points() {
        ArrayList<PointCh> points = new ArrayList<>();
        for (int i = 0; i < segments.size(); i++) {
            points.addAll(segments.get(i).points());
            if (i != segments.size() - 1) {
                points.remove(points.size() - 1);
            }
        }
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

        for (Route s : segments){
            if(position > s.length()){
                position -= s.length();
            } else{
                return s.pointAt(position);
            }
        } return lastRoute.pointAt(lastRoute.length());
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
        for(Route s : segments){
            if(position > s.length()){
                position -= s.length();
            } else {
                return  s.elevationAt(position);
            }
        }
        return lastRoute.elevationAt(lastRoute.length());
    }

    /**
     * Retourne l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     *
     * @param position position le long de l'itinéraire dont on cherche le noeud le plus proche
     * @return l'identité du nœud appartenant à l'itinéraire et se trouvant le plus proche de la position donnée
     */
    @Override
    public int nodeClosestTo(double position) {
        for(Route s : segments){
            if(position > s.length()){
                position -= s.length();
            } else {
                return  s.nodeClosestTo(position);
            }
        }
        return lastRoute.nodeClosestTo(lastRoute.length());
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
        double sum = 0;


        for (Route r : segments) {
            routePoint = routePoint.min(r.pointClosestTo(point).withPositionShiftedBy(sum));
            sum += r.length();
        }


        return routePoint;
    }
}

