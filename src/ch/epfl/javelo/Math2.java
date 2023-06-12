package ch.epfl.javelo;

import static java.lang.Math.*;

/**
 * Regroupe les formules mathématiques nécessaires pour le programme.
 *
 * @author Georges Moussalli (316630)
 */

public final class Math2 {

    /**
     * Constructeur privé de la classe.
     */
    private Math2() {
    }

    /**
     * Retourne la partie entière par excès de la division de x par y.
     *
     * @param x numérateur de la division
     * @param y dénominateur de la division
     * @throws IllegalArgumentException si (x < 0) || (y < 0)
     * @return la partie entière par excès de la division de x par y
     */

    public static int ceilDiv(int x, int y) {
        Preconditions.checkArgument((x >= 0) && (y > 0));
        return (x + y - 1) / y;
    }

    /**
     * Retourne la coordonnée y du point se trouvant sur la droite passant
     * par (0,y0) et (1,y1) et de coordonnée x donnée.
     *
     * @param y0 ordonnée du point d'abscisse 0
     * @param y1 ordonnée du point d'abscisse 1
     * @param x  Abscisse du point recherché
     * @return l'ordonnée du point d'abscisse x
     */

    public static double interpolate(double y0, double y1, double x) {
        return fma((y1 - y0), x, y0);
    }

    /**
     * limite la valeur v à l'intervalle allant de min à max, en retournant min si
     * v est inférieure à min, max si v est supérieure à max,
     * et v sinon.
     *
     * @param min valeur minimale de v
     * @param v   valeur que l'on veut borner
     * @param max valeur maximale de v
     * @throws IllegalArgumentException si min > max
     * @return min si v est inférieure à min, max si v est supérieure à max, et v sinon.
     */

    public static int clamp(int min, int v, int max) {
        Preconditions.checkArgument(min <= max);
        if (v < min) {
            return min;
        } else if (v > max) {
            return max;
        } else return v;
    }

    /**
     * limite la valeur v à l'intervalle allant de min à max, en retournant min si
     * v est inférieure à min, max si v est supérieure à max,
     * et v sinon.
     *
     * @param min valeur minimale de v
     * @param v   valeur que l'on veut borner
     * @param max valeur maximale de v
     * @throws IllegalArgumentException si min > max
     * @return min si v est inférieure à min, max si v est supérieure à max, et v sinon.
     */
    public static double clamp(double min, double v, double max) {
        Preconditions.checkArgument(min <= max);
        if (v < min) {
            return min;
        } else return Math.min(v, max);
    }

    /**
     * Retourne le sinus hyperbolique inverse de x
     *
     * @param x
     * @return sinus hyperbolique inverse de x
     */
    public static double asinh(double x) {
        return log(x + sqrt(1 + x * x));
    }

    /**
     * Retourne le produit scalaire de deux vecteurs
     *
     * @param uX coordonnée du premier vecteur
     * @param uY coordonnée du premier vecteur
     * @param vX coordonnée du deuxième vecteur
     * @param vY coordonnée du deuxième vecteur
     * @return le produit scalaire des deux vecteurs dont les coordonnées sont les arguments
     */

    public static double dotProduct(double uX, double uY, double vX, double vY) {
        return fma(uX, vX, fma(uY, vY, 0));
    }

    /**
     * Retourne la norme au carré d'un vecteur
     *
     * @param uX coordonnée du vecteur
     * @param uY coordonnée du vecteur
     * @return la norme au carré du vecteur
     */

    public static double squaredNorm(double uX, double uY) {
        return dotProduct(uX, uY, uX, uY);
    }

    /**
     * Retourne la norme d'un vecteur
     *
     * @param uX coordonnée du vecteur
     * @param uY coordonnée du vecteur
     * @return la norme d'un vecteur
     */

    public static double norm(double uX, double uY) {
        return sqrt(squaredNorm(uX, uY));
    }

    /**
     * Retourne la longueur de la projection d'un vecteur AB sur un autre AP
     *
     * @param aX abscisse du point A
     * @param aY ordonnée du point A
     * @param bX abscisse du point B
     * @param bY ordonnée du point B
     * @param pX abscisse du point P
     * @param pY ordonnée du point P
     * @return la longeur de la projection
     */

    public static double projectionLength
    (double aX, double aY, double bX, double bY, double pX, double pY) {
        return (dotProduct(pX - aX, pY - aY, bX - aX, bY - aY))
                / norm(bX - aX, bY - aY);
    }

}



