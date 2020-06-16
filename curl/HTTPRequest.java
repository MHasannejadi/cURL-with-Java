import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * manage a http request
 */

public class HTTPRequest implements Serializable{
    
    private String name;
    private String method;
    private URL url;
    private HashMap<String,String> headers;
    private HashMap<String,String> body;

    public HTTPRequest(){
        body = new HashMap<>();
        headers = new HashMap<>();
    }

    /**
     * @return the headers
     */
    public HashMap<String, String> getHeaders() {
        return headers;
    }

    /**
     * @return the body
     */
    public HashMap<String, String> getBody() {
        return body;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
    
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * add a header to map of headers
     * @param name
     * @param value
     */
    public void addHeader(String name , String value){
        headers.put(name, value);
    }
    /**
     * add a pair to map of body
     * @param name
     * @param value
     */
    public void addBody(String name , String value){
        body.put(name, value);
    }

    public String headersString() {
        StringBuilder stringBuilder = new StringBuilder();
     
        for (String key : headers.keySet()) {
         if (stringBuilder.length() > 0) {
          stringBuilder.append(";");
         }
         String value = headers.get(key);
         try {
          stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
          stringBuilder.append(":");
          stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
         } catch (UnsupportedEncodingException e) {
          throw new RuntimeException("This method requires UTF-8 encoding support", e);
         }
        }
     
        return stringBuilder.toString();
    }

    public String bodyString() {
        StringBuilder stringBuilder = new StringBuilder();
     
        for (String key : body.keySet()) {
            if (stringBuilder.length() > 0) {
            stringBuilder.append("&");
            }
            String value = body.get(key);
            try {
            stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
            stringBuilder.append("=");
            stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }
     
        return stringBuilder.toString();
    }
}