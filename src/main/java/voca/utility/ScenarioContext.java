package voca.utility;

    import com.google.common.collect.ListMultimap;
	import com.google.common.collect.MultimapBuilder;
	import org.springframework.stereotype.Component;
	import java.util.HashMap;

	@Component
	public class ScenarioContext {

	    private HashMap<String, Object> ctx = new HashMap<>();
	    ListMultimap<String, Object> obj = MultimapBuilder.hashKeys().arrayListValues().build();

	    public Object get(String key) { return ctx.get(key); }

	    public Object getval(String key) { return obj.get(key); }

	    public void set(String key, Object value) { ctx.put(key, value); }

	    public void addVal(String key, Object value) { obj.put(key, value); }

	    public void removeValues(String mykey) { obj.removeAll(mykey); }

	    public boolean containsValue(String key) { return ctx.containsKey(key); }

	    public void flush(){
	        ctx.clear();
	        obj.clear();
	    }
    
}
