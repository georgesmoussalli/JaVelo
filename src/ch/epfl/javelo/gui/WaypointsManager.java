package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

/**
 *La classe WaypointsManager, du sous-paquetage gui, publique et finale,
 * gère l'affichage et l'interaction avec les points de passage.
 *
 * @author Georges Moussalli (316630)
 */
public final class WaypointsManager {
    public final static int SEARCH_RADIUS = 1000;
    public final static String PIN_OUTLINE_SVG = "M-8-20C-5-14-2-7 0 0 2-7 5-14 8-20 20-40-20-40-8-20";
    public final static String PIN_DOT_SVG = "M0-23A1 1 0 000-29 1 1 0 000-23";

    private final Graph graph;
    private final ObjectProperty<MapViewParameters> mapView;
    private final ObservableList<Waypoint> waypoints;
    private final Consumer<String> errorConsumer;

    private final List<Node> pins = new ArrayList<>();

    private final ObjectProperty<Point2D> mousePosition;

    private final Pane pane;

    /**
     * Constructeur public
     *
     * @param graph le graphe du réseau routier
     * @param mapView une propriété JavaFX contenant les paramètres de la carte affichée
     * @param waypoints la liste (observable) de tous les points de passage
     * @param errorConsumer un objet permettant de signaler les erreurs
     *
     * @author Georges Moussalli (316630)
     */
    public WaypointsManager(Graph graph,
                            ObjectProperty<MapViewParameters> mapView,
                            ObservableList<Waypoint> waypoints,
                            Consumer<String> errorConsumer) {
        this.graph = graph;
        this.mapView = mapView;
        this.waypoints = waypoints;
        this.errorConsumer = errorConsumer;

        mousePosition = new SimpleObjectProperty<>();

        pane = new Pane();
        pane.setPickOnBounds(false);

        createPins();

        // ajout de marqueurs
        waypoints.addListener((ListChangeListener<Waypoint>)w -> createPins());

        // déplacement des marqueurs
        mapView.addListener((o, oldM, newM) -> {
            if (oldM.zoomLevel() != newM.zoomLevel()) {
                createPins();
            } else if (!oldM.topLeft().equals(newM.topLeft())) {
                for (int i = 0; i < waypoints.size(); i++) {
                    placePin(pins.get(i), waypoints.get(i));
                }
            }
        });
    }


    /**
     * ajoute un nouveau point de passage au nœud du graphe qui en est le plus proche
     * @param e coordonnée x du point
     * @param n coordonnée y du point
     */
    public void addWaypoint(double e, double n) {
        int closestId = graph.nodeClosestTo(new PointCh(e, n), SEARCH_RADIUS);
        if (closestId != -1) {
            PointCh closestNode = graph.nodePoint(closestId);
            waypoints.add(new Waypoint(closestNode, closestId));
        } else {
            errorConsumer.accept("Aucune route à proximité !");
        }
    }

    /**
     * retourne le panneau contenant les points de passage,
     * @return le panneau contenant les points de passage,
     */
    public Pane pane() {
        return pane;
    }


    private void placePin(Node pin, Waypoint waypoint) {
        PointWebMercator point = PointWebMercator.ofPointCh(waypoint.point());
        pin.setLayoutX(mapView.get().viewX(point));
        pin.setLayoutY(mapView.get().viewY(point));
    }

    private void createPins() {
        pins.clear();
        for (int i = 0; i < waypoints.size(); i++) {
            // création des noeuds du marqueur
            SVGPath outline = new SVGPath();
            SVGPath dot = new SVGPath();
            outline.setContent(PIN_OUTLINE_SVG);
            dot.setContent(PIN_DOT_SVG);
            outline.getStyleClass().add("pin_outside");
            dot.getStyleClass().add("pin_inside");

            // création du marqueur
            Group pin = new Group(outline, dot);
            ObservableList<String> pinStyle = pin.getStyleClass();
            pinStyle.add("pin");
            String style;
            if (i == 0) {
                style = "first";
            } else if (i < waypoints.size() - 1) {
                style = "middle";
            } else {
                style = "last";
            }
            pinStyle.add(style);

            placePin(pin, waypoints.get(i));

            final int idx = i;

            // suppression du marqueur
            pin.setOnMouseClicked(e -> {
                if (e.isStillSincePress()) {
                    waypoints.remove(idx);
                }
            });

            // déplacement du marqueur
            pin.setOnMousePressed(e -> {
                mousePosition.set(new Point2D(e.getX(), e.getY()));
            });
            pin.setOnMouseDragged(e -> {
                mousePosition.set(mousePosition.get().add(e.getX(), e.getY()));
                pin.setLayoutX(mousePosition.get().getX());
                pin.setLayoutY(mousePosition.get().getY());
            });
            pin.setOnMouseReleased(e -> {
                if (!e.isStillSincePress()) {
                    PointCh waypointPosition = mapView.get()
                            .pointAt(mousePosition.get().getX(), mousePosition.get().getY())
                            .toPointCh();

                    int closestId = graph.nodeClosestTo(waypointPosition, SEARCH_RADIUS);
                    if (closestId != -1) {
                        PointCh closestNode = graph.nodePoint(closestId);
                        waypoints.set(idx, new Waypoint(closestNode, closestId));
                    } else {
                        placePin(pin, waypoints.get(idx));
                        errorConsumer.accept("Aucun point de passage n'est présent dans les environs !");
                    }
                }
            });

            pins.add(pin);
        }
        pane.getChildren().setAll(pins);
    }
}
