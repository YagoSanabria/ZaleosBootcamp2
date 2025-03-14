
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

            System.out.println("New uri: " + direccion);

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

                    System.out.println("Response code: " + status);

                    //System.out.println("Response: " + jsonResponse.toString());
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
                    } else if (type.equals("forecast")) {

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
                System.out.println("Error. Algo ha petado");
                System.out.println("Error message: " + e.getMessage());

            }
        }
        scanner.close();
        System.out.println("Exiting program...");
    }
}
