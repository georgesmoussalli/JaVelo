package ch.epfl.javelo.gui;

import java.util.ArrayList;
import java.util.List;
import ch.epfl.javelo.routing.ElevationProfile;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;

/**
 * La classe ElevationProfileManager du sous-paquetage gui, publique et finale,
 * gère l'affichage et l'interaction avec le profil en long d'un itinéraire.
 *
 * @author Georges Moussalli (316630)
 */
public final class ElevationProfileManager {

    private static final Insets INSETS = new Insets(10, 10, 20, 40);
    private static final int[] POS_STEPS =
            { 1000, 2000, 5000, 10_000, 25_000, 50_000, 100_000 };
    private static final int[] ELE_STEPS =
            { 5, 10, 20, 25, 50, 100, 200, 250, 500, 1_000 };
    private static final double MIN_POS_STEP = 50;
    private static final double MIN_ELE_STEP = 25;

    private final ReadOnlyObjectProperty<ElevationProfile> elevationProfile;
    private final ReadOnlyDoubleProperty highlightedPosition;

    private final BorderPane borderPane = new BorderPane();

    private final ObjectProperty<Rectangle2D> rectangle =
            new SimpleObjectProperty<>(Rectangle2D.EMPTY);
    private final ObjectProperty<Transform> screenToWorld =
            new SimpleObjectProperty<>(new Affine());
    private final ObjectProperty<Transform> worldToScreen =
            new SimpleObjectProperty<>(new Affine());
    private final DoubleProperty mousePositionOnProfile =
            new SimpleDoubleProperty(Double.NaN);

    /**
     * Constructeur public
     * @param elevationProfile une propriété, accessible en lecture seule, contenant le profil à afficher; et contient null dans le cas où aucun profil n'est à afficher,
     * @param highlightedPosition une propriété, accessible en lecture seule, contenant la position le long du profil à mettre en évidence; et contient NaN dans le cas où aucune position n'est à mettre en évidence.
     */
    public ElevationProfileManager(
            ReadOnlyObjectProperty<ElevationProfile> elevationProfile,
            ReadOnlyDoubleProperty highlightedPosition
    ) {

        this.elevationProfile = elevationProfile;
        this.highlightedPosition = highlightedPosition;

        // éléments javafx
        Path grid = new Path();
        grid.setId("grid");
        Group gridLabels = new Group();
        Polygon profile = new Polygon();
        profile.setId("profile");
        Line verticalLine = new Line();

        Pane pane = new Pane(grid, gridLabels, profile, verticalLine);

        Text stats = new Text();
        VBox profileData = new VBox(stats);
        profileData.setId("profile_data");

        borderPane.getStylesheets().add("elevation_profile.css");
        borderPane.setCenter(pane);
        borderPane.setBottom(profileData);

        // binding entre pane et rectangle
        rectangle.bind(Bindings.createObjectBinding(() ->
                        new Rectangle2D(
                                INSETS.getLeft(),
                                INSETS.getTop(),
                                Math.max(0, pane.getWidth() - INSETS.getRight() - INSETS.getLeft()),
                                Math.max(0, pane.getHeight() - INSETS.getBottom() - INSETS.getTop())),
                pane.widthProperty(), pane.heightProperty()));

        // bindings vertical line
        bindVerticalLine(verticalLine);

        // position de la souris
        pane.setOnMouseMoved(e -> {
            boolean mouseXInRectangle = INSETS.getLeft() < e.getX() && e.getX() < pane.getWidth() - INSETS.getRight();
            boolean mouseYInRectangle = INSETS.getTop() < e.getY() && e.getY() < pane.getHeight() - INSETS.getBottom();
            if (!(mouseXInRectangle && mouseYInRectangle)) {
                mousePositionOnProfile.set(Double.NaN);
            } else {
                mousePositionOnProfile.set(screenToWorld.get().transform(e.getX(), e.getY()).getX());
            }
        });
        pane.setOnMouseExited(e -> {
            mousePositionOnProfile.set(Double.NaN);
        });

        // on redessine
        rectangle.addListener((o, oldR, newR) -> {
            createTransforms(newR, elevationProfile.get());
            drawGraph(newR, profile);
            drawGrid(newR, elevationProfile.get(), grid, gridLabels);
        });
        elevationProfile.addListener((o, oldP, newP) -> {
            if (newP != null) {
                createTransforms(rectangle.get(), newP);
                drawGraph(rectangle.get(), profile);
                drawGrid(rectangle.get(), newP, grid, gridLabels);
                stats.setText(createStats(newP));
            }
        });
    }


    private void drawGraph(Rectangle2D rectangle, Polygon profile) {
        List<Double> coordinates = new ArrayList<>();

        for (int x = 0; x < (int) (rectangle.getWidth()); x++) {

            Point2D worldX = screenToWorld.get().transform(INSETS.getLeft() + x, 0);
            double worldY = elevationProfile.get().elevationAt(worldX.getX());
            Point2D screen = worldToScreen.get().transform(worldX.getX(), worldY);

            coordinates.add(screen.getX());
            coordinates.add(screen.getY());
        }

        profile.getPoints().setAll(coordinates);
        // bottom right
        profile.getPoints().add(INSETS.getLeft() + rectangle.getWidth());
        profile.getPoints().add(INSETS.getTop() + rectangle.getHeight());
        // bottom left
        profile.getPoints().add(INSETS.getLeft());
        profile.getPoints().add(INSETS.getTop() + rectangle.getHeight());
    }

