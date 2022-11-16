package utils;

import java.util.HashMap;
import java.util.NoSuchElementException;

import net.sf.json.JSONObject;

/**
 *
 * @author markskroba
 */
public class JsonHelper {
    public static JSONObject buildNoneLanguageMap(String label)
    {
        JSONObject languageMap = new JSONObject();
        String[] noneMap = new String[] {label};
        languageMap.put("none", noneMap);
        return languageMap;
    }

    public static JSONObject buildLanguageMap(String language, String label)
    {
	JSONObject languageMap = new JSONObject();
        String[] noneMap = new String[] {label};
        languageMap.put(language, noneMap);
        return languageMap;
    }
    
        public static JSONObject buildLanguageMapOtherContent(String language, String label)
    {
	JSONObject languageMap = new JSONObject();
        languageMap.put(language,label+ " List");
        return languageMap;
    }
    
    /**
    *  Make a hashmap with old @type from v2 API being keys and new type and format fields being values
     *  to use in buildAnnotationResource
    * */
    private static HashMap formatTextualBody()
    {
        HashMap<String, JSONObject> map = new HashMap();
        
        JSONObject textMap = new JSONObject();
        textMap.put("type", "TextualBody");
        textMap.put("format", "text/plain");
        map.put("cnt:ContentAsText", textMap);
	
        return map;

    }

    /**
     * Make a body property for annotations according to IIIF v3 API using @type and value from v2
     * works based on map build by formatTextualBody() and throws NoSuchElementException if type was not found among keys
     * */
    public static JSONObject buildAnnotationBody(String type, String value) throws NoSuchElementException
    {
        // since jsonobject extends object, casting should be safe
        JSONObject body = (JSONObject) formatTextualBody().get(type);
        if (body == null)
        {
            throw new NoSuchElementException(type + " was not found among supported types: " + formatTextualBody().keySet().toString());
        }
        body.put("language", new String[] {"none"});
        body.put("value", value);
        return body;
    }

    public static JSONObject buildAnnotationBody(String type, String value, String purpose) throws NoSuchElementException
    {
        JSONObject body = buildAnnotationBody(type, value);
        body.put("purpose", purpose);
        return body;
    }

}
