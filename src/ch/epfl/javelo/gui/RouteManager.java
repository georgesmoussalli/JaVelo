package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;
import ch.epfl.javelo.routing.Route;
import ch.epfl.javelo.routing.RoutePoint;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;

/**
 * La classe RouteManager du sous-paquetage gui, publique et finale, gère l'affichage de l'itinéraire et (une partie de) l'interaction avec lui.
 *
 * @author Georges Moussalli (316630)
 * */
public final class RouteManager {

    public final static int HIGHLIGHT_RADIUS = 5;
    private final RouteBean routeBean;
    private final ReadOnlyObjectProperty<MapViewParameters> mapView;
    private final Pane pane;
    private final Polyline polyline;
    private final Circle disk;

    /**
     *
     * @param routeBean le bean de l'itinéraire
     * @param mapView une propriété JavaFX, en lecture seule, contenant les paramètres de la carte affichée,
     * un «consommateur d'erreurs» permettant de signaler une erreur.
     */
    public RouteManager(RouteBean routeBean,
                        ReadOnlyObjectProperty<MapViewParameters> mapView) {
        this.routeBean = routeBean;
        this.mapView = mapView;

        polyline = new Polyline();
        polyline.setId("route");
        buildRoute(routeBean.getRoute());

        disk = new Circle(HIGHLIGHT_RADIUS);
        disk.setId("highlight");
        placeDisk(routeBean.getHighlightedPosition());

        pane = new Pane();
        pane.setPickOnBounds(false);
        pane.getChildren().setAll(polyline, disk);

        disk.setOnMouseClicked(e -> {
            Point2D mousePosition = disk.localToParent(e.getX(), e.getY());
            PointCh mousePoint = mapView.get().pointAt(mousePosition.getX(), mousePosition.getY()).toPointCh();
            RoutePoint closestPoint = routeBean.getRoute().pointClosestTo(mousePoint);
            Waypoint waypoint = new Waypoint(mousePoint, routeBean.getRoute().nodeClosestTo(closestPoint.position()));
            routeBean.waypoints().add(waypoint);
        });

        routeBean.highlightedPositionProperty().addListener((o, oldP, newP) -> {
            disk.setVisible(newP.doubleValue() >= 0);
            if (disk.isVisible()) {
                placeDisk(newP.doubleValue());
            }
        });

        routeBean.routeProperty().addListener((o, oldR, newR) -> {
            polyline.setVisible(newR != null);
            placeDisk(routeBean.getHighlightedPosition());
            buildRoute(newR);
        });

        mapView.addListener((o, oldM, newM) -> {
            placeDisk(routeBean.getHighlightedPosition());
            if (newM.x() != oldM.x() || newM.y() != oldM.y()) {
                placeRoute(newM);
            }
            if (newM.zoomLevel() != oldM.zoomLevel()) {
                buildRoute(routeBean.getRoute());
            }
        });
    }


    private void placeRoute(MapViewParameters mapView) {
        polyline.setLayoutX(-mapView.x());
        polyline.setLayoutY(-mapView.y());
    }


    private void buildRoute(Route route) {
        if (route != null) {
            List<PointCh> points = route.points();
            List<Double> coordinates = new ArrayList<>();
            for (PointCh point : points) {
                PointWebMercator data = PointWebMercator.ofPointCh(point);
                coordinates.add(mapView.get().x() + mapView.get().viewX(data));
                coordinates.add(mapView.get().y() + mapView.get().viewY(data));
            }
            placeRoute(mapView.get());
            polyline.getPoints().setAll(coordinates);
        }
    }


    private void placeDisk(double position) {
        if (!Double.isNaN(position) && routeBean.getRoute() != null) {
            PointWebMercator diskPosition = PointWebMercator.ofPointCh(routeBean.getRoute().pointAt(position));
            disk.setLayoutX(mapView.get().viewX(diskPosition));
            disk.setLayoutY(mapView.get().viewY(diskPosition));
        }
    }


    /**
     * Retourne le panneau JavaFX contenant la ligne représentant l'itinéraire et le disque de mise en évidence
     * @return le panneau JavaFX contenant la ligne représentant l'itinéraire et le disque de mise en évidence
     */
    public Pane pane() {
        return pane;
    }
}
