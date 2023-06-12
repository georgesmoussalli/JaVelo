package ch.epfl.javelo.projection;

/**
 * Fixe les constantes du programme, qui sont les bornes de la carte Suisse
 *
 * @author Georges Moussalli (316630)
 */

public final class SwissBounds {

    /**
     * Constructeur privé de la classe
     */

    private SwissBounds() {
    }

    public final static double MIN_E = 2485000;
    public final static double MAX_E = 2834000;
    public final static double MIN_N = 1075000;
    public final static double MAX_N = 1296000;
    public final static double WIDTH = MAX_E - MIN_E;
    public final static double HEIGHT = MAX_N - MIN_N;

    /**
     * Vérifie qu'un point est contenu dans les bornes de la carte
     *
     * @param e Coordonnée du point
     * @param n Coordonnée du point
     * @return True si le point appartient aux bornes et False sinon
     */

    public static boolean containsEN(double e, double n) {
        return (MIN_E <= e && MAX_E >= e && MIN_N <= n && MAX_N >= n);
    }
}