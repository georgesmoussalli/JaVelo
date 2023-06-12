package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import ch.epfl.javelo.routing.ElevationProfile;
import ch.epfl.javelo.routing.ElevationProfileComputer;
import ch.epfl.javelo.routing.MultiRoute;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RouteComputer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Pair;

/**
 *  La classe RouteBean du sous-paquetage gui, publique et finale, est un bean JavaFX
 *  regroupant les propriétés relatives aux points de passage et à l'itinéraire correspondant.
 *
 * @author Georges Moussalli (316630)
 */
public final class RouteBean {
    public final static int INIT_CAPACITY = 100;
    public final static float FACTOR = 0.75F;
    public final static int MAX_SAMPLE_DISTANCE = 5;

    private final RouteComputer routeComputer;
    private final ObservableList<Waypoint> waypoints;
    private final DoubleProperty highlightedPosition;
    private final ObjectProperty<Route> route;
    private final ObjectProperty<ElevationProfile> elevationProfile;
    private static final LinkedHashMap<Pair<Integer, Integer>, Route> cacheMemory =
            new LinkedHashMap<>(INIT_CAPACITY, FACTOR, true);


    /**
     * Constructeur public
     * @param routeComputer un calculateur d'itinéraire  utilisé pour déterminer le meilleur itinéraire reliant deux points de passage.
     */
    public RouteBean(RouteComputer routeComputer) {
        this.routeComputer = routeComputer;
        highlightedPosition = new SimpleDoubleProperty(Double.NaN);
        route = new SimpleObjectProperty<>(null);
        elevationProfile = new SimpleObjectProperty<>(null);
        waypoints = FXCollections.observableArrayList();

        waypoints.addListener((ListChangeListener<Waypoint>)(w -> {
            computeRoute();
            computeElevationProfile();
        }));
    }


    private void computeRoute() {
        if (waypoints.size() < 2) {
            route.set(null);
        } else {
            List<Route> segments = new ArrayList<>();

            for (int i = 0; i < waypoints.size() - 1; i++) {

                int startNodeId = waypoints.get(i).nodeId();
                int endNodeId = waypoints.get(i + 1).nodeId();
                if (startNodeId == endNodeId) {
                    continue;
                }
                Pair<Integer, Integer> nodePair = new Pair<>(startNodeId, endNodeId);

                if (cacheMemory.containsKey(nodePair)) {
                    segments.add(cacheMemory.get(nodePair));
                } else {
                    Route singleRoute = routeComputer.bestRouteBetween(
                            startNodeId,
                            endNodeId
                    );
                    if (singleRoute == null) {
                        route.set(null);
                        return;
                    }
                    if (cacheMemory.size() == INIT_CAPACITY) {
                        cacheMemory.remove(cacheMemory.keySet().iterator().next());
                    }
                    cacheMemory.put(nodePair, singleRoute);
                    segments.add(singleRoute);
                }
            }
            route.set(new MultiRoute(segments));
        }
    }


    private void computeElevationProfile() {
        elevationProfile.set(route.get() == null ?
                null : ElevationProfileComputer.elevationProfile(route.get(), MAX_SAMPLE_DISTANCE)
        );
    }

    /**
     * Retourne le profil de l'itinéraire,qui est en lecture seule
     * @return le profil de l'itinéraire, qui est en lecture seule
     */
    public ReadOnlyObjectProperty<ElevationProfile> elevationProfileProperty() {
        return elevationProfile;
    }


    /**
     * Retourne le profil de l(itinéraire
     * @return le profil de l'itinéraire
     */
    public ElevationProfile getElevationProfile() {
        return elevationProfile.get();
    }

    /**
     * Retourne l'itinéraire permettant de relier les points de passage, qui est en lecture seule
     * @return l'itinéraire permettant de relier les points de passage, qui est en lecture seule
     */
    public ReadOnlyObjectProperty<Route> routeProperty() {
        return route;
    }

    /**
     * Retourne la route
     * @return la route
     */
    public Route getRoute() {
        return route.get();
    }

    /**
     * Retourne la position mise en évidence
     * @return la position mise en évidence
     */
    public DoubleProperty highlightedPositionProperty() {
        return highlightedPosition;
    }

    /**
     * Retournant le contenu de la propriété, de type double ;
     * @return le contenu de la propriété, de type double ;
     */
    public double getHighlightedPosition() {
        return highlightedPosition.get();
    }

    /**
     * Prenant une valeur de type double et la stockant dans la propriété.
     * @param value double
     */
    public void setHighlightedPosition(double value) {
        highlightedPosition.set(value);
    }

    /**
     * Retourne la liste (observable) des points de passage
     * @return la liste (observable) des points de passage
     */
    public ObservableList<Waypoint> waypoints() {
        return waypoints;
    }
}
