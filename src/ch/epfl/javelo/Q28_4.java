package ch.epfl.javelo;

import java.lang.Math;


/**
 * La classe Q28_4 permet de convertir des nombres entre la représentation Q28.4 et d'autres représentations
 *
 * @author Georges Moussalli (316630)
 */
public final class Q28_4 {

    public static final int DECALAGE = 4;

    /**
     * Constructeur privé de la classe qui est donc non instanciable
     */
    private Q28_4() {
    }

    /**
     * Retourne la valeur Q28.4 correspondant à l'entier donné
     *
     * @param i entier à convertir
     * @return valeur Q28_4 correspondante
     */
    public static int ofInt(int i) {
        return i << DECALAGE;
    }

    /**
     * Retourne la valeur de type double égale à la valeur Q28.4 donnée
     *
     * @param q28_4 valeur en q28_4 à convertir
     * @return double correspondant
     */
    public static double asDouble(int q28_4) {
        return Math.scalb((double) q28_4, -DECALAGE);
    }

    /**
     * Retourne la valeur de type float égale à la valeur Q28.4 donnée
     *
     * @param q28_4 valeur en q28_4 à convertir
     * @return float correspondant
     */
    public static float asFloat(int q28_4) {
        return Math.scalb( q28_4, -DECALAGE);
    }
}
