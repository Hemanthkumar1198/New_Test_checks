package voca.config;

    import java.util.Properties;
	import com.vocalink.bacs.test.environment.Environments;
	import static com.vocalink.bacs.util.FileUtils.readAsInputStream;
	import org.apache.logging.log4j.LogManager;
	import org.apache.logging.log4j.Logger;
	import javax.inject.Singleton;

	@Singleton
	public final class ConfigManager {

	    private static final Logger LOGGER = LogManager.getLogger(ConfigManager.class);
	    private static ConfigManager configManager;
	    private final String testEnv;

	    private Properties atfConfig;
	    private Properties sutConfig;
	    private Properties dbConfig;
	    private Properties etsStsConfig;
	    private Properties userConfig;
	    private Properties certificateKeysConfig;
	    private Properties tivoliConfig;

	    private ConfigManager() {
	        testEnv = Environments.getConfiguredOrDefaultAsString();
	    }

	    public static ConfigManager getInstance() {
	        if (configManager == null) {
	            try {
	                configManager = new ConfigManager();
	            } catch (Exception e) {
	                LOGGER.error(e.getMessage());
	            }
	        }
	        return configManager;
	    }

	    public Properties getAtfConfig() { return getConfig(atfConfig, "atf-config"); }

	    public Properties getSutConfig() { return getConfig(sutConfig, "sut-config"); }

	    public Properties getDbConfig() { return getConfig(dbConfig, "sut-database-connection"); }

	    public Properties getEtsStsConfig() { return getConfig(etsStsConfig, "sut-ets-sts"); }

	    public Properties getUserConfig() { return getConfig(userConfig, "sut-user"); }

	    public Properties getCertificateKeysConfig() { return getConfig(certificateKeysConfig, "certificate-keys"); }

	    public Properties getTivoliConfig() { return getConfig(tivoliConfig, "tivoli"); }

	    private Properties getConfig(Properties props, final String configFileName) {
	        try {
	            if (props == null) {
	                props = new Properties();
	                if (configFileName.equals("certificate-keys")) {
	                    props.load(readAsInputStream(String.format("config/%s.properties", configFileName)));
	                } else {
	                    props.load(readAsInputStream(String.format("config/%s/%s.properties", testEnv, configFileName)));
	                }
	            }
	        } catch (Exception ex) {
	            LOGGER.error(String.format("Exception while loading %s.properties file: %s", configFileName, ex));
	        }
	        return props;
	    }
    
}
