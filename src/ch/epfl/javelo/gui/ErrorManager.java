package ch.epfl.javelo.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;


/**
 *La classe ErrorManager du sous-paquetage gui, publique et instanciable (donc finale), gère l'affichage de messages d'erreur.
 *
 * @author Georges Moussalli (316630)
 */
public final class ErrorManager {

    private static final int MAX_MESSAGE_LENGTH = 100;
    private final Pane pane;
    private final Text text;

    private final FadeTransition fadeIn;
    private final FadeTransition fadeOut;
    private final PauseTransition pause;
    private SequentialTransition transition;

    /**
     * Constructeur public
     */
    public ErrorManager() {
        text = new Text();
        pane = new VBox(text);
        pane.getStylesheets().add("error.css");
        pane.setMouseTransparent(true);

        fadeIn = new FadeTransition(Duration.millis(200));
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.8);

        pause = new PauseTransition(Duration.millis(2000));

        fadeOut = new FadeTransition(Duration.millis(500));
        fadeOut.setFromValue(0.8);
        fadeOut.setToValue(0);

        transition = new SequentialTransition();
    }

    /**
     *  Prenant en argument une chaîne de caractères représentant
     *  un (court) message d'erreur et le faisant apparaître temporairement à l'écran,
     *  accompagné d'un son indiquant l'erreur.
     * @param message message d'erreur
     */
    public void displayError(String message) {
        if (message.length() < MAX_MESSAGE_LENGTH) {
            // émission du son d'erreur
            java.awt.Toolkit.getDefaultToolkit().beep();

            text.setText(message);

            // on arrête l'animation précédente
            transition.stop();
            transition = new SequentialTransition(pane, fadeIn, pause, fadeOut);
            transition.play();
        }
    }

    /**
     * Retourne le panneau, de type Pane, sur lequel apparaissent les messages d'erreur
     * @return le panneau, de type Pane, sur lequel apparaissent les messages d'erreur
     */
    public Pane pane() {
        return pane;
    }
}
