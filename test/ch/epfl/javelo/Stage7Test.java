package ch.epfl.javelo;

import ch.epfl.javelo.data.Graph;
import ch.epfl.javelo.routing.*;

import java.io.IOException;
import java.nio.file.Path;

public class Stage7Test {
    public static void main(String[] args) throws IOException {
        Graph g = Graph.loadFrom(Path.of("lausanne"));
        CostFunction cf = new CityBikeCF(g);
        RouteComputer rc = new RouteComputer(g, cf);
        Route r = rc.bestRouteBetween(159049, 117669);
        double stepLength = 5;
        ElevationProfile ele = ElevationProfileComputer.elevationProfile(r, stepLength);
        GpxGenerator.writeGpx("EPFL_Sauvabelin" +
                "", r, ele);
    }
}