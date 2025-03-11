
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;


public class WeatherApi {

    public static void main(String[] args) {

        final double KELVIN = 273.15;

        String apiUrl = "http://localhost:8080/api"; //port 8080
        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("\nInsert city or country name (exit to quit): ");
            String city = scanner.nextLine();
            String direccion = "";

            if (city.equals("exit")) {
                break;
            } else if (city.equals("")) {
                continue;
            }else{
                direccion = apiUrl+city.replaceAll(" ", "");
            }

            try {

                URL url = new URL(direccion);

                //Open http connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);  //Connection timeout of 5 seconds
                connection.setReadTimeout(5000);  //Read timeout of 5 seconds

                //Get http response code
                int status = connection.getResponseCode();

                //If response code is 200, read the response (all ok)
                if (status == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //Parse response to json
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    ///Get data from json
                    String cityName = jsonResponse.getString("name");
                    String country = jsonResponse.getJSONObject("sys").getString("country");
                    double temperature = jsonResponse.getJSONObject("main").getDouble("temp") - KELVIN;

                    double tempLike = jsonResponse.getJSONObject("main").getDouble("feels_like")- KELVIN;

                    double humidity = jsonResponse.getJSONObject("main").getDouble("humidity");
                    String weatherDescription = jsonResponse.getJSONArray("weather").getJSONObject(0).getString("main") + ", " + jsonResponse.getJSONArray("weather").getJSONObject(0).getString("description");

                    double coordX = jsonResponse.getJSONObject("coord").getDouble("lat");
                    double coordY = jsonResponse.getJSONObject("coord").getDouble("lon");

                    String coordXtxt = (coordX < 0) ? Math.abs(coordX) + "S" : coordX + "N";
                    String coordYtxt = (coordY < 0) ? Math.abs(coordY) + "W" : coordY + "E";

                    //Print data in terminal
                    System.out.println("\nWeather in " + cityName + "(" + country + "), " + "(" + coordXtxt + "," + coordYtxt + "):");
                    System.out.println("\tDescription: " + weatherDescription);
                    System.out.println("\tTemperature: " + (String.format("%.2f", temperature)) + "°C, feels like " + (String.format("%.2f", tempLike)) + "°C");
                    System.out.println("\tHumidity: " + humidity + "%");


                } else {
                    System.out.println("Error in request. Response code: " + status);

                    //Get error message
                }

                //Close connection
                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //exit
        scanner.close();
        System.out.println("Exiting program...");
    }
}
