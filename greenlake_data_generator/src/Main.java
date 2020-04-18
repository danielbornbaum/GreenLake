import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        System.out.println("Willkommen beim Smart Greenhouse Daten Generator.\n" +
                "Diese Anwendung generiert automatisch Daten, die von einem Smart Greenhouse stammen könnten und bieten dadurch die " +
                "Möglichkeit ein solches zu simulieren.");
        System.out.println("Geben Sie zunächst ein, welche der folgenden Varianten genutzt werden soll:\n" +
                "\t1. Standard Prototyp (4x moisture, 2x temp, 2x humidity, 1x brightness)\n" +
                "\t2. Alternative 1 (2x moisture, 2x temp, 2x humidity, 1x brightness)");
        boolean valid = false;
        int alternative = 0;
        while(valid == false) {
            System.out.print("Auswahl Variante: ");
            String variant = readConsole();
            if(variant.contentEquals("1") || variant.contentEquals("2")) {
                valid = true;
                alternative = Integer.parseInt(variant);
            }
            else {
                System.out.println("Ungültige Eingabe! Bitte geben Sie eine gültige Nummer ein.");
            }
        }
        System.out.print("Geben Sie eine ID für das Greenhouse an. Sollten mehrere Greenhouses simuliert werden, bitte mit anderen Quellen abstimmen, damit jedes eine andere ID verwendet: ");
        valid = false;
        int id = 0;
        while(valid == false) {
            String idString = readConsole();
            try {
                id = Integer.parseInt(idString);
                valid = true;
            }
            catch (Exception e) {
                System.out.println("Ungültige Eingabe! Bitte geben Sie eine gültige ID ein.");
            }
        }

        System.out.println("Beginne mit der Generierung von Daten für das Greenhouse mit der Id " + id);
        GeneratorThread thread = new GeneratorThread(id, alternative);

        System.out.println("Drücke eine beliebige Taste, um die Anwendung zu schließen.");
        readConsole();
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
