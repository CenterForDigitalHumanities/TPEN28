package utils;

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
}
