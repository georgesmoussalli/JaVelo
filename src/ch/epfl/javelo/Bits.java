package ch.epfl.javelo;

/**
 * La classe Bits contient deux méthodes permettant d'extraire une séquence de bits d'un vecteur de 32 bits
 *
 * @author Georges Moussalli (316630)
 */
public final class Bits {

    /**
     * Constructeur privé de la classe qui est donc non instanciable
     */
    private Bits() {
    }

    /**
     * Extraction depuis un vecteur en représentation signée
     *
     * @param value  vecteur dont on extrait une séquence de bits
     * @param start  index du début de la plage à extraire
     * @param length longueur de la plage à extraire
     * @return
     */
    public static int extractSigned(int value, int start, int length) {
        Preconditions.checkArgument(0 <= length && length <=  Integer.SIZE && 1 <= start + length && start + length <=  Integer.SIZE);
        int temp = value << ( Integer.SIZE - (start + length));
        return temp >> ( Integer.SIZE - length);
    }

    /**
     * Extraction depuis un vecteur en représentation non-signée
     *
     * @param value  vecteur dont on extrait une séquence de bits
     * @param start  index du début de la plage à extraire
     * @param length longueur de la plage à extraire
     * @return
     */
    public static int extractUnsigned(int value, int start, int length) {
        Preconditions.checkArgument(0 <= length && length <=  (Integer.SIZE - 1) && 1 <= start + length && start + length <=  Integer.SIZE);
        int temp = value << ( Integer.SIZE - (start + length));
        return temp >>> ( Integer.SIZE - length);
    }
}
