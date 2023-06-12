package ch.epfl.javelo.gui;

import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * AnnotatedMapManager du sous-paquetage gui, publique et instanciable (donc finale),
 * gère l'affichage de la carte «annotée», le fond de carte au-dessus duquel sont superposés l'itinéraire et les points de passage.
 *
 * @author Georges Moussalli (316630)
 */
public final class AnnotatedMapManager {

    private static final int MAX_DISTANCE = 15;
    private static final int BASE_ZOOM = 12;
    private static final int X = 543_200;
    private static final int Y = 370_650;
    private final ObjectProperty<Point2D> mousePosition = new SimpleObjectProperty<>(null);
    private final StackPane pane = new StackPane();
    private final DoubleProperty mousePositionOnRoute = new SimpleDoubleProperty(Double.NaN);

    /**
     * le constructeur crée un gestionnaire de fond de carte (BaseMapManager),
     * un gestionnaire de points de passage (WaypointsManager)
     * et un gestionnaire d'itinéraire (RouteManager) et combine, par empilement, leurs panneaux respectifs
     * @param graph le graphe du réseau routier, de type Graph
     * @param tileManager le gestionnaire de tuiles OpenStreetMap, de type TileManager
     * @param routeBean le bean de l'itinéraire, de type RouteBean
     * @param errorConsumer un «consommateur d'erreurs» permettant de signaler une erreur, de type Consumer<String>
     */
    public AnnotatedMapManager(Graph graph, TileManager tileManager, RouteBean routeBean, Consumer<String> errorConsumer) {

        pane.getStylesheets().add("map.css");
        ObjectProperty<MapViewParameters> mapView = new SimpleObjectProperty<>(new MapViewParameters(BASE_ZOOM, X, Y));;

        WaypointsManager waypointsManager = new WaypointsManager(graph, mapView, routeBean.waypoints(), errorConsumer);
        BaseMapManager baseMapManager = new BaseMapManager(tileManager, waypointsManager, mapView);
        RouteManager routeManager = new RouteManager(routeBean, mapView);

        pane.getChildren().setAll(baseMapManager.pane(), routeManager.pane(), waypointsManager.pane());

        pane.setOnMouseMoved(e -> {
            mousePosition.set(new Point2D(e.getX(), e.getY()));
        });
        pane.setOnMouseExited(e -> {
            mousePosition.set(null);
        });

        mousePosition.addListener((o, oldM, newM) -> {
            if (newM == null || routeBean.getRoute() == null) {
                mousePositionOnRoute.set(Double.NaN);
            } else {
                PointCh mousePoint = mapView.get().pointAt(newM.getX(), newM.getY()).toPointCh();
                RoutePoint routePoint = routeBean.getRoute().pointClosestTo(mousePoint);
                PointWebMercator closestPoint = PointWebMercator.ofPointCh(routePoint.point());
                Point2D otherPoint = new Point2D(mapView.get().viewX(closestPoint), mapView.get().viewY(closestPoint));

                if (otherPoint.distance(newM) < MAX_DISTANCE) {
                    mousePositionOnRoute.set(routePoint.position());
                } else {
                    mousePositionOnRoute.set(Double.NaN);
                }
            }
        });
    }

    /**
     * retourne le panneau contenant la carte annotée,
     * @return le panneau contenant la carte annotée,
     */
    public StackPane pane() {
        return pane;
    }

    /**
     * Retourne la propriété contenant la position du pointeur de la souris le long de l'itinéraire.
     * @return la propriété contenant la position du pointeur de la souris le long de l'itinéraire.
     */
    public ReadOnlyDoubleProperty mousePositionOnRouteProperty() {
        return mousePositionOnRoute;
    }


}
