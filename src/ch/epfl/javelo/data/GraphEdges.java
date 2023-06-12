package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.Q28_4;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static ch.epfl.javelo.Bits.*;
import static ch.epfl.javelo.Q28_4.*;
import static java.lang.Short.toUnsignedInt;

/**
 * Représente le tableau de toutes les arêtes du graphe JaVelo
 *
 * @param edgesBuffer la mémoire tampon contenant la valeur d'attributs pour la totalité des arêtes du graphe
 * @param profileIds  la mémoire tampon contenant la valeur d'attributs pour la totalité des arêtes du graphe
 * @param elevations  la mémoire tampon contenant la totalité des échantillons des profils, compressés ou non
 * @author Georges Moussalli (316630)
 */

public record GraphEdges(ByteBuffer edgesBuffer, IntBuffer profileIds, ShortBuffer elevations) {

    public static final int OFFSET_NB_OF_BYTES = 10;
    public static final int OFFSET_LENGTH_METERS = Integer.BYTES;
    public static final int OFFSET_HEIGHT_METERS = OFFSET_LENGTH_METERS + Short.BYTES;
    public static final int OFFSET_ID_OSM = OFFSET_HEIGHT_METERS + Short.BYTES;
    public static final int LENGTH = 30;
    public static final int SIGN_BIT = 1;

    /**
     * @param edgeId identité de l'arête
     * @return vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient
     * @ Retourne vrai ssi l'arête d'identité donnée va dans le sens inverse de la voie OSM dont elle provient
     */
    public boolean isInverted(int edgeId) {
    return (extractUnsigned((edgesBuffer.getInt(edgeId * OFFSET_NB_OF_BYTES)), LENGTH + SIGN_BIT, SIGN_BIT) == 1);
    }

    /**
     * Retourne l'identité du nœud destination de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return l'identité du nœud destination de l'arête d'identité donnée
     */
    public int targetNodeId(int edgeId) {
        if (isInverted(edgeId)) {
            return ~(edgesBuffer.getInt(edgeId * OFFSET_NB_OF_BYTES));
        } else
            return extractUnsigned(edgesBuffer.getInt(edgeId * OFFSET_NB_OF_BYTES), 0, LENGTH + SIGN_BIT);
    }

    /**
     * Retourne la longueur, en mètres, de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return la longueur, en mètres, de l'arête d'identité donnée
     */
    public double length(int edgeId) {
        return asDouble
                (toUnsignedInt(edgesBuffer.getShort(OFFSET_LENGTH_METERS + edgeId * OFFSET_NB_OF_BYTES)));
    }

    /**
     * Retourne le dénivelé positif, en mètres, de l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return le dénivelé positif, en mètres, de l'arête d'identité donnée
     */
    public double elevationGain(int edgeId) {
        return asDouble
                (toUnsignedInt(edgesBuffer.getShort(OFFSET_HEIGHT_METERS + edgeId * OFFSET_NB_OF_BYTES)));
    }

    /**
     * Retourne vrai ssi l'arête d'identité donnée possède un profil
     *
     * @param edgeId identité de l'arête
     * @return vrai ssi l'arête d'identité donnée possède un profil
     */
    public boolean hasProfile(int edgeId) {
        int temp = extractUnsigned(profileIds.get(edgeId), LENGTH, Short.BYTES);
    return temp != 0;
    }

    /**
     * Retourne le tableau des échantillons du profil de l'arête d'identité donnée,
     * qui est vide si l'arête ne possède pas de profil
     *
     * @param edgeId identité de l'arête
     * @return le tableau des échantillons du profil de l'arête d'identité donnée,
     * qui est vide si l'arête ne possède pas de profil
     */
    public float[] profileSamples(int edgeId) {
        int number = 1 + (Math2.ceilDiv((Short.toUnsignedInt(edgesBuffer.getShort(edgeId * OFFSET_NB_OF_BYTES + OFFSET_LENGTH_METERS))),
                Q28_4.ofInt(2)));
        int first = extractUnsigned(profileIds.get(edgeId), 0, 30);
        float[] profile = new float[number];
        float[] backProfile = new float[number];

        profile[0] = (asFloat(toUnsignedInt(elevations.get(first))));


        switch (extractUnsigned(profileIds.get(edgeId), 30, 2)) {
            case 0:
                float[] temp0 = new float[0];
                return temp0;

            case 1:
                for (int i = 0; i < number; i++) {
                    profile[i] = asFloat((toUnsignedInt(elevations.get(first + i))));
                }
                break;

            case 2:
                for (int i = 1; i < number; i++) {
                    profile[i] = asFloat(extractSigned(toUnsignedInt
                            (elevations.get(((i - 1) / 2) + 1 + first)), 8 - ((i + 1) % 2) * 8, 8)) + profile[i - 1];
                }

                break;

            case 3:
                for (int i = 1; i < number; i++) {
                    profile[i] = asFloat(extractSigned(toUnsignedInt(elevations.get(first + ((i - 1) / 4) + 1)), 12 - ((i + 3) % 4) * 4, 4))
                            + profile[i - 1];
                }
                break;

        }
        for (int i = 0; i < number; i++) {
            backProfile[i] = profile[number - i - 1];
        }

        if (isInverted(edgeId)) {
            return backProfile;
        } else
            return profile;
    }

    /**
     * Retourne l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée
     *
     * @param edgeId identité de l'arête
     * @return l'identité de l'ensemble d'attributs attaché à l'arête d'identité donnée
     */
    public int attributesIndex(int edgeId) {
        return toUnsignedInt(edgesBuffer.getShort(OFFSET_ID_OSM + edgeId * OFFSET_NB_OF_BYTES));
    }
}