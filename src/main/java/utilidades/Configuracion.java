package utilidades;

import java.io.FileInputStream;
import java.util.Properties;

public class Configuracion {

    private static final Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("configuracion.properties"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String get(String key) {
        return properties.getProperty(key);
    }
}