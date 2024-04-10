import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class AdvancedFuzzer {

    public static void main(String[] args) {
        String targetURL = "http://192.168.56.213/";
        String filename = "C:\\Users\\art86\\Documents\\rockyou.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String urlToCheck = targetURL + line.trim();
                try {
                    URL url = new URL(urlToCheck);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        System.out.println("Найден скрытый каталог или файл: " + urlToCheck);
                    } else {
                        System.out.println("Каталог или файл не найден: " + urlToCheck);
                    }
                    connection.disconnect();
                } catch (IOException e) {
                    System.out.println("Ошибка при обращении к URL: " + urlToCheck);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
