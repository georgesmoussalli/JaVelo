package ch.epfl.javelo.gui;

import java.awt.*;
import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Consumer;
import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;


import javafx.stage.Stage;

/**
 * La classe JaVelo du sous-paquetage gui, publique et instanciable (donc finale), est la classe principale de l'application.
 * Elle hérite de Application.
 *
 * @author Georges Moussalli (316630)
 */
public final class JaVelo extends Application {

    private static final String DATA_DIRECTORY = "lausanne" ;

    private static final String TILE_SERVER_HOST_OSM = "tile.openstreetmap.org";
    private static final String TILE_SERVER_HOST_GERMAN = "a.tile.openstreetmap.de";
    private static final String TILE_SERVER_HOST_CYCLOSM = "c.tile-cyclosm.openstreetmap.fr/cyclosm/";
    private static final String TILE_SERVER_HOST_HUMANITARIAN= "a.tile.openstreetmap.fr/hot/";
    private static final String TILE_CACHE_OSM = "osm-cache";
    private static final String TILE_CACHE_GERMAN = "german-cache";
    private static final String TILE_CACHE_CYCLOSM ="cyclosm-cache";
    private static final String TILE_CACHE_HUMANITARIAN ="humanitarian-cache";
    private static String cache;
    private static String server;
    private static final String GPX_FILE = "javelo.gpx";



    public static void main(String[] args) {launch(args);}

    /**
     * La méthode start de JaVelo se charge de construire l'interface graphique finale en combinant les parties gérées
     * par les classes écrites précédemment et en y ajoutant le menu très simple présenté plus haut.
     * @param stage  stage
     */
    @Override
    public void start(Stage stage) throws IOException {

       Text text = new Text("Welcome to JaVelo!");
        text.setX(47);
        text.setY(20);
        Text text2 = new Text("Please press start.");
        text2.setX(50);
        text2.setY(40);
        Button start = new Button("START");
        start.setLayoutX(0);
        start.setLayoutY(60);


        Pane welcomePane = new Pane(text, text2, start);

        stage.setScene(new Scene(welcomePane));
        stage.setMinWidth(200);
        stage.setMinHeight(100);

        stage.setTitle("Welcome to JaVelo");
        stage.show();
        /**music();*/

        start.setOnAction(a -> { mapChoice(stage);
        });



    }

