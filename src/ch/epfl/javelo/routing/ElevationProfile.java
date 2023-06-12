package ch.epfl.javelo.routing;

import ch.epfl.javelo.Functions;
import ch.epfl.javelo.Preconditions;

import java.util.DoubleSummaryStatistics;
import java.util.function.DoubleUnaryOperator;

/**
 * Représente le profil en long d'un itinéraire simple ou multiple.
 *
 * @author Georges Moussalli (316630)
 */
public final class ElevationProfile {

    private final double length;
    private final DoubleUnaryOperator profile;
    private DoubleSummaryStatistics s = new DoubleSummaryStatistics();
    private double ascent = 0;
    private double descent = 0;

    /**
     * Constructeur public qui construit le profil en long d'un itinéraire de longueur length (en mètres) et dont les échantillons d'altitude, répartis uniformément le long de l'itinéraire,
     * sont contenus dans elevationSamples ;
     * @param length           longueur de l'itinéraire en mètres
     * @param elevationSamples échantillons d'altitude répartis uniformément le long de l'itinéraire
     * @throws IllegalArgumentException si la longueur est négative ou nulle, ou si le tableau d'échantillons contient moins de 2 éléments
     */
    public ElevationProfile(double length, float[] elevationSamples) {
        Preconditions.checkArgument(length > 0 && elevationSamples.length >= 2);
        this.length = length;
        profile = Functions.sampled(elevationSamples, length);
        s.accept(elevationSamples[0]);

        for (int i = 1; i < elevationSamples.length; i++) {
            s.accept(elevationSamples[i]);
            if (elevationSamples[i - 1] > elevationSamples[i])
                descent = descent + elevationSamples[i - 1] - elevationSamples[i];
            if (elevationSamples[i - 1] < elevationSamples[i])
                ascent = ascent + elevationSamples[i] - elevationSamples[i - 1];
        }
    }

    /**
     * Retourne la longueur du profil, en mètres,
     *
     * @return la longueur du profil, en mètres,
     */
    public double length() {
        return this.length;
    }

    /**
     * Retourne l'altitude minimum du profil, en mètres
     *
     * @return l'altitude minimum du profil, en mètres
     */
    public double minElevation() {
        return s.getMin();
    }

    /**
     * Retourne l'altitude maximum du profil, en mètres
     *
     * @return l'altitude maximum du profil, en mètres
     */
    public double maxElevation() {
        return s.getMax();
    }

    /**
     * Retourne le dénivelé positif total du profil, en mètres
     *
     * @return le dénivelé positif total du profil, en mètres
     */
    public double totalAscent() {
        return ascent;
    }

    /**
     * Retourne le dénivelé négatif total du profil, en mètres
     *
     * @return le dénivelé positif total du profil, en mètres
     */
    public double totalDescent() {
        return descent;
    }


    /**
     * qui retourne l'altitude du profil à la position donnée, qui n'est pas forcément comprise entre 0 et la longueur du profil;
     * le premier échantillon est retourné lorsque la position est négative,
     * le dernier lorsqu'elle est supérieure à la longueur.
     *
     * @param position position où l'on cherche l'altitude du profil
     * @return l'altitude du profil à la position donnée, qui n'est pas forcément comprise entre 0 et la longueur du profil;
     * le premier échantillon est retourné lorsque la position est négative,
     * le dernier lorsqu'elle est supérieure à la longueur.
     */
    public double elevationAt(double position) {
        return profile.applyAsDouble(position);
    }
}
