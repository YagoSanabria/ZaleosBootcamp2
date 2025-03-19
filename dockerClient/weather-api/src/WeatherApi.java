
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import org.json.*;

public class WeatherApi {

    private static final double KELVIN = 273.15; // Constant to convert Kelvin to Celsius
    private static final int TIMEOUT = 5000; // Timeout for HTTP connections
    private static final String BASE_API_URL = "http://192.168.0.231:8080/"; // Base URL for the API
    //IP Luna: http://192.168.0.142:8080/
    //IP Yago: http://192.168.0.231:8080/

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        //print how to use and info
        //it is not legible here because we had to scape the characters
        System.out.println("\n"
                + "\n"
                + "╔═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n"
                + "║                                                                                                                       ║\n"
                + "║   █████         █████                     ████     ████                               █████████   ███████████  █████  ║\n"
                + "║  ░░███         ░░███                     ░░███    ░░███                              ███░░░░░███ ░░███░░░░░███░░███   ║\n"
                + "║   ░███   ░███   ░███   ██████   ██████   ███████   ░███████    ██████  ████████     ░███    ░███  ░███    ░███ ░███   ║\n"
                + "║   ░███   ░███   ░███  ███░░███ ░░░░░███ ░░░███░    ░███░░███  ███░░███░░███░░███    ░███████████  ░██████████  ░███   ║\n"
                + "║   ░░███  █████  ███  ░███████   ███████   ░███     ░███ ░███ ░███████  ░███ ░░░     ░███░░░░░███  ░███░░░░░░   ░███   ║\n"
                + "║    ░░░█████░█████░   ░███░░░   ███░░███   ░███ ███ ░███ ░███ ░███░░░   ░███         ░███    ░███  ░███         ░███   ║\n"
                + "║      ░░███ ░░███     ░░██████ ░░████████  ░░█████  ████ █████░░██████  █████        █████   █████ █████        █████  ║\n"
                + "║       ░░░   ░░░       ░░░░░░   ░░░░░░░░    ░░░░░  ░░░░ ░░░░░  ░░░░░░  ░░░░░        ░░░░░   ░░░░░ ░░░░░        ░░░░░   ║\n"
                + "║                                                                                                                       ║\n"
                + "╚═══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n"
                + "\n");

        System.out.println("On every step you can type 'exit' to quit the program.");
        System.out.println("You can upload a JSON file with weather data, view the current weather or forecast for a city, or delete a forecast.");
        System.out.println("The JSON file must be in the 'workspace' folder and have the name of the city or country.");

        while (true) {//program loop

            System.out.println("\nWhat do you want to do?");
            String method = "";
            method = getUserInput(scanner, "\tView weather (view), insert new weather info (upload), or delete (delete): ");
            if (method.equals("exit")) {
                break;
            } else if (!method.equals("upload") && !method.equals("view") && !method.equals("delete")) {
                System.out.println("Invalid type. Try again.");
                continue;
            }

            while (true) {//method loop

                // Get the type of weather data (current, forecast or graph)
                String type = getUserInput(scanner, "\n\tCurrent weather (api), forecast (forecast)" + (method.equals("view") ? ", or graph (graph): " : ": "));
                if (type.equals("exit")) {
                    break;
                } else if (!type.equals("api") && !type.equals("forecast") && !type.equals("graph")) {
                    System.out.println("Invalid type. Try again.");
                    continue;
                }

                String apiUrl = BASE_API_URL + type;

                // Handle the chosen method
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
        }
        scanner.close();
        System.out.println("\nExiting program...");
    }

    // Get user input with a prompt
    private static String getUserInput(Scanner scanner, String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().toLowerCase().replaceAll(" ", "");
    }

