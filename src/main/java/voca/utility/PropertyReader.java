package voca.utility;

import org.apache.logging.log4j.LogManager;
		import org.apache.logging.log4j.Logger;

		import java.io.FileReader;
		import java.io.IOException;
		import java.util.Properties;


public class PropertyReader {
    

		    private static final Logger LOGGER = LogManager.getLogger(PropertyReader.class);

		    Properties loadConfigProperty;
		    Properties loadEnvProperty;

		    private static final String FILE_PATH = "src/main/resources/environmentConfig/";
		    private static final String CONFIG_READER = "src/main/resources/config/Config.properties";
		    private static final String DEFAULT_ENV_READER = "src/main/resources/environmentConfig/sit.properties";

		    private FileReader fileReader;
		    private String environment;
		    private String autoLoginCheckValue;
		    private boolean useSoftCertificates;

		    public Properties loadConfigProperty() {
		        try {
		            loadConfigProperty = new Properties();
		            loadConfigProperty.load(readFile(CONFIG_READER));
		        }
		        catch (Exception e) {
		            e.printStackTrace();
		        }
		        return loadConfigProperty;
		    }

	    public String getEnv() {
	        environment = getProperty("env");
	        return environment;
	    }

	    public boolean useSoftCertificates() {
	        useSoftCertificates = Boolean.parseBoolean(getProperty("useSoftCertificates"));
	        return useSoftCertificates;
	    }

	    public String getAutoLoginCheckValue() {
	        autoLoginCheckValue = getProperty("autoLoginCheck");
	        return autoLoginCheckValue;
	    }

	    private String getProperty(String propertyName) {
	        loadConfigProperty = loadConfigProperty();
	        loadEnvProperty = new Properties();
	        return (System.getProperty(propertyName) != null) ? System.getProperty(propertyName).toLowerCase() : ...
	    }

	    public Properties loadEnvProperty() {
	        FileReader defaultReader = null;
	        try {
	            switch (getEnv()) {
	                case "sit":
	                    loadEnvProperty.load(readFile(FILE_PATH + "sit.properties"));
	                    break;
	                case "ct-0239":
	                    loadEnvProperty.load(readFile(FILE_PATH + "ct-0239.properties"));
	                    break;
	                case "ct-12":
	                    loadEnvProperty.load(readFile(FILE_PATH + "ct-12.properties"));
	                    break;
	                default:
	                    defaultReader = new FileReader(FILE_PATH);
	                    loadEnvProperty.load(defaultReader);
	            }
	        }
	        catch (IOException e) {
	            e.printStackTrace();
	        }
	        finally {
	            if (defaultReader != null) {
	                try {
	                    defaultReader.close();
	                }
	                catch (IOException e) {
	                    LOGGER.error("Could not close default property reader");
	                }
	            }
	        }
	        return loadEnvProperty;
	    }

	    public FileReader readFile(String filePath) {
	        try {
	            fileReader = new FileReader(filePath);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return fileReader;
	    }
}
