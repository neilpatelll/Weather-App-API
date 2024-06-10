import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class WeatherApp {
    public static JSONObject getWeatherData(String locName) {
        JSONArray locData = getLocationData(locName);
        JSONObject loc = (JSONObject) locData.get(0);
        double lat = (double) loc.get("latitude");
        double lon = (double) loc.get("longitude");
        String apiUrl = String.format("https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,weathercode&timezone=America%%2FLos_Angeles", lat, lon);

        try {
            HttpURLConnection conn = fetchApiResponse(apiUrl);
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while (scanner.hasNext()) {
                resultJson.append(scanner.nextLine());
            }
            scanner.close();
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

            JSONObject hourly = (JSONObject) resultJsonObj.get("hourly");
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);
            JSONArray tempData = (JSONArray) hourly.get("temperature_2m");
            double temp = (double) tempData.get(index);
            JSONArray weatherCode = (JSONArray) hourly.get("weathercode");
            String weatherCond = convertWeatherCode((long) weatherCode.get(index));

            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temp);
            weatherData.put("weather_condition", weatherCond);
            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONArray getLocationData(String locName) {
        locName = locName.replaceAll(" ", "+");
        String apiUrl = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locName + "&count=10&language=en&format=json";

        try {
            HttpURLConnection conn = fetchApiResponse(apiUrl);
            if (conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            } else {
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while (scanner.hasNext()) {
                    resultJson.append(scanner.nextLine());
                }
                scanner.close();
                conn.disconnect();
                JSONParser parser = new JSONParser();
                JSONObject resultsJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));
                JSONArray locData = (JSONArray) resultsJsonObj.get("results");
                return locData;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HttpURLConnection fetchApiResponse(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currTime = getCurrentTime();
        for (int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currTime)) {
                return i;
            }
        }
        return 0;
    }

    private static String getCurrentTime() {
        LocalDateTime currDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");
        String formattedDateTime = currDateTime.format(formatter);
        return formattedDateTime;
    }

    private static String convertWeatherCode(long weathercode) {
        String weatherCond = "";
        if (weathercode == 0L) {
            weatherCond = "Clear";
        } else if (weathercode > 0L && weathercode <= 3L) {
            weatherCond = "Cloudy";
        } else if ((weathercode >= 51L && weathercode <= 67L)
                || (weathercode >= 80L && weathercode <= 99L)) {
            weatherCond = "Rain";
        } else if (weathercode >= 71L && weathercode <= 77L) {
            weatherCond = "Snow";
        }
        return weatherCond;
    }
}
