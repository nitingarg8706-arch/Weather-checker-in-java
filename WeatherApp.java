import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.json.JSONArray;
import org.json.JSONObject;

public class WeatherApp extends JFrame {

    private JTextField placeField;
    private JLabel tempLabel, feelsLabel, humidityLabel, pressureLabel,
            windLabel, sunriseLabel, sunsetLabel, conditionLabel;

    // 🔑 PUT YOUR OWN API KEY HERE
    private final String API_KEY = "d29f5be63ab34d966d1a551a26e0d9e4";

    public WeatherApp() {
        setTitle("🌍 Global Weather Checker ");
        setSize(850, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(30, 30, 60));

        JLabel title = new JLabel("Global Weather Checker", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        placeField = new JTextField();
        placeField.setFont(new Font("Arial", Font.PLAIN, 18));

        JButton button = new JButton("Get Weather");
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(0, 153, 255));
        button.setForeground(Color.WHITE);

        JPanel top = new JPanel(new GridLayout(3, 1, 8, 8));
        top.setBackground(new Color(20, 20, 50));
        top.add(title);
        top.add(placeField);
        top.add(button);

        tempLabel = label("Temperature: -- °C");
        feelsLabel = label("Feels Like: -- °C");
        conditionLabel = label("Condition: --");
        humidityLabel = label("Humidity: -- %");
        pressureLabel = label("Pressure: -- hPa");
        windLabel = label("Wind Speed: -- m/s");
        sunriseLabel = label("Sunrise: --");
        sunsetLabel = label("Sunset: --");

        JPanel center = new JPanel(new GridLayout(8, 1, 5, 5));
        center.setBackground(new Color(30, 30, 60));
        center.add(tempLabel);
        center.add(feelsLabel);
        center.add(conditionLabel);
        center.add(humidityLabel);
        center.add(pressureLabel);
        center.add(windLabel);
        center.add(sunriseLabel);
        center.add(sunsetLabel);

        main.add(top, BorderLayout.NORTH);
        main.add(center, BorderLayout.CENTER);
        add(main);

        button.addActionListener(e -> fetchWeather());

        setVisible(true);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Arial", Font.PLAIN, 18));
        l.setForeground(Color.WHITE);
        return l;
    }

    // 🌍 STEP 1: Convert village/city to latitude & longitude
    private double[] getCoordinates(String place) throws Exception {
        String geoUrl =
                "https://api.openweathermap.org/geo/1.0/direct?q="
                        + URLEncoder.encode(place, "UTF-8")
                        + "&limit=1&appid=" + API_KEY;

        URL url = new URL(geoUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader br = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line);
        br.close();

        JSONArray arr = new JSONArray(sb.toString());

        if (arr.length() == 0) {
            throw new Exception("Place not found");
        }

        double lat = arr.getJSONObject(0).getDouble("lat");
        double lon = arr.getJSONObject(0).getDouble("lon");

        return new double[]{lat, lon};
    }

    // 🌦️ STEP 2: Fetch weather using lat & lon
    private void fetchWeather() {
        String place = placeField.getText().trim();
        if (place.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter village or city name");
            return;
        }

        try {
            double[] coord = getCoordinates(place);

            String weatherUrl =
                    "https://api.openweathermap.org/data/2.5/weather?"
                            + "lat=" + coord[0]
                            + "&lon=" + coord[1]
                            + "&appid=" + API_KEY
                            + "&units=metric";

            URL url = new URL(weatherUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            br.close();

            JSONObject obj = new JSONObject(sb.toString());

            JSONObject main = obj.getJSONObject("main");
            JSONObject wind = obj.getJSONObject("wind");
            JSONObject sys = obj.getJSONObject("sys");

            double temp = main.getDouble("temp");
            double feels = main.getDouble("feels_like");
            int humidity = main.getInt("humidity");
            int pressure = main.getInt("pressure");
            double windSpeed = wind.getDouble("speed");
            String condition = obj.getJSONArray("weather")
                                  .getJSONObject(0)
                                  .getString("main");

            long sunrise = sys.getLong("sunrise") * 1000;
            long sunset = sys.getLong("sunset") * 1000;

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");

            tempLabel.setText("Temperature: " + temp + " °C");
            feelsLabel.setText("Feels Like: " + feels + " °C");
            conditionLabel.setText("Condition: " + condition);
            humidityLabel.setText("Humidity: " + humidity + " %");
            pressureLabel.setText("Pressure: " + pressure + " hPa");
            windLabel.setText("Wind Speed: " + windSpeed + " m/s");
            sunriseLabel.setText("Sunrise: " + sdf.format(new Date(sunrise)));
            sunsetLabel.setText("Sunset: " + sdf.format(new Date(sunset)));

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Unable to fetch weather.\nCheck place name or internet.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WeatherApp::new);
    }
}