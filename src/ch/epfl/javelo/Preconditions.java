package ch.epfl.javelo;

/**
 * Classe qui contient la méthode de vérification de validité des arguments des méthodes
 * de tous le programme selon les préconditions à remplir.
 *
 * @author Georges Moussalli (316630)
 */

public final class Preconditions {

    /**
     * Constructeur par défaut privé de la classe.
     */
    private Preconditions() {
    }

    /**
     * Lance une erreur si la ou les précondtions en arguments ne sont pas vérifiées.
     * @param shouldBeTrue Préconditon(s)
     * @throws IllegalArgumentException si shouldBeTrue est faux
     *
     */

    public static void checkArgument(boolean shouldBeTrue) {
        if (!shouldBeTrue) throw new IllegalArgumentException();
    }
}
