package checker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class WebsiteAvailabilityChecker {

    private static volatile boolean shouldContinue = true;
    private static volatile boolean reset = false;
    private static volatile boolean record = false;
    private static List<String> records = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<String> urls = new ArrayList<>();

        System.out.println("Instrukcje:");
        System.out.println("Wpisz 'stop', aby zatrzymać odpytywanie stron.");
        System.out.println("Wpisz 'start', aby wznowić odpytywanie stron.");
        System.out.println("Wpisz 'odnowa', aby zacząć od nowa (wymaga ponownego wpisania adresów URL).");
        System.out.println("Wpisz 'rec', aby rozpocząć zapisywanie danych do pliku TXT.");
        System.out.println("Wpisz 'srec', aby zakończyć zapisywanie danych i zapisać plik.");

        System.out.println("\nWpisz 5 adresów URL do sprawdzenia:");

        // Pobranie 5 adresów URL od użytkownika
        for (int i = 0; i < 5; i++) {
            System.out.print("URL " + (i + 1) + ": ");
            urls.add(scanner.nextLine());
        }

        Thread checkerThread = new Thread(() -> {
            while (true) {
                if (shouldContinue) {
                    for (String url : urls) {
                        new WebsiteAvailabilityChecker().checkAvailability(url);

                        // Krótkie opóźnienie między sprawdzeniami różnych adresów URL
                        try {
                            Thread.sleep(3000); // 3 sekundy
                        } catch (InterruptedException e) {
                            System.out.println("Wątek został przerwany.");
                            e.printStackTrace();
                        }
                    }
                }

                // Opóźnienie przed ponownym sprawdzeniem wszystkich adresów URL
                try {
                    Thread.sleep(10000); // 10 sekund
                } catch (InterruptedException e) {
                    System.out.println("Wątek został przerwany.");
                    e.printStackTrace();
                }

                if (reset) {
                    urls.clear();
                    records.clear();
                    System.out.println("\nWpisz 5 nowych adresów URL do sprawdzenia:");

                    // Pobranie 5 nowych adresów URL od użytkownika
                    for (int i = 0; i < 5; i++) {
                        System.out.print("URL " + (i + 1) + ": ");
                        urls.add(scanner.nextLine());
                    }

                    reset = false;
                    shouldContinue = true;
                }
            }
        });

        checkerThread.start();

        while (true) {
            String command = scanner.nextLine().trim().toLowerCase();
            switch (command) {
                case "stop":
                    shouldContinue = false;
                    break;
                case "start":
                    shouldContinue = true;
                    break;
                case "odnowa":
                    shouldContinue = false;
                    reset = true;
                    break;
                case "rec":
                    record = true;
                    break;
                case "srec":
                    record = false;
                    saveRecordsToTxt();
                    break;
                default:
                    System.out.println("Nieznana komenda. Dostępne komendy: stop, start, odnowa, rec, srec.");
                    break;
            }
        }
    }

    public void checkAvailability(String url) {
        try {
            URL siteURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();

            long timestamp = System.currentTimeMillis() / 1000; // Konwersja na sekundy
            String message;

            if (responseCode == HttpURLConnection.HTTP_OK) {
                message = url + " jest dostępna. Kod odpowiedzi: " + responseCode + " | Czas: " + timestamp;
            } else {
                message = url + " nie jest dostępna. Kod odpowiedzi: " + responseCode + " | Czas: " + timestamp;
            }
            System.out.println(message);

            if (record) {
                records.add(message);
            }
        } catch (Exception e) {
            System.out.println("Wystąpił błąd podczas sprawdzania dostępności strony " + url + ": " + e.getMessage());
        }
    }

    private static void saveRecordsToTxt() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("WebsiteAvailability.txt"))) {
            for (String record : records) {
                writer.write(record);
                writer.newLine();
            }
            System.out.println("Dane zostały zapisane do pliku WebsiteAvailability.txt.");
        } catch (IOException e) {
            System.out.println("Nie udało się zapisać danych do pliku: " + e.getMessage());
        }
    }
}
