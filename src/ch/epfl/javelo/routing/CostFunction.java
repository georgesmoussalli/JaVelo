package ch.epfl.javelo.routing;


/**
 * CostFunction représente une fonction de coût
 *
 * @author Georges Moussalli (316630)
 */
public interface CostFunction {


    /**
     * Retourne le facteur par lequel la longueur de l'arête d'identité edgeId,
     * partant du nœud d'identité nodeId, doit être multipliée
     * ce facteur doit impérativement être supérieur ou égal à 1.
     *
     * @param nodeId identité du noeud
     * @param edgeId identité de l'arête
     * @return le facteur par lequel la longueur de l'arête d'identité edgeId,
     * partant du nœud d'identité nodeId, doit être multipliée;
     * ce facteur doit impérativement être supérieur ou égal à 1.
     */
    double costFactor(int nodeId, int edgeId);

}
