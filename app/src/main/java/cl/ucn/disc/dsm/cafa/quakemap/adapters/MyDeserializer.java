package cl.ucn.disc.dsm.cafa.quakemap.adapters;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

//Basado en: https://stackoverflow.com/questions/23070298/get-nested-json-object-with-gson-using-retrofit


public class MyDeserializer<T> implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        // Obtiene la lista de terremotos desde el JSON.
        JsonElement earthquakesData = json.getAsJsonObject().get("features");

        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
        // to this deserializer

        return new Gson().fromJson(earthquakesData, typeOfT);
    }
}
