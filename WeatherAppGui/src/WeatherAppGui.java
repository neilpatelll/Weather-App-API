import org.json.simple.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WeatherAppGui extends JFrame {
    private JSONObject weatherData;

    public WeatherAppGui() {
        super("Weather App");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(450, 650);
        setLocationRelativeTo(null);
        setLayout(null);
        setResizable(false);
        addComponents();
    }

    private void addComponents() {
        JTextField locationField = new JTextField();
        locationField.setBounds(15, 15, 351, 45);
        locationField.setFont(new Font("Dialog", Font.PLAIN, 24));
        add(locationField);

        JLabel weatherImageLabel = new JLabel(loadImage("src/assets/cloudy.png"));
        weatherImageLabel.setBounds(0, 125, 450, 217);
        add(weatherImageLabel);

        JLabel tempLabel = new JLabel("70 F");
        tempLabel.setBounds(0, 350, 450, 54);
        tempLabel.setFont(new Font("Dialog", Font.BOLD, 48));
        tempLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(tempLabel);

        JLabel conditionLabel = new JLabel("Cloudy");
        conditionLabel.setBounds(0, 405, 450, 36);
        conditionLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        conditionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(conditionLabel);

        JButton searchBtn = new JButton(loadImage("src/assets/search.png"));
        searchBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        searchBtn.setBounds(375, 13, 47, 45);
        searchBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userInput = locationField.getText();
                if (userInput.replaceAll("\\s", "").length() <= 0) {
                    return;
                }

                weatherData = WeatherApp.getWeatherData(userInput);

                String weatherCondition = (String) weatherData.get("weather_condition");

                double tempCelsius = (double) weatherData.get("temperature");
                double tempFahrenheit = convertCelsiusToFahrenheit(tempCelsius);

                long roundedTemp = Math.round(tempFahrenheit);

                tempLabel.setText(roundedTemp + " F");

                conditionLabel.setText(weatherCondition);
            }
        });
        add(searchBtn);
    }

    private double convertCelsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private ImageIcon loadImage(String resourcePath) {
        try {
            BufferedImage image = ImageIO.read(new File(resourcePath));
            return new ImageIcon(image);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Could not find resource");
        return null;
    }
}
