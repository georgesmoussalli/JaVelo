package ch.epfl.javelo.data;

import ch.epfl.javelo.Q28_4;

import java.nio.IntBuffer;

import static ch.epfl.javelo.Bits.extractUnsigned;

/**
 * Représente le tableau de tous les nœuds du graphe JaVelo
 *
 * @param buffer la mémoire tampon contenant la valeur des attributs
 *               de la totalité des nœuds du graphe
 * @author Georges Moussalli (316630)
 */
public record GraphNodes(IntBuffer buffer) {

    private static final int OFFSET_E = 0;
    private static final int OFFSET_N = OFFSET_E + 1;
    private static final int OFFSET_OUT_EDGES = OFFSET_N + 1;
    private static final int NODE_INTS = OFFSET_OUT_EDGES + 1;
    private static final int START_EDGE_ID = 0;
    private static final int START_NODE = 28;
    private static final int LENGTH_EDGE_ID = 28;
    private static final int LENGTH_NODE_ID = 4;

    /**
     * Retourne le nombre total de nœuds
     *
     * @return le nombre total de nœuds
     */
    public int count() {
        return buffer().capacity() / NODE_INTS;
    }

    /**
     * Retourne la coordonnée E du nœud d'identité donnée
     *
     * @param nodeId Identité du noeud
     * @return retourne la coordonnée E du nœud d'identité donnée,
     */
    public double nodeE(int nodeId) {
        return Q28_4.asDouble(buffer.get(nodeId * NODE_INTS) + OFFSET_E);
    }

    /**
     * Retourne la coordonnée N du nœud d'identité donnée
     *
     * @param nodeId Identité du noeudn
     * @return la coordonnée N du nœud d'identité donnée,
     */
    public double nodeN(int nodeId) {
        return Q28_4.asDouble(buffer.get(nodeId * NODE_INTS + OFFSET_N));
    }

    /**
     * Retourne le nombre d'arêtes sortant du nœud d'identité donné
     *
     * @param nodeId Identité du noeud
     * @return le nombre d'arêtes sortant du nœud d'identité donné,
     */
    public int outDegree(int nodeId) {
        return extractUnsigned(buffer.get((nodeId * NODE_INTS) + OFFSET_OUT_EDGES), START_NODE, LENGTH_NODE_ID);
    }

    /**
     * Retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId.
     *
     * @param nodeId    Identité du noeud
     * @param edgeIndex Identité de l'arête
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId.
     */
    public int edgeId(int nodeId, int edgeIndex) {
        return extractUnsigned
                (buffer.get((nodeId * NODE_INTS) + OFFSET_OUT_EDGES), START_EDGE_ID, LENGTH_EDGE_ID) + edgeIndex;
    }

}
