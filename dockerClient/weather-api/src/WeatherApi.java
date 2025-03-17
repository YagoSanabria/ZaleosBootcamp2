
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import org.json.*;

public class WeatherApi {

    private static final double KELVIN = 273.15;
    private static final int TIMEOUT = 5000;
    private static final String BASE_API_URL = "http://192.168.0.231:8080/"; //Yago ip

    //String apiUrl = "http://192.168.0.231:8080/"; // Usa la IP especial para la máquina host Yago
    //String apiUrl = "http://192.168.0.142:8080/"; // Usa la IP especial para la máquina host Luna
    //String apiUrl = "http://localhost:8080/"; // port 8080
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            String type = getUserInput(scanner, "Current weather (api) or forecast (forecast) [exit to quit]: ");
            if (type.equals("exit")) {
                break;
            } else if (!type.equals("api") && !type.equals("forecast")) {
                System.out.println("Invalid type. Try again.");
                continue;
            }

            String method = getUserInput(scanner, "Insert new weather (upload), view the weather (view) or delete a forecast (delete) [exit to quit]: ");
            if (method.equals("exit")) {
                break;
            }

            String apiUrl = BASE_API_URL + type + "/";
            System.out.println("New URL: " + apiUrl);

            switch (method) {
                case "delete":
                    handleDelete(scanner, apiUrl, type);
                    break;
                case "upload":
                    handleUpload(scanner, apiUrl);
                    break;
                case "view":
                    handleView(scanner, apiUrl, type);
                    break;
                default:
                    System.out.println("Invalid method. Try again.");
            }
        }
        scanner.close();
        System.out.println("Exiting program...");
    }

    private static String getUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim().toLowerCase();
    }

    private static void handleDelete(Scanner scanner, String apiUrl, String type) {
        String fileName = getUserInput(scanner, "Insert the file name [exit to quit]: ");
        if (fileName.equals("exit")) {
            return;
        }

        String truePath = "db/" + (type.equals("api") ? "" : "forecast/") + fileName + ".json";
        System.out.println("Deleting: " + truePath);
        try {
            URL url = new URL(apiUrl + "delete?filePath=" + URLEncoder.encode(truePath, StandardCharsets.UTF_8));
            sendRequest(url, "DELETE");
        } catch (Exception e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private static void handleUpload(Scanner scanner, String apiUrl) {
        String fileName = getUserInput(scanner, "Insert the file name [exit to quit]: ");
        if (fileName.equals("exit")) {
            return;
        }

        String filePath = "workspace/" + fileName + ".json";
        System.out.println("Uploading: " + filePath);
        try {
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            sendPostRequest(apiUrl, jsonData);
        } catch (IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
        }
    }

    private static void handleView(Scanner scanner, String apiUrl, String type) {
        String city = getUserInput(scanner, "Insert city or country name [exit to quit]: ");
        if (city.equals("exit")) {
            return;
        }

        try {
            URL url = new URL(apiUrl + city);
            String response = sendRequest(url, "GET");
            if (response != null) {
                parseJsonResponse(response, type);
            }
        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }
    }

    private static String sendRequest(URL url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        int status = connection.getResponseCode();
        if (status != 200) {
            System.out.println("Error: Response code " + status);
            return null;
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line);
        }
        in.close();
        return response.toString();
    }

    private static void sendPostRequest(String apiUrl, String jsonData) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        try (OutputStream os = connection.getOutputStream()) {
            os.write(jsonData.getBytes());
            os.flush();
        }

        int status = connection.getResponseCode();
        if (status == 200) {
            System.out.println("Upload successful.");
        } else {
            System.out.println("Error: Response code " + status);
        }
    }

    private static void parseJsonResponse(String response, String type) {
        JSONObject jsonResponse = new JSONObject(response);
        if (type.equals("api")) {
            parseCurrentWeather(jsonResponse);
        } else {
            parseWeatherForecast(jsonResponse);
        }
    }

    private static void parseCurrentWeather(JSONObject jsonResponse) {
        String cityName = jsonResponse.getString("name");
        String country = jsonResponse.getJSONObject("sys").getString("country");
        double temperature = jsonResponse.getJSONObject("main").getDouble("temp") - KELVIN;
        double tempLike = jsonResponse.getJSONObject("main").getDouble("feels_like") - KELVIN;
        double humidity = jsonResponse.getJSONObject("main").getDouble("humidity");
        String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main") + ", " + jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");
        double coordX = jsonResponse.getJSONObject("coord").getDouble("lat");
        double coordY = jsonResponse.getJSONObject("coord").getDouble("lon");
        String coordXtxt = (coordX < 0) ? Math.abs(coordX) + "S" : coordX + "N";
        String coordYtxt = (coordY < 0) ? Math.abs(coordY) + "W" : coordY + "E";

        double windSpeed = jsonResponse.getJSONObject("wind").getDouble("speed");
        int windDeg = jsonResponse.getJSONObject("wind").getInt("deg");

        System.out.println("\nWeather in " + cityName + "(" + country + "), (" + coordXtxt + "," + coordYtxt + "):");
        System.out.println("\tDescription: " + weatherDescription);
        System.out.println("\tTemperature: " + String.format("%.2f", temperature) + "°C, feels like " + String.format("%.2f", tempLike) + "°C");
        System.out.println("\tHumidity: " + humidity + "%");

        String windDirection;
        if (windDeg >= 337.5 || windDeg < 22.5) {
            windDirection = "N";
        } else if (windDeg >= 22.5 && windDeg < 67.5) {
            windDirection = "NE";
        } else if (windDeg >= 67.5 && windDeg < 112.5) {
            windDirection = "E";
        } else if (windDeg >= 112.5 && windDeg < 157.5) {
            windDirection = "SE";
        } else if (windDeg >= 157.5 && windDeg < 202.5) {
            windDirection = "S";
        } else if (windDeg >= 202.5 && windDeg < 247.5) {
            windDirection = "SW";
        } else if (windDeg >= 247.5 && windDeg < 292.5) {
            windDirection = "W";
        } else {
            windDirection = "NW";
        }

        System.out.println("\tWind speed: " + windSpeed + " m/s" + "(" + windDirection + ")");

    }

    private static void parseWeatherForecast(JSONObject jsonResponse) {
        String cityName = jsonResponse.getJSONObject("city").getString("name");
        String country = jsonResponse.getJSONObject("city").getString("country");
        System.out.println("\nWeather forecast in " + cityName + "(" + country + "):");
        JSONArray list = jsonResponse.getJSONArray("list");

        for (int i = 1; i < list.length(); i += 2) {
            JSONObject day = list.getJSONObject(i);
            String date = day.getString("dt_txt");
            double temperature = day.getJSONObject("main").getDouble("temp") - KELVIN;
            double tempLike = day.getJSONObject("main").getDouble("feels_like") - KELVIN;
            double humidity = day.getJSONObject("main").getDouble("humidity");
            String weatherDescription = day.getJSONArray("weather").getJSONObject(0).getString("main") + ", " + day.getJSONArray("weather").getJSONObject(0).getString("description");
            double windSpeed = day.getJSONObject("wind").getDouble("speed");
            int windDeg = day.getJSONObject("wind").getInt("deg");

            if (date.endsWith("00:00:00")) {
                System.out.println("\n");
            }

            System.out.println("\t" + date + ":");
            System.out.println("\t\tDescription: " + weatherDescription);
            System.out.println("\t\tTemperature: " + String.format("%.2f", temperature) + "°C, feels like " + String.format("%.2f", tempLike) + "°C");
            System.out.println("\t\tHumidity: " + humidity + "%");

            String windDirection;
            if (windDeg >= 337.5 || windDeg < 22.5) {
                windDirection = "N";
            } else if (windDeg >= 22.5 && windDeg < 67.5) {
                windDirection = "NE";
            } else if (windDeg >= 67.5 && windDeg < 112.5) {
                windDirection = "E";
            } else if (windDeg >= 112.5 && windDeg < 157.5) {
                windDirection = "SE";
            } else if (windDeg >= 157.5 && windDeg < 202.5) {
                windDirection = "S";
            } else if (windDeg >= 202.5 && windDeg < 247.5) {
                windDirection = "SW";
            } else if (windDeg >= 247.5 && windDeg < 292.5) {
                windDirection = "W";
            } else {
                windDirection = "NW";
            }

            System.out.println("\t\tWind speed: " + windSpeed + " m/s" + "(" + windDirection + ")");

        }
    }
}
