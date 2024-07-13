
package example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static String sourceCurrency;
    private static String targetCurrency;
    private static final List<String> conversionHistory = new ArrayList<>();
    private static final Map<String, String> currencyCodes = new HashMap<>();

    static {
        currencyCodes.put("Peso Mexicano (MXN)", "MXN");
        currencyCodes.put("Peso Argentino (ARS)", "ARS");
        currencyCodes.put("Peso Colombiano (COP)", "COP");
        currencyCodes.put("Dólar (USD)", "USD");
        currencyCodes.put("Euro (EUR)", "EUR");
        currencyCodes.put("Libra Esterlina (GBP)", "GBP");
    }

    public static void main(String[] args) {
        while (true) {
            int option = showMainMenu();

            switch (option) {
                case 0:
                    convertCurrency();
                    break;
                case 1:
                    viewConversionHistory();
                    break;
                case 2:
                    exitApplication();
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Opción inválida. Por favor, seleccione una opción válida.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static int showMainMenu() {
        String[] options = {"Convertir una moneda", "Consultar el historial", "Salir del sistema"};
        return JOptionPane.showOptionDialog(null, "Seleccione una opción:", "Menú principal",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);
    }

    private static void convertCurrency() {
        String[] currencies = currencyCodes.keySet().toArray(new String[0]);

        if (sourceCurrency == null || targetCurrency == null) {
            sourceCurrency = showCurrencySelectionDialog("Seleccione la moneda de origen:", currencies);
            targetCurrency = showCurrencySelectionDialog("Seleccione la moneda de destino:", currencies);
        }

        String amountStr = showInputDialogWithColor("Ingrese la cantidad a convertir:", "blue");
        if (amountStr != null && !amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                double conversionRate = getConversionRate(sourceCurrency, targetCurrency);
                double result = amount * conversionRate;
                String message = String.format("<html><font color='green'>Resultado de la conversión: %.2f %s = %.2f %s</font></html>", amount, sourceCurrency, result, targetCurrency);
                JOptionPane.showMessageDialog(null, message, "Resultado", JOptionPane.INFORMATION_MESSAGE);
                conversionHistory.add(String.format("Convertido %.2f %s a %.2f %s", amount, sourceCurrency, result, targetCurrency));

                int choice = JOptionPane.showConfirmDialog(null, String.format("<html><font color='blue'>¿Desea convertir otra cantidad de %s a %s?</font></html>", sourceCurrency, targetCurrency), "Convertir otra cantidad", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    convertCurrency();
                } else {
                    sourceCurrency = null;
                    targetCurrency = null;
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "<html><font color='red'>Por favor ingrese un número válido.</font></html>", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "<html><font color='red'>Error al realizar la conversión: " + e.getMessage() + "</font></html>", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static String showCurrencySelectionDialog(String message, String[] currencies) {
        return (String) JOptionPane.showInputDialog(null, String.format("<html><font color='blue'>%s</font></html>", message),
                "Convertir moneda",
                JOptionPane.PLAIN_MESSAGE, null, currencies, currencies[0]);
    }

    private static String showInputDialogWithColor(String message, String color) {
        return JOptionPane.showInputDialog(String.format("<html><font color='%s'>%s</font></html>", color, message));
    }

    private static void viewConversionHistory() {
        StringBuilder historyMessage = new StringBuilder("<html><font color='blue'>----- HISTORIAL DE CONVERSIONES -----<br>");
        if (conversionHistory.isEmpty()) {
            historyMessage.append("El historial está vacío.</font></html>");
        } else {
            for (String entry : conversionHistory) {
                historyMessage.append(entry).append("<br>");
            }
        }
        historyMessage.append("</font><br>--------------------------------------</html>");
        JOptionPane.showMessageDialog(null, historyMessage.toString(), "Historial de Conversiones", JOptionPane.PLAIN_MESSAGE);
    }

    private static void exitApplication() {
        JOptionPane.showMessageDialog(null, "¡Adiós!", "Saliendo", JOptionPane.INFORMATION_MESSAGE);
        System.exit(0);
    }

    private static double getConversionRate(String baseCurrency, String targetCurrency) {
        try {
            String url = "https://v6.exchangerate-api.com/v6/1037ae973d901a6844f1f32b/latest/" + currencyCodes.get(baseCurrency);
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                reader.close();

                JsonObject conversionRates = jsonObject.getAsJsonObject("conversion_rates");
                if (conversionRates != null && conversionRates.has(currencyCodes.get(targetCurrency))) {
                    return conversionRates.get(currencyCodes.get(targetCurrency)).getAsDouble();
                } else {
                    throw new Exception("La tasa de conversión para " + targetCurrency + " no está disponible en el JSON recibido");
                }
            } else {
                throw new Exception("Error al obtener la tasa de conversión. Código de respuesta: " + responseCode);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la tasa de conversión: " + e.getMessage(), e);
        }
    }
}
