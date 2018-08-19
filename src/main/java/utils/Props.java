package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Props {
    private static final Logger LOG = LoggerFactory.getLogger(Props.class);
    private static Props instance;
    private static Properties properties;

    private Props() {
        initProperties();
    }

    private static void initProperties() {
        String sConfigFile = System.getProperty("ConfigFile", "config.properties");
        properties = new Properties();
        LOG.debug("Loading properties from {}", sConfigFile);

        try {
            InputStream streamFromResources = Props.class.getClassLoader().getResourceAsStream(sConfigFile);
            Throwable var2 = null;

            try {
                InputStreamReader isr = new InputStreamReader(streamFromResources, "UTF-8");
                properties.load(isr);
            } catch (Throwable var12) {
                var2 = var12;
                throw var12;
            } finally {
                if (streamFromResources != null) {
                    if (var2 != null) {
                        try {
                            streamFromResources.close();
                        } catch (Throwable var11) {
                            var2.addSuppressed(var11);
                        }
                    } else {
                        streamFromResources.close();
                    }
                }

            }

        } catch (NullPointerException | IOException var14) {
            throw new RuntimeException("Failed to access properties file", var14);
        }
    }

    private static synchronized Props getInstance() {
        if (instance == null) {
            instance = new Props();
        }

        return instance;
    }

    private String getProp(String name) {
        String val = getProps().getProperty(name, "");
        if (val.isEmpty()) {
            LOG.debug("Property {} was not found in properties file", name);
        }

        return val.trim();
    }

    public static Properties getProps() {
        initProperties();
        return properties;
    }

    public static String get(String prop) {
        return getInstance().getProp(prop);
    }

    public static String get(String prop, String defaultValue) {
        String value = getInstance().getProp(prop);
        return value.isEmpty() ? defaultValue : value;
    }
}