
import java.io.*;
import java.net.*;
import java.util.Scanner;
import org.json.*;

public class WeatherApi {

    public static void main(String[] args) {
        final double KELVIN = 273.15;
        Scanner scanner = new Scanner(System.in);

        while (true) {

            String apiUrl = "http://192.168.0.231:8080/"; // Usa la IP especial para la máquina host
            //String apiUrl = "http://localhost:8080/"; // port 8080

            System.out.print("\nCurrent weather (api) or forecast (forecast) [exit to quit]: ");
            String type = scanner.nextLine().replaceAll(" ", "").toLowerCase();
            if (type.equals("")) {
                continue;
            } else if (type.equals("exit")) {
                break;
            } else if (!type.equals("api") && !type.equals("forecast")) {
                System.out.println("Invalid type. Try again.");
                continue;
            } else {
                apiUrl += type + "/";
            }

            System.out.println("New uri: " + apiUrl);
            System.out.print("\nInsert city or country name [exit to quit]: ");
            String city = scanner.nextLine().replaceAll(" ", "").toLowerCase();
            if (city.equals("exit")) {
                break;
            } else if (city.equals("") || !city.matches("[a-z]+")) {
                System.out.println("Invalid city or country name. Try again.");
                continue;
            }

            String direccion = apiUrl + city;
            try {
                URL url = new URL(direccion);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int status = connection.getResponseCode();

                if (status == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (type.equals("api")) {
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

                        System.out.println("\nWeather in " + cityName + "(" + country + "), (" + coordXtxt + "," + coordYtxt + "):");
                        System.out.println("\tDescription: " + weatherDescription);
                        System.out.println("\tTemperature: " + String.format("%.2f", temperature) + "°C, feels like " + String.format("%.2f", tempLike) + "°C");
                        System.out.println("\tHumidity: " + humidity + "%");
                    } else if (type.equals("forecast")) {

                        try {
                            // Specify the path to your bash script
                            String scriptPath = "web.sh";

                            //System.out.println(response.toString());
                            // Use ProcessBuilder to execute the script
                            ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", scriptPath, response.toString());

                            // Start the process
                            Process process = processBuilder.start();

                            // Capturar la salida de error
                            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            String line;
                            while ((line = errorReader.readLine()) != null) {
                                System.out.println("Error: " + line);
                            }

                            // Wait for the process to complete
                            int exitCode = process.waitFor();
                            System.out.println("Script executed with exit code: " + exitCode);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                } else {
                    System.out.println("Error in request. Response code: " + status);

                    //Get error message
                    String a = connection.getResponseMessage();

                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String msg = jsonResponse.getString("Error message");
                    System.out.println("\tError message: " + msg);
                }

                //Close connection
                connection.disconnect();

            } catch (Exception e) {
                System.out.println("Error. Response code: 503");
                System.out.println("Trying to request to server");
            }
        }
        scanner.close();
        System.out.println("Exiting program...");
    }
}
