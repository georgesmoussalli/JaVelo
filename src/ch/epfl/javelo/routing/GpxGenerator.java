package ch.epfl.javelo.routing;

import ch.epfl.javelo.projection.PointCh;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;


/**
 * La classe GpxGenerator du sous-paquetage routing, publique et non instanciable, représente un générateur d'itinéraire au format GPX
 *
 * @author Georges Moussalli (316630)
 */
public class GpxGenerator {


    private GpxGenerator() {
    }

    /**
     * Prend en arguments un itinéraire et le profil de cet itinéraire et retourne le document GPX (de type Document) correspondant
     * @param route itinéraire
     * @param elevationProfile profil de l'itinéraire
     * @return retourne le document GPX (de type Document) correspondant
     */
    public static Document createGpx(Route route, ElevationProfile elevationProfile) {
        Document doc = newDocument();



        Element root = doc
                .createElementNS("http://www.topografix.com/GPX/1/1",
                        "gpx");
        doc.appendChild(root);

        root.setAttributeNS(
                "http://www.w3.org/2001/XMLSchema-instance",
                "xsi:schemaLocation",
                "http://www.topografix.com/GPX/1/1 "
                        + "http://www.topografix.com/GPX/1/1/gpx.xsd");
        root.setAttribute("version", "1.1");
        root.setAttribute("creator", "JaVelo");

        Element metadata = doc.createElement("metadata");
        root.appendChild(metadata);

        Element name = doc.createElement("name");
        metadata.appendChild(name);
        name.setTextContent("Route JaVelo");

        Element rte = doc.createElement( "rte");
        root.appendChild(rte);

        Iterator<Edge> i = route.edges().iterator();
        int length = 0;

        for (PointCh p : route.points()) {
                Element rtept = doc.createElement("rtept");
                Element ele = doc.createElement("ele");

                rtept.setAttribute("lat", String.format(Locale.ROOT, "%.5f", Math.toDegrees(p.lat())));
                rtept.setAttribute("lon", String.format(Locale.ROOT, "%.5f", Math.toDegrees(p.lon())));

                ele.setTextContent(String.format(Locale.ROOT, "%.2f", elevationProfile.elevationAt(length)));


                    if(i.hasNext()){
                       length += i.next().length();}

            rte.appendChild(rtept);
            rtept.appendChild(ele);

            }


        return doc;
    }

    /**
     * la seconde, nommée p. ex. writeGpx, qui prend en arguments un nom de fichier, un itinéraire
     * et le profil de cet itinéraire et écrit le document GPX correspondant dans le fichier,
     * ou lève IOException en cas d'erreur d'entrée/sortie
     * @param s nom du fichier
     * @param route itinéraire
     * @param elevationProfile elevation du profil
     * @throws IOException en cas d'erreur d'entrée ou de sortie
     */
    public static void writeGpx (String s, Route route, ElevationProfile elevationProfile) throws IOException {
        Document doc = createGpx( route, elevationProfile);
        try( Writer w = Files.newBufferedWriter(Path.of(s))){
        Transformer transformer = TransformerFactory
                .newDefaultInstance()
                .newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc),
                new StreamResult(w));
        } catch (TransformerException e) {
            throw new Error(e);
        }
    }

    private static Document newDocument() {
        try {
            return DocumentBuilderFactory
                    .newDefaultInstance()
                    .newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException e) {
            throw new Error(e);
        }
    }

    }

