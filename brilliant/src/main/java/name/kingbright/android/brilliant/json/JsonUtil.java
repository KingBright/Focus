package name.kingbright.android.brilliant.json;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * @author Jin Liang
 * @since 16/1/4
 */
public class JsonUtil {
    private static Gson gson;

    private static final String EMPTY_JSON = "{}";

    static {
        GsonBuilder builder = new GsonBuilder();
        gson = builder.create();
    }

    public static String toJson(Object object) {
        if (object == null) {
            return EMPTY_JSON;
        }

        return gson.toJson(object);
    }

    public static <T> T toObject(String json, Class<T> cls) {
        if (TextUtils.isEmpty(json) || cls == null) {
            return null;
        }
        try {
            return gson.fromJson(json, cls);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> ArrayList<T> toList(String json, Class<T> classOfT) {
        Type type = new TypeToken<ArrayList<JsonObject>>() {
        }.getType();
        ArrayList<JsonObject> jsonObjs = gson.fromJson(json, type);

        ArrayList<T> listOfT = new ArrayList<>();
        for (JsonObject jsonObj : jsonObjs) {
            listOfT.add(gson.fromJson(jsonObj, classOfT));
        }
        return listOfT;
    }
}
