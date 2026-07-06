package voca.model.email;

import lombok.Getter;
import Lombok.Setter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Setter
@Getter
public class EmailMessage {

    private Map<String, List<String>> headers = new HashMap<>();

    private String body;

    public void addHeader (String key, String value) {  
        headers.computeIfAbsent(key.trim(), String k->new ArrayList<>()).add(value.trim());
    }

    public List<String> getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String toString() {
        return "Email Message{" + "headers=" + headers + ", body= '" + body + '\'' + '}';
    }

}
