package ch.epfl.javelo.data;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.projection.PointCh;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import static java.lang.Double.NaN;

/**
 * Représente le graphe JaVelo
 *
 * @author Georges Moussalli (316630)
 */

public final class Graph {

    private final GraphNodes nodes;
    private final GraphSectors sectors;
    private final GraphEdges edges;
    private final List<AttributeSet> attributeSets;
    private final List<GraphSectors.Sector> sectorsInArea = new LinkedList<>();

    /**
     * Constructeur public du Graphe
     *
     * @param nodes         noeuds donnés
     * @param sectors       secteurs donnés
     * @param edges         arêtes données
     * @param attributeSets ensembles d'attributs donnés
     */
    public Graph(GraphNodes nodes, GraphSectors sectors, GraphEdges edges, List<AttributeSet> attributeSets) {
        this.nodes = nodes;
        this.sectors = sectors;
        this.edges = edges;
        this.attributeSets = List.copyOf(attributeSets);
    }

    /**
     * retourne le graphe JaVelo obtenu à partir des fichiers se trouvant dans le répertoire dont le
     * chemin d'accès est basePath, ou
     * lève IOException en cas d'erreur d'entrée/sortie, p. ex. si l'un des fichiers attendu n'existe pas
     *
     * @param basePath chemin d'accès du répertoire
     * @return le graphe JaVelo obtenu à partir des fichiers se trouvant dans le répertoire dont le
     * * chemin d'accès est basePath, ou
     * * lève IOException en cas d'erreur d'entrée/sortie, p. ex. si l'un des fichiers attendu n'existe pas
     * @throws IOException en cas d'erreur d'entrée/sortie
     */
    public static Graph loadFrom(Path basePath) throws IOException {

        Path attributesPath = basePath.resolve("attributes.bin");
        Path edgesPath = basePath.resolve("edges.bin");
        Path elevationsPath = basePath.resolve("elevations.bin");
        Path nodesPath = basePath.resolve("nodes.bin");

        Path profile_idsPath = basePath.resolve("profile_ids.bin");
        Path sectorsPath = basePath.resolve("sectors.bin");

        IntBuffer nodesBuffer;

        ByteBuffer sectorsBuffer;
        ByteBuffer edgesBuffer;
        ShortBuffer elevationsBuffer;
        IntBuffer profileIds;
        List<AttributeSet> attributeSet = new ArrayList<>();
        LongBuffer attribute;


        try (FileChannel channel = FileChannel.open(nodesPath)) {
            nodesBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asIntBuffer();
        }

        try (FileChannel channel = FileChannel.open(sectorsPath)) {

            sectorsBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }


        try (FileChannel channel = FileChannel.open(edgesPath)) {

            edgesBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }

        try (FileChannel channel = FileChannel.open(elevationsPath)) {

            elevationsBuffer = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asShortBuffer();
        }

        try (FileChannel channel = FileChannel.open(profile_idsPath)) {

            profileIds = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asIntBuffer();
        }


        try (FileChannel channel = FileChannel.open(attributesPath)) {

            attribute = channel
                    .map(FileChannel.MapMode.READ_ONLY, 0, channel.size())
                    .asLongBuffer();
        }

        for (int i = 0; i < attribute.capacity(); i++) {
            attributeSet.add(i, new AttributeSet(attribute.get(i)));
        }

        return new Graph(new GraphNodes(nodesBuffer), new GraphSectors(sectorsBuffer),
                new GraphEdges(edgesBuffer, profileIds, elevationsBuffer), attributeSet);

    }


    /**
     * Retourne le nombre de noeuds donnés
     *
     * @return le nombre de noeuds donnés
     */
    public int nodeCount() {
        return nodes.count();
    }

    /**
     * Retourne la position du noeud d'identité donnée
     *
     * @param nodeId identité donnée
     * @return la position du noeud
     */
    public PointCh nodePoint(int nodeId) {
        return new PointCh(nodes.nodeE(nodeId), nodes.nodeN(nodeId));
    }