    private void mapCreation(Stage stage ,String cacheName, String serverName, CostFunction fc) throws IOException {


        Graph graph = Graph.loadFrom(Path.of(DATA_DIRECTORY));
        Path cache = Path.of(cacheName);
        TileManager tileManager = new TileManager(cache, serverName);
        RouteComputer routeComputer = new RouteComputer(graph, fc);
        RouteBean bean = new RouteBean(routeComputer);
        bean.setHighlightedPosition(1000);


        ErrorManager errorManager = new ErrorManager();
        Consumer<String> errorConsumer = errorManager::displayError;

        AnnotatedMapManager annotatedMapManager =
                new AnnotatedMapManager(graph, tileManager, bean, errorConsumer);

        ElevationProfileManager profileManager =
                new ElevationProfileManager(bean.elevationProfileProperty(), bean.highlightedPositionProperty());

        bean.highlightedPositionProperty().bind(Bindings.createDoubleBinding(() -> {
            if (annotatedMapManager.mousePositionOnRouteProperty().get() >= 0) {
                return annotatedMapManager.mousePositionOnRouteProperty().get();
            } else {
                return profileManager.mousePositionOnProfileProperty().get();
            }
        }, profileManager.mousePositionOnProfileProperty(), annotatedMapManager.mousePositionOnRouteProperty()));

        SplitPane splitPane = new SplitPane();
        Pane profilePane = profileManager.pane();
        SplitPane.setResizableWithParent(profilePane, false);

        bean.routeProperty().addListener((o, oldR, newR) -> {
            if (newR != null) {
                splitPane.getItems().setAll(annotatedMapManager.pane(), profilePane);
            } else {
                splitPane.getItems().setAll(annotatedMapManager.pane());
            }
        });
        splitPane.getItems().setAll(annotatedMapManager.pane());
        splitPane.orientationProperty().set(Orientation.VERTICAL);

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("File");
        MenuItem menuItem = new MenuItem("Export GPX");

        menuItem.disableProperty().bind(bean.routeProperty().isNull());
        menuItem.setOnAction(a -> {
            try {
                GpxGenerator.writeGpx(GPX_FILE, bean.getRoute(), bean.getElevationProfile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        menu.getItems().add(menuItem);

        menuBar.getMenus().add(menu);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(new StackPane(splitPane, errorManager.pane()));
        mainPane.setTop(menuBar);


        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setScene(new Scene(mainPane));
        stage.setTitle("JaVelo");
        stage.show();



    }

    private void mapChoice(Stage stage){



       Button osm = new Button("OSM");
       setSize(osm);
       Button german = new Button("GERMAN OSM");
       setSize(german);
       Button cyclosm = new Button("OSM FOR BICYCLE");
       setSize(cyclosm);
       Button humanitarian = new Button(" HUMANITARIAN OSM");
       setSize(humanitarian);


        GridPane pane = new GridPane();

        pane.add(new Text(" Choose your background : "), 1, 0);

        pane.add(osm, 0, 1);
        pane.add(german, 1, 1);
        pane.add(cyclosm, 2, 1);
        pane.add(humanitarian, 3, 1);

        stage.setScene(new Scene(pane));
        stage.setMinWidth(800);
        stage.setMinHeight(300);
        stage.resizableProperty().set(false);
        stage.setTitle("Background  choices : ");
        stage.show();

        osm.setOnAction(a -> {
            cache = TILE_CACHE_OSM;
            server = TILE_SERVER_HOST_OSM;
            try {
                bikeChoice(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        german.setOnAction(a -> {
            cache = TILE_CACHE_GERMAN;
            server = TILE_SERVER_HOST_GERMAN;
            try {
                bikeChoice(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        cyclosm.setOnAction(a -> {
            cache = TILE_CACHE_CYCLOSM;
            server = TILE_SERVER_HOST_CYCLOSM;
            try {
                bikeChoice(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        humanitarian.setOnAction(a -> {
            cache = TILE_CACHE_HUMANITARIAN;
            server = TILE_SERVER_HOST_HUMANITARIAN;
            try {
                bikeChoice(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        }

private void bikeChoice(Stage stage) throws IOException {
    Graph graph = Graph.loadFrom(Path.of(DATA_DIRECTORY));

    Button cityBike = new Button("CITYBIKE");
    setSize(cityBike);
    Button vtt = new Button("VTT");
    setSize(vtt);
    Button sport = new Button("SPEEDBIKE");
    setSize(sport);
    Button hiking = new Button(" HIKING");
    setSize(hiking);

    GridPane pane = new GridPane();
    pane.add(new Text(" Choose your bike : "), 1, 2);
    pane.add(cityBike, 0, 3);
    pane.add(vtt, 1, 3);
    pane.add(sport, 2, 3);
    pane.add(hiking, 3, 3);
    stage.setScene(new Scene(pane));
    stage.setMinWidth(800);
    stage.setMinHeight(300);
    stage.resizableProperty().set(false);
    stage.setTitle(" Bicycle choices : ");
    stage.show();


    cityBike.setOnAction(a -> {
        try {
            mapCreation(stage, cache, server, new HikingBikeCF(graph));
        } catch (IOException e) {
            e.printStackTrace();
        }});


        vtt.setOnAction(a -> {
        try {
            mapCreation(stage, cache, server, new HikingBikeCF(graph));
        } catch (IOException e) {
            e.printStackTrace();
        }

    });

    sport.setOnAction(a -> {
        try {
            mapCreation(stage, cache, server, new HikingBikeCF(graph));
        } catch (IOException e) {
            e.printStackTrace();
        }

    });

    hiking.setOnAction(a -> {
        try {
            mapCreation(stage, cache, server, new HikingBikeCF(graph));
        } catch (IOException e) {
            e.printStackTrace();
        }
    });


}


    private void setSize(Button button) {
        button.minHeightProperty().set(280);
        button.minWidthProperty().set(200);

    }

    /**private void music(){
        String s ="la-bicyclette.mp3";
        Media montand = new Media(Paths.get(s).toUri().toString());
        MediaPlayer yves= new MediaPlayer(montand);
        yves.play();
    }
*/

}