    private void createTransforms(Rectangle2D rectangle, ElevationProfile elevationProfile) {

        Affine affine = new Affine();

        affine.prependTranslation(-INSETS.getLeft(), -(rectangle.getHeight() + INSETS.getTop()));

        double xScaleFactor = elevationProfile.length() / rectangle.getWidth();
        double yScaleFactor = -(elevationProfile.maxElevation() - elevationProfile.minElevation()) / rectangle.getHeight();
        affine.prependScale(xScaleFactor, yScaleFactor);

        affine.prependTranslation(0, elevationProfile.minElevation());

        try {
            screenToWorld.set(affine);
            worldToScreen.set(affine.createInverse());
        } catch (NonInvertibleTransformException e) {
            // on laisse la valeur précédente
        }
    }

    private void drawGrid(Rectangle2D rectangle, ElevationProfile elevationProfile, Path grid, Group labels) {

        int posStepMeters = POS_STEPS[POS_STEPS.length - 1];
        double posStepPixels = worldToScreen.get().deltaTransform(posStepMeters, 0).getX();
        for (int stepMeters : POS_STEPS) {
            double stepPixels = worldToScreen.get().deltaTransform(stepMeters, 0).getX();
            if (stepPixels > MIN_POS_STEP) {
                posStepMeters = stepMeters;
                posStepPixels = stepPixels;
                break;
            }
        }

        int eleStepMeters = ELE_STEPS[ELE_STEPS.length - 1];
        double eleStepPixels = -worldToScreen.get().deltaTransform(0, eleStepMeters).getY();
        for (int stepMeters : ELE_STEPS) {
            double stepPixels = -worldToScreen.get().deltaTransform(0, stepMeters).getY();
            if (stepPixels > MIN_ELE_STEP) {
                eleStepMeters = stepMeters;
                eleStepPixels = stepPixels;
                break;
            }
        }

        int posEndMeters = (int) (elevationProfile.length() / posStepMeters + 1) * posStepMeters;
        int posStartPixels = (int) worldToScreen.get().transform(0, 0).getX();
        int posEndPixels = (int) worldToScreen.get().transform(posEndMeters, 0).getX();

        int eleStartMeters = (int) (elevationProfile.minElevation() / eleStepMeters + 1) * eleStepMeters;
        int eleEndMeters = (int) (elevationProfile.maxElevation() / eleStepMeters) * eleStepMeters;
        int eleStartPixels = (int) worldToScreen.get().transform(0, eleEndMeters).getY();
        int eleEndPixels = (int) worldToScreen.get().transform(0, eleStartMeters).getY();

        List<PathElement> lines = new ArrayList<>();
        List<Text> texts = new ArrayList<>();

        int meters = 0;
        for (double x = posStartPixels; x < posEndPixels; x += posStepPixels) {
            // draw vertical line
            lines.add(new MoveTo(x, INSETS.getTop()));
            lines.add(new LineTo(x, INSETS.getTop() + rectangle.getHeight()));
            // draw position label
            Text label = new Text(String.valueOf(meters));
            label.setTextOrigin(VPos.TOP);
            label.setLayoutX(x - 0.5 * label.prefWidth(0));
            label.setLayoutY(INSETS.getTop() + rectangle.getHeight());
            texts.add(label);
            meters += (posStepMeters / 1000);
        }

        meters = eleEndMeters;
        for (double y = eleStartPixels; y < eleEndPixels; y += eleStepPixels) {
            // draw horizontal line
            lines.add(new MoveTo(INSETS.getLeft(), y));
            lines.add(new LineTo(INSETS.getLeft() + rectangle.getWidth(), y));
            // draw elevation label
            Text label = new Text(String.valueOf(meters));
            label.setTextOrigin(VPos.CENTER);
            label.setLayoutX(INSETS.getLeft() - label.prefWidth(0) - 2);
            label.setLayoutY(y);
            texts.add(label);
            meters -= eleStepMeters;
        }
        lines.add(new MoveTo(INSETS.getLeft(), INSETS.getTop() + rectangle.getHeight()));
        lines.add(new LineTo(INSETS.getLeft() + rectangle.getWidth(), INSETS.getTop() + rectangle.getHeight()));

        grid.getElements().setAll(lines);
        labels.getChildren().setAll(texts);
    }


    private String createStats(ElevationProfile elevationProfile) {
        return String.format("Longueur : %.1f km" +
                        "     Montée : %.0f m" +
                        "     Descente : %.0f m" +
                        "     Altitude : de %.0f m à %.0f m",
                elevationProfile.length() / 1000,
                elevationProfile.totalAscent(),
                elevationProfile.totalDescent(),
                elevationProfile.minElevation(),
                elevationProfile.maxElevation());
    }


    private void bindVerticalLine(Line line) {
        line.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> worldToScreen.get().transform(highlightedPosition.get(), 0).getX(), highlightedPosition));
        line.startYProperty().bind(Bindings.select(rectangle, "minY"));
        line.endYProperty().bind(Bindings.select(rectangle, "maxY"));
        line.visibleProperty().bind(highlightedPosition.greaterThanOrEqualTo(0));
    }

    /**
     * retourne le panneau contenant le dessin du profil,
     * @return le panneau contenant le dessin du profil
     */
    public Pane pane() {
        return borderPane;
    }

    /**
     * retourne une propriété en lecture seule contenant la position du pointeur de la souris le long du profil (en mètres, arrondie à l'entier le plus proche),
     * ou NaN si le pointeur de la souris ne se trouve pas au-dessus du profil.
     * @return une propriété en lecture seule contenant la position du pointeur de la souris le long du profil (en mètres, arrondie à l'entier le plus proche),
     * ou NaN si le pointeur de la souris ne se trouve pas au-dessus du profil.
     */
    public ReadOnlyDoubleProperty mousePositionOnProfileProperty() {
        return mousePositionOnProfile;
    }
}

