package ch.epfl.javelo.gui;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import java.io.IOException;
import ch.epfl.javelo.projection.PointCh;
import ch.epfl.javelo.projection.PointWebMercator;


/**
 * La classe BaseMapManager du sous-paquetage gui, publique et finale, gère l'affichage et l'interaction avec le fond de carte.
 *
 * @author Georges Moussalli (316630)
 */
public final class BaseMapManager {
    private final static int TILE_LENGTH = 256;

    private final Pane pane;
    private final TileManager tileManager;
    private final ObjectProperty<MapViewParameters> mapView;
    private final GraphicsContext graphics;
    private final Canvas canvas;
    private final ObjectProperty<Point2D> mousePosition;
    private boolean redrawNeeded;

    /**
     * Constructeur public
     * @param tileManager le gestionnaire de tuiles à utiliser pour obtenir les tuiles de la carte,
     * @param waypointsManager le gestionnaire des points de passage,
     * @param mapView une propriété JavaFX contenant les paramètres de la carte affichée.
     */
    public BaseMapManager(TileManager tileManager,
                          WaypointsManager waypointsManager,
                          ObjectProperty<MapViewParameters> mapView) {

        this.mapView = mapView;
        this.tileManager = tileManager;

        mousePosition = new SimpleObjectProperty<>();

        pane = new Pane();
        canvas = new Canvas();
        pane.getChildren().add(canvas);

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        graphics = canvas.getGraphicsContext2D();

        canvas.sceneProperty().addListener((o, oldS, newS) -> {
            assert oldS == null;
            if (newS != null) {
                newS.addPreLayoutPulseListener(this::redrawIfNeeded);
            }
        });

        // changement du niveau de zoom
        SimpleLongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            if (e.getDeltaY() == 0d) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            int zoomDelta = (int) Math .signum(e.getDeltaY());

            int oldZoomLevel = mapView.get().zoomLevel();
            int newZoomLevel = Math.min(19, Math.max(8, oldZoomLevel + zoomDelta));

            double mouseX = e.getX();
            double mouseY = e.getY();

            PointWebMercator mouse = mapView.get().pointAt(mouseX, mouseY);

            mapView.set(new MapViewParameters(
                    newZoomLevel,
                    mouse.xAtZoomLevel(newZoomLevel) - mouseX,
                    mouse.yAtZoomLevel(newZoomLevel) - mouseY
            ));
        });

        // glissement de la carte
        pane.setOnMousePressed(e -> {
            mousePosition.set(new Point2D(e.getX(), e.getY()));
        });
        pane.setOnMouseDragged(e -> {
            Point2D newTopLeft = mapView.get().topLeft().add(mousePosition.get()).subtract(e.getX(), e.getY());
            mapView.set(mapView.get().withMinXY(newTopLeft.getX(), newTopLeft.getY()));
            mousePosition.set(new Point2D(e.getX(), e.getY()));
        });

        // ajout d'un point de passage
        pane.setOnMouseClicked(e -> {
            if (e.isStillSincePress()) {
                PointCh waypointPosition = mapView.get().pointAt(e.getX(), e.getY()).toPointCh();
                waypointsManager.addWaypoint(waypointPosition.e(), waypointPosition.n());
            }
        });

        // dessin nécessaire
        canvas.widthProperty().addListener(o -> redrawOnNextPulse());
        canvas.heightProperty().addListener(o -> redrawOnNextPulse());
        mapView.addListener(o -> redrawOnNextPulse());
    }


    /**
     * Retourne le panneau JavaFX affichant le fond de carte.
     * @return le panneau JavaFX affichant le fond de carte.
     */
    public Pane pane() {
        return pane;
    }


    private void redrawIfNeeded() {
        if (redrawNeeded) {
            redrawNeeded = false;
            drawMap();
        }
    }


    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void drawMap() {
        double topLeftX = mapView.get().x();
        double topLeftY = mapView.get().y();
        int numberOfTilesPerRow = (int) (Math.ceil(canvas.getWidth() / TILE_LENGTH));
        int numberOfTilesPerColumn = (int) (Math.ceil(canvas.getHeight() / TILE_LENGTH));

        TileManager.TileId firstTile = new TileManager.TileId(
                mapView.get().zoomLevel(),
                (int) topLeftX / TILE_LENGTH,
                (int) topLeftY / TILE_LENGTH
        );

        for (int x = firstTile.x(); x <= firstTile.x() + numberOfTilesPerRow; x++) {

            for (int y = firstTile.y(); y <= firstTile.y() + numberOfTilesPerColumn; y++) {

                TileManager.TileId currentTile = new TileManager.TileId(mapView.get().zoomLevel(), x, y);
                try {
                    graphics.drawImage(
                            tileManager.imageForTileAt(currentTile),
                            TILE_LENGTH * x - topLeftX,
                            TILE_LENGTH * y - topLeftY
                    );
                } catch (IOException ioException) {
                    // on ne dessine rien
                }
            }
        }
    }
}
