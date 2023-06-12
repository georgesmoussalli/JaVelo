package ch.epfl.javelo.gui;

import ch.epfl.javelo.projection.PointCh;


/**
 * Représente un point de passage
 *
 * @author Georges Moussalli (316630)
 */
public record Waypoint(PointCh point, int nodeId) {

}
