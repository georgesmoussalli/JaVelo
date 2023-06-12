package ch.epfl.javelo.gui;

import ch.epfl.javelo.Preconditions;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;

import static java.nio.file.Files.*;


/**
 * Représente un gestionnaire de tuiles OSM
 *
 * @author Georges Moussalli (316630)
 */
public final class TileManager {

    private final static int INIT_CAPACITY = 100;
    private final static float FACTOR = 0.75F;
    private final Path path;
    private final String server;
    private static final String TILE_CACHE_CYCLOSM  ="cyclosm-cache";
    private static final LinkedHashMap<TileId, Image> cacheMemory =
            new LinkedHashMap<>(INIT_CAPACITY, FACTOR, true);

    /**
     * Constructeur de tuiles
     * @param path le chemin d'accès au répertoire contenant le cache disque
     * @param server le nom du serveur de tuile
     */
    public TileManager(Path path, String server) {
        this.path = path;
        this.server = server;
    }

    /**
     * Prend en argument l'identité d'une tuile et retourne son image
     * @param tileId Identité de la tuile
     * @return retourne son image
     * @throws IOException en cas d'erreur d'entrée ou de sortie
     */
    public Image imageForTileAt(TileId tileId) throws IOException {
        Preconditions.checkArgument(TileId.isValid(tileId.zoomLevel, tileId.x, tileId.y));

        if (cacheMemory.containsKey(tileId)) {
            return cacheMemory.get(tileId);
        }

        Path finalPath = path.resolve(tileId.zoomLevel + "/" + tileId.x + "/" + tileId.y + ".png");
        Files.createDirectories(path);

        if (!exists(finalPath)) {
            URL url = getUrl(tileId);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "JaVelo");
            Files.createDirectories(finalPath.getParent());
            try (InputStream tile = connection.getInputStream()) {
                tile.transferTo(newOutputStream(finalPath));
            }
        }

        try (InputStream tile = new FileInputStream(finalPath.toFile())) {
            if (cacheMemory.size() == INIT_CAPACITY) {
                cacheMemory.remove(cacheMemory.keySet().iterator().next());
            }
            Image image = new Image(tile);
            cacheMemory.put(tileId, image);
            return image;
        }
    }

    private URL getUrl(TileId tileId) throws MalformedURLException {
        if(path == Path.of(TILE_CACHE_CYCLOSM)) return new URL("https://" + server + "/"
                + tileId.zoomLevel + "/" + tileId.x + "/" + tileId.y + "/cyclosm.png");
        else
        return new URL("https://" + server + "/"
                + tileId.zoomLevel + "/" + tileId.x + "/" + tileId.y + ".png");
    }


    /**
     * L'enregistrement TileId, imbriqué dans la classe TileManager
     * représente l'identité d'une tuile OSM
     */
    public record TileId(int zoomLevel, int x, int y) {
        /**
         * Retournant vrai si et seulement s'ils constituent une identité de tuile valide
         * @param zoomLevel niveau de zoom de la tuile
         * @param x l'index X de la tuile
         * @param y l'index Y de la tuile
         * @throws IllegalArgumentException si le zoom est inferieur à 0 ou x ou y
         * @return  retournant vrai si et seulement s'ils constituent une identité de tuile valide
         */
        public static boolean isValid(int zoomLevel, int x, int y) {
            Preconditions.checkArgument(zoomLevel >= 0 && x >= 0 && y >= 0);
            int max = (int) Math.pow(2, zoomLevel) - 1;
            return x <= max && y <= max;
        }
    }
}