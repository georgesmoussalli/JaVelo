package ch.epfl.javelo.data;

import ch.epfl.javelo.Math2;
import ch.epfl.javelo.projection.PointCh;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static ch.epfl.javelo.projection.SwissBounds.*;
import static java.lang.Short.toUnsignedInt;

/**
 * Représente le tableau contenant les 16384 secteurs de JaVelo
 * @buffer la mémoire tampon contenant la valeur des attributs de la totalité des secteurs
 * @author Georges Moussalli (316630)
 */

public record GraphSectors(ByteBuffer buffer) {
    private static final int OFFSET_ID = Integer.BYTES + Short.BYTES;
    private static final int OFFSET_NB = OFFSET_ID - Short.BYTES;
    private static final double NB_SECTOR_PER_ROW = 128.0;
    private static final double WIDTH_SECTOR = WIDTH / NB_SECTOR_PER_ROW;
    private static final double HEIGHT_SECTOR = HEIGHT / NB_SECTOR_PER_ROW;

    /**
     * Un enregistrement imbriqué nommé Sector, représentant un secteur
     */
    public record Sector(int startNodeId, int endNodeId) {
    }

    /**
     * Retourne la liste de tous les secteurs ayant une intersection
     * avec le carré centré au point donné et de côté égal au double de la distance donnée
     *
     * @param center   centre du carré
     * @param distance distance entre le centre et le bord, égale à un demi-côté
     * @return la ;iste des secteurs ayant une intersection avec le carré donné
     */
    public List<Sector> sectorsInArea(PointCh center, double distance) {

        int xDown = (int) ((center.e() - distance - MIN_E) / WIDTH_SECTOR);
        int xUp = (int) ((center.e() + distance - MIN_E) / WIDTH_SECTOR);
        int yDown = (int) ((center.n() - distance - MIN_N) / HEIGHT_SECTOR);
        int yUp = (int) ((center.n() + distance - MIN_N) / HEIGHT_SECTOR);

        xDown = Math2.clamp(0, xDown, 127);
        xUp = Math2.clamp(0, xUp, 127);
        yDown = Math2.clamp(0, yDown, 127);
        yUp = Math2.clamp(0, yUp, 127);


        ArrayList<Sector> intersection = new ArrayList<>();

        for (int x = xDown; x <= xUp; x++) {
            for (int y = yDown; y <= yUp; y++) {
                int index = x + ((int) (NB_SECTOR_PER_ROW)) * y;
                intersection.add(new Sector
                        (buffer.getInt(index * OFFSET_ID),
                                buffer.getInt(index * OFFSET_ID) + toUnsignedInt(buffer.getShort(OFFSET_ID * index + OFFSET_NB))));
            }
        }
        return intersection;
    }

}