    /**
     * Retourne le nombre d'arêtes sortant du nœud d'identité donnée,
     *
     * @param nodeId identité du noeud
     * @return le nombre d'arêtes sortant du nœud d'identité donnée,
     */
    public int nodeOutDegree(int nodeId) {
        return nodes.outDegree(nodeId);
    }

    /**
     * Retourne l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId,
     *
     * @param nodeId    identité du noeud
     * @param edgeIndex index de l'arête
     * @return l'identité de la edgeIndex-ième arête sortant du nœud d'identité nodeId,
     */
    public int nodeOutEdgeId(int nodeId, int edgeIndex) {
        return nodes.edgeId(nodeId, edgeIndex);
    }

    /**
     * Retourne l'identité du nœud se trouvant le plus proche du point donné,
     * à la distance maximale donnée (en mètres), ou -1 si aucun nœud ne correspond à ces critères,
     *
     * @param point          point dont on cherche le noeud le plus proche
     * @param searchDistance distance maximale de recherche
     * @return l'identité du nœud se trouvant le plus proche du point donné,
     * à la distance maximale donnée (en mètres), ou -1 si aucun nœud ne correspond à ces critères,
     */
    public int nodeClosestTo(PointCh point, double searchDistance) {
        int index = -1;

        sectorsInArea.addAll(sectors.sectorsInArea(point, searchDistance));
        double minimum = searchDistance * searchDistance;
        for (GraphSectors.Sector s : sectorsInArea) {
            for (int i = s.startNodeId(); i < s.endNodeId(); i++) {
                double squaredDistance = nodePoint(i).squaredDistanceTo(point);
                if (squaredDistance <= minimum) {
                    index = i;
                    minimum = squaredDistance;
                }
            }
        }
        return index;
    }


    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée,
     *
     * @param edgeId identité de l'arête
     * @return l'identité du nœud destination de l'arête d'identité donnée,
     */
    public int edgeTargetNodeId(int edgeId) {
        return edges.targetNodeId(edgeId);
    }

    /**
     * Retourne vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient,
     *
     * @param edgeId identité de l'arête
     * @return vrai ssi l'arête d'identité donnée va dans le sens contraire de la voie OSM dont elle provient,
     */
    public boolean edgeIsInverted(int edgeId) {
        return edges.isInverted(edgeId);
    }

    /**
     * Retourne l'ensemble des attributs OSM attachés à l'arête d'identité donnée,
     *
     * @param edgeId identité de l'arête
     * @return l'ensemble des attributs OSM attachés à l'arête d'identité donnée,
     */
    public AttributeSet edgeAttributes(int edgeId) {
        return attributeSets.get(edges.attributesIndex(edgeId));
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée,
     *
     * @param edgeId indentité de l'arête
     * @return la longueur, en mètres, de l'arête d'identité donnée,
     */
    public double edgeLength(int edgeId) {
        return edges.length(edgeId);
    }

    /**
     * Retourne le dénivelé positif total de l'arête d'identité donnée,
     *
     * @param edgeId identité de l'arête
     * @return le dénivelé positif total de l'arête d'identité donnée,
     */
    public double edgeElevationGain(int edgeId) {
        return edges.elevationGain(edgeId);
    }

    /**
     * Retourne le profil en long de l'arête d'identité donnée, sous la forme d'une fonction; si l'arête ne possède pas de profil,
     * alors cette fonction doit retourner Double.NaN pour n'importe quel argument.
     *
     * @param edgeId identité de l'arête
     * @return le profil en long de l'arête d'identité donnée, sous la forme d'une fonction; si l'arête ne possède pas de profil,
     * alors cette fonction doit retourner Double.NaN pour n'importe quel argument.
     */
    public DoubleUnaryOperator edgeProfile(int edgeId) {
        if (edges.hasProfile(edgeId)) {
            return Functions.sampled(edges.profileSamples(edgeId), edgeLength(edgeId));
        } else return Functions.constant(NaN);

    }


}
