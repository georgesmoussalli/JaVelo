package ch.epfl.javelo.routing;

import ch.epfl.javelo.Preconditions;
import ch.epfl.javelo.data.Graph;
import java.util.*;

/**
 * Représente un planificateur d'itinéraire
 *
 * @author Georges Moussalli (316630)
 */
public final class RouteComputer {

    private final Graph graph;
    private final CostFunction costFunction;

    /**
     * Construit un planificateur d'itinéraire pour le graphe et la fonction de coût donnés
     *
     * @param graph        Graphe donné
     * @param costFunction Fonction de coût donnée
     */
    public RouteComputer(Graph graph, CostFunction costFunction) {
        this.graph = graph;
        this.costFunction = costFunction;
    }

    /**
     * Retourne l'itinéraire de coût total minimal allant du nœud d'identité startNodeId au nœud d'identité endNodeId dans le graphe passé au constructeur,
     * ou null si aucun itinéraire n'existe
     * Si le nœud de départ et d'arrivée sont identiques, lève IllegalArgumentException
     *
     * @param startNodeId identité du noeud de départ
     * @param endNodeId   identité du noeud d'arrivée
     * @return qui retourne l'itinéraire de coût total minimal allant du nœud d'identité startNodeId au nœud d'identité endNodeId dans le graphe passé au constructeur,
     * ou null si aucun itinéraire n'existe.
     * Si le nœud de départ et d'arrivée sont identiques, lève IllegalArgumentException
     */
    public SingleRoute bestRouteBetween(int startNodeId, int endNodeId) {
        record WeightedNode(int nodeId, float distance)
                implements Comparable<WeightedNode> {
            @Override
            public int compareTo(WeightedNode that) {
                return Float.compare(this.distance, that.distance);
            }
        }
        Preconditions.checkArgument(startNodeId != endNodeId);


        float[] distance = new float[graph.nodeCount()];
        int[] previousNodeId = new int[graph.nodeCount()];
        PriorityQueue<WeightedNode> en_exploration = new PriorityQueue<>();

        for (int i = 0; i < graph.nodeCount(); i++) {
            distance[i] = Float.POSITIVE_INFINITY;

        }

        distance[startNodeId] = 0;
        en_exploration.add(new WeightedNode(startNodeId, distance[startNodeId]));

        while (!en_exploration.isEmpty()) {

            WeightedNode N = en_exploration.remove();

            if (N.nodeId == endNodeId) {

                int i = N.nodeId;
                List<Edge> route = new LinkedList<>();

                while (i != startNodeId) {
                    int previous = previousNodeId[i];
                    int edgeToAdd = 0;
                    for (int j = 0; j < graph.nodeOutDegree(previous); j++) {
                        int edge_id = graph.nodeOutEdgeId(previous, j);
                        if (graph.edgeTargetNodeId(edge_id) == i) {
                            edgeToAdd = edge_id;
                        }
                    }

                    route.add(0, Edge.of(graph, edgeToAdd, previousNodeId[i], i));
                    i = previousNodeId[i];
                    distance[N.nodeId] = Float.NEGATIVE_INFINITY;

                }
                return new SingleRoute(route);

            }
            if( distance[N.nodeId] != Float.NEGATIVE_INFINITY) {

                for (int i = 0; i < graph.nodeOutDegree(N.nodeId); i++) {
                    int edge_id = graph.nodeOutEdgeId(N.nodeId, i);
                    int arrival_id = graph.edgeTargetNodeId(edge_id);

                    double d = distance[N.nodeId] + (graph.edgeLength(edge_id) * costFunction.costFactor(N.nodeId, edge_id));
                    float dFloat = (float) d;

                    if (dFloat < distance[arrival_id]) {
                        distance[arrival_id] = dFloat;
                        previousNodeId[arrival_id] = N.nodeId;
                        en_exploration.add(new WeightedNode(arrival_id, dFloat + (float) graph.nodePoint(N.nodeId).distanceTo(graph.nodePoint(endNodeId))));


                    }
                }


            }}

        return null;
    }

}