    // Handle delete request
    private static void handleDelete(Scanner scanner, String apiUrl, String type) {
        String fileName = getUserInput(scanner, "\n\tInsert the file name: ");
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

    // Handle upload request
    private static void handleUpload(Scanner scanner, String apiUrl) {
        String fileName = getUserInput(scanner, "\n\tInsert the file name: ");
        if (fileName.equals("exit")) {
            return;
        }

        String filePath = "workspace/" + fileName + ".json";
        System.out.println("\nUploading: " + filePath);
        try {
            String jsonData = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
            sendPostRequest(apiUrl, jsonData);
        } catch (IOException e) {
            System.out.println("Error reading JSON file: " + e.getMessage());
        }
    }

    // Handle view request
    private static void handleView(Scanner scanner, String apiUrl, String type) {
        String city = getUserInput(scanner, "\n\tInsert location name: ");
        if (city.equals("exit")) {
            return;
        }

        try {
            URL url = new URL(apiUrl + "?" + city);
            String response = sendRequest(url, "GET");
            if (response != null) {
                parseJsonResponse(response, type);
            }
        } catch (Exception e) {
            System.out.println("Error fetching data: " + e.getMessage());
        }
    }

    // Send HTTP request
    private static String sendRequest(URL url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(TIMEOUT);
        connection.setReadTimeout(TIMEOUT);

        int status = connection.getResponseCode();
        if (status != 200) {
            System.out.println("\nError: Response code " + status);
            System.out.println("Error message: " + connection.getResponseMessage());
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

    // Send HTTP POST request
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
            System.out.println("\nError: Response code " + status);
            System.out.println("Error message: " + connection.getResponseMessage());
        }
    }

    // Parse JSON response
    private static void parseJsonResponse(String response, String type) {
        JSONObject jsonResponse = new JSONObject(response);
        if (type.equals("api")) {
            parseCurrentWeather(jsonResponse);
        } else if (type.equals("forecast")) {
            parseWeatherForecast(jsonResponse);
        } else if (type.equals("graph")) {
            parsePrintWeather(jsonResponse);
        }
    }

    // Parse current weather data
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

        System.out.println("\n══════════════════════════════════════════════════════════════════════");
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
        System.out.println("\n══════════════════════════════════════════════════════════════════════");

    }

    // Parse weather forecast data
    private static void parseWeatherForecast(JSONObject jsonResponse) {
        String cityName = jsonResponse.getJSONObject("city").getString("name");
        String country = jsonResponse.getJSONObject("city").getString("country");

        System.out.println("\n══════════════════════════════════════════════════════════════════════");

        System.out.println("\nWeather forecast in " + cityName + "(" + country + "):");
        JSONArray list = jsonResponse.getJSONArray("list");

        for (int i = 1; i < list.length(); i++) {
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
        System.out.println("\n══════════════════════════════════════════════════════════════════════");
    }

    // Parse weather forecast data and print a graph
    private static void parsePrintWeather(JSONObject jsonResponse) {
        String cityName = jsonResponse.getJSONObject("city").getString("name");
        String country = jsonResponse.getJSONObject("city").getString("country");
        System.out.println("\n══════════════════════════════════════════════════════════════════════");

        System.out.println("\nWeather forecast in " + cityName + "(" + country + "):");
        JSONArray list = jsonResponse.getJSONArray("list");

        double maxTemp = 30;
        double minTemp = -2;
        double[] temperatures = new double[list.length()];
        int[] cloudiness = new int[list.length()];
        String[] dates = new String[list.length()];

        for (int i = 1; i < list.length(); i += 2) {
            JSONObject day = list.getJSONObject(i);
            String date = day.getString("dt_txt");
            double temperature = day.getJSONObject("main").getDouble("temp") - KELVIN;
            int clouds = day.getJSONObject("clouds").getInt("all");

            temperatures[i] = temperature;
            cloudiness[i] = clouds;
            dates[i] = date.substring(5, 10);

            if (temperature > maxTemp) {
                maxTemp = temperature;
            }
            if (temperature < minTemp) {
                minTemp = temperature;
            }

        }

        //Print the graph
        double cloudCount = 100.0;
        System.out.println("Temperature (ºC)    &    Cloudiness Graph (%):\n");
        for (int temp = (int) maxTemp; temp >= minTemp; temp--) {
            System.out.printf("%3d°C |", temp);
            for (int i = 1; i < list.length(); i += 2) {
                if (temp == 0) { //line 0
                    if ((int) temperatures[i] == temp) {
                        System.out.print("_\033[91m*\033[0m_");
                    } else if (cloudiness[i] >= 0 && (cloudiness[i] * 10 / maxTemp) > temp) {
                        System.out.print("_█_");
                    } else {
                        System.out.print("___");
                    }
                } else {
                    if ((int) temperatures[i] == temp) {
                        System.out.print(" \033[91m*\033[0m ");
                    } else if (cloudiness[i] >= 0 && temp >= 0 && (cloudiness[i] * 10 / maxTemp) > temp) {
                        System.out.print(" █ ");
                    } else {
                        System.out.print("   ");
                    }
                }
            }
            if (temp >= 0) {
                System.out.printf("| %5.2f", cloudCount);
                System.out.println(" %");
                cloudCount -= 100 / maxTemp;
            } else {
                System.out.println("|");
            }
        }
        //Print dates (2 columns per day)
        System.out.print("      ");
        for (int i = 1; i < list.length(); i += 4) {
            System.out.printf("|%s", dates[i]);
        }
        System.out.println(" |");
        System.out.println("\n══════════════════════════════════════════════════════════════════════");
    }
}
