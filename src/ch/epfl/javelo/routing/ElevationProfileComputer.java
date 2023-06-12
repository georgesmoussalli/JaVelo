package ch.epfl.javelo.routing;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;

import static java.lang.Float.isNaN;

/**
 * Cette classe permet de calculer le profil en long d'un itinéraire donné.
 *
 * @author Georges Moussalli (316630)
 */
public final class ElevationProfileComputer {

    /**
     * Constructeur privé de la classe qui est donc non instanciable
     */
    private ElevationProfileComputer() {
    }

    /**
     * Retourne le profil en long de l'itinéraire route,
     * en garantissant que l'espacement entre les échantillons du profil est d'au maximum maxStepLength mètres;
     * lève IllegalArgumentException si cet espacement n'est pas strictement positif.
     *
     * @param route         qui représente l'itinéraire
     * @param maxStepLength espacement maximal entre les échantillons
     * @return le profil en long de l'itinéraire route
     */
    public static ElevationProfile elevationProfile(Route route, double maxStepLength) {
        Preconditions.checkArgument(maxStepLength > 0);

        int nb = ((int) Math.ceil((route.length() / maxStepLength)) + 1);
        float[] r = new float[nb];
        double actualStep = route.length() / (nb - 1);

        for (int i = 0; i < nb; i++) {
            r[i] = (float) route.elevationAt(actualStep * i);
        }
        int first = 0;
        while (first < nb - 1 && isNaN(r[first])) {
            first++;
        }
        if (first < nb - 1) {
            Arrays.fill(r, 0, first, r[first]);
        } else {
            Arrays.fill(r, 0, first, 0);
        }


        int last = nb - 1;
        while (isNaN(r[last])) {
            last--;
        }
        Arrays.fill(r, last + 1, nb, r[last]);

        for (int u = first; u <= last; u++) {
            int firstHole;
            int lastHole;

            if (isNaN(r[u])) {
                firstHole = u - 1;
                lastHole = u;
                while (isNaN(r[lastHole])) {
                    lastHole++;
                }
                for (int v = 1; v <= lastHole - firstHole; v++) {
                    r[v + firstHole] = (float) Math2.interpolate(r[firstHole], r[lastHole], (double) v / ((double) (lastHole - firstHole)));
                }
            }
        }

        return new ElevationProfile(route.length(), r);
    }
}