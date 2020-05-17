import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("Willkommen beim Smart Greenhouse Daten Generator.\n" +
                "Diese Anwendung generiert automatisch Daten, die von einem Smart Greenhouse stammen könnten und bieten dadurch die " +
                "Möglichkeit ein solches zu simulieren.");
        System.out.println("Geben Sie ein, welche der folgenden Varianten genutzt werden soll:\n" +
                "\t1. Standard Prototyp (4x moisture, 2x temp, 2x humidity, 1x brightness)\n" +
                "\t2. Alternative 1 (2x moisture, 2x temp, 2x humidity, 1x brightness)\n" +
                "\t3. Alternative 2 (10x moisture, 2x temp, 2x humidity, 1x brightness)");
        boolean valid = false;
        int alternative = 0;
        while(valid == false) {
            System.out.print("Auswahl Variante: ");
            String variant = readConsole();
            if(variant.contentEquals("1") || variant.contentEquals("2") || variant.contentEquals("3")) {
                valid = true;
                alternative = Integer.parseInt(variant);
            }
            else {
                System.out.println("Ungültige Eingabe! Bitte geben Sie eine gültige Nummer ein.");
            }
        }

        System.out.println("Beginne mit der Generierung von Daten für das Greenhouse mit der Id " + alternative);
        GeneratorThread thread = new GeneratorThread(alternative, alternative);
        thread.run();
        logger.info("Generator thread started");
        System.out.println("Drücke Enter, um die Anwendung zu schließen.");
        readConsole();
        System.out.println("Beende Applikation");
        thread.stopExecution();
    }

    private static String readConsole() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        }
        catch (IOException e) {
            System.out.println("Reading from console failed: " + e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
