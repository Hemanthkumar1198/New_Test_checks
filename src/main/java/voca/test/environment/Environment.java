package voca.test.environment;

//public class Environment {
    
    
	import java.util.stream.Stream;
	import static org.apache.commons.lang3.StringUtils.isEmpty;

	/**
	 * Manages Environment related configurations and access.
	 *
	 * To be able to run a test against any environment, it should be included in this enum.
	 * Also there should exist configurations and test data specific for that environment.
	 *
	 * When running the test, to target a specific environment pass the 'env' System Property for the executor to pick up.
	 * e.g. env=ft
	 */
	public enum Environments { // 16 usages Deepak Jagga +3
	    FT("ft"),
	    LOCAL("local"),
	    CT_0193("ct-0193"),
	    CT_0239("ct-0239"),
	    UT("ut"),
	    SIT("sit"),
	    FTR("ftr"),
	    PT("pt"),
	    TT("tt"),
	    CT_12("ct-12"),
	    AUT("aut");

	    public static final String ENVIRONMENT_CONFIG_NAME = "env"; // 4 usages

	    private final String name; // 2 usages

	    Environments(String name) { this.name = name; }

	    public static Environments getConfiguredOrDefault() { // 1 usage Deepak Jagga
	        String confEnv = System.getProperty(ENVIRONMENT_CONFIG_NAME);
	        if (isEmpty(confEnv)) {
	            return Environments.CT_0239;
	        }
	        return withName(confEnv);
	    }

	    public static String getConfiguredOrDefaultAsString() { 
	        return getConfiguredOrDefault().getName(); 
	    }

	    public static Environments withName(String confEnv) { // 1 usage Deepak Jagga
	        return Stream.of(values())
	                .filter(e -> e.getName().equalsIgnoreCase(confEnv))
	                .findFirst()
	                .orElseThrow(() -> new IllegalArgumentException("No Environment found with name: " + confEnv));
	    }

	    public String getName() { return name; }
	}

}
