package ch.epfl.javelo.data;

import ch.epfl.javelo.Preconditions;

import java.util.StringJoiner;

/**
 * Permet de créer un ensemble d'attributs
 * @param bits
 * @author Georges Moussalli (316630)
 */

public record AttributeSet(long bits) {

    /**
     * Constructeur d'un ensemble d'éléments
     *
     * @param bits qui définit quels éléments appartiennent à l'ensemble
     * @throws IllegalArgumentException si bits >>> Attribute.COUNT != 0L
     */
    public AttributeSet {
        Preconditions.checkArgument((bits >>> Attribute.COUNT) == 0L);
    }

    /**
     * Construit un ensemble d'attributs à partir des éléments eux-mêmes
     *
     * @param attributes éléments qui appartiennent à la liste créée
     * @return Ensemble contenant tous les éléments en arguments
     */
    public static AttributeSet of(Attribute... attributes) {
        long bits = 0L;
        for (Attribute i : attributes) {
            bits = bits | 1L << i.ordinal();
        }
        return new AttributeSet(bits);
    }

    /**
     * Détermine si un attribut appartient à un Set d'attributs
     *
     * @param attribute attribut en question
     * @return vrai si l'attribut appartient au Set, faux sinon
     */
    public boolean contains(Attribute attribute) {
        long mask = 1L << attribute.ordinal();
        return ((this.bits & mask) == mask);
    }


    /**
     * Détermine si l'intersection entre deux Sets est vide ou non
     *
     * @param that Set comparé passé en argument
     * @return vrai si l'intersection n'est pas vide, faux si elle l'est
     */
    public boolean intersects(AttributeSet that) {
        return ((this.bits & that.bits) != 0L);

    }

    /**
     * Override de la méthode toString() qui renvoie la keyvalue des arguments d'un set
     *
     * @return une chaîne composée de la représentation textuelle des éléments
     * de l'ensemble entourés d'accolades ({}) et séparés par des virgules
     */
    @Override
    public String toString() {
        StringJoiner j = new StringJoiner(",", "{", "}");

        for (Attribute i : Attribute.ALL) {
            if (this.contains(i)) {
                j.add(i.keyValue());
            }
        }
        return j.toString();
    }
}
