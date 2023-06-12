package ch.epfl.javelo;

import java.util.function.DoubleUnaryOperator;

/**
 * La classe Functions contient des méthodes permettant
 * de créer des objets représentant des fonctions mathématiques des réels vers les réels,
 *
 * @author Georges Moussalli (316630)
 */
public final class Functions {

    /**
     * Constructeur privé de la classe qui est donc non instanciable
     */
    private Functions() {
    }

    /**
     * Retourne une fonction constante, dont la valeur est toujours y
     *
     * @param y
     * @return une fonction constante, dont la valeur est toujours y
     */
    public static DoubleUnaryOperator constant(double y) {
        return new Constant(y);
    }

    /**
     * Retourne une fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax ; lève IllegalArgumentException
     * si le tableau samples contient moins de deux éléments, ou si xMax est inférieur ou égal à 0.
     *
     * @param samples
     * @param xMax    valeur maximale de x
     * @return une fonction obtenue par interpolation linéaire entre les échantillons samples,
     * espacés régulièrement et couvrant la plage allant de 0 à xMax
     */
    public static DoubleUnaryOperator sampled(float[] samples, double xMax) {
        Preconditions.checkArgument(xMax > 0 && samples.length >= 2);
        return new Sampled(samples, xMax);
    }

    /**
     * Classe privée et imbriquée statiquement dans Functions
     */
    private static final class Constant implements DoubleUnaryOperator {
        private final double y;

        /**
         * Constructeur d'instance de la classe Constant
         *
         * @param y
         */
        Constant(double y) {
            this.y = y;
        }

        /**
         * Redéfinition de la méthode abstraite applyAsDouble
         *
         * @return y. C est une fonction constante
         */
        @Override
        public double applyAsDouble(double x) {
            return y;
        }

    }

    /**
     * Classe privée et imbriquée statiquement dans Functions
     */
    private static final class Sampled implements DoubleUnaryOperator {
        private final float[] samples;
        private final double xMax;

        /**
         * Constructeur d'instance de la classe Sampled
         *
         * @param xMax    valeur maximale de l'élément x
         */
        Sampled(float[] samples, double xMax) {
            this.samples = samples;
            this.xMax = xMax;
        }

        /**
         * Redéfinition de la méthpde applyAsDouble qui retorune l'interpolation de x entre deux valeurs
         * de samples
         *
         * @param x qui est à interpoler
         * @return l'interpolation de x entre deux éléments de samples
         */
        @Override
        public double applyAsDouble(double x) {

            double ecart = xMax / (samples.length - 1);
            double val = x / ecart;

            if (x <= 0) {
                return samples[0];
            }
            if (x >= xMax) {
                return samples[samples.length - 1];
            }
            return Math2.interpolate(samples[(int) val], samples[(int) val + 1], val - (int) val);
        }
    }

}
