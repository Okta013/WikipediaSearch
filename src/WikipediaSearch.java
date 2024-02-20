import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class WikipediaSearch {
    public static void main(String[] args) {
        // Шаг 1: Считать запрос
        String query = readQueryFromConsole();

        // Шаг 2: Сделать запрос к серверу
        String url = "https://ru.wikipedia.org/w/api.php?action=query&format=json&list=search&srsearch=" + query;
        String jsonResponse = sendGetRequest(url);

        // Шаг 3: Распарсить ответ
        JsonObject response = new Gson().fromJson(jsonResponse, JsonObject.class);
        JsonObject queryObject = response.getAsJsonObject("query");
        JsonArray searchResults = queryObject.getAsJsonArray("search");

        // Шаг 4: Вывести результат
        int totalResults = queryObject.get("searchinfo").getAsJsonObject().get("totalhits").getAsInt();
        System.out.println("Найдено статей: " + totalResults);

        System.out.println("Ссылки на первые 5 статей:");
        for (int i = 0; i < 5 && i < searchResults.size(); i++) {
            JsonObject result = searchResults.get(i).getAsJsonObject();
            String title = result.get("title").getAsString();
            String urlTitle = title.replaceAll(" ", "_");
            System.out.println("- " + "https://ru.wikipedia.org/wiki/" + urlTitle);
        }

        if (searchResults.size() > 0) {
            JsonObject firstResult = searchResults.get(0).getAsJsonObject();
            String firstTitle = firstResult.get("title").getAsString();
            String firstUrlTitle = firstTitle.replaceAll(" ", "_");
            String firstPageId = firstResult.get("pageid").getAsString();
            String firstPageUrl = "https://ru.wikipedia.org/wiki/" + firstUrlTitle;

            // Получение полного текста первой статьи
            String fullTextUrl = "https://ru.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&pageids=" + firstPageId + "&explaintext=true";
            String fullTextResponse = sendGetRequest(fullTextUrl);
            JsonObject fullTextObject = new Gson().fromJson(fullTextResponse, JsonObject.class);
            JsonObject pagesObject = fullTextObject.getAsJsonObject("query").getAsJsonObject("pages");
            JsonElement pageElement = pagesObject.get(firstPageId);
            String fullText = pageElement.getAsJsonObject().get("extract").getAsString();

            System.out.println("Полный текст первой статьи: " + fullText);
        }
    }

    private static String readQueryFromConsole() {
        System.out.print("Введите поисковый запрос: ");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            return reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static String sendGetRequest(String url) {
        try {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
            connection.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}