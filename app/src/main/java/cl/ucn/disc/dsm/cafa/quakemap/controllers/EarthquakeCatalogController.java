package cl.ucn.disc.dsm.cafa.quakemap.controllers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import cl.ucn.disc.dsm.cafa.quakemap.adapters.MyDeserializer;
import cl.ucn.disc.dsm.cafa.quakemap.models.EarthquakeData;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class EarthquakeCatalogController {
    /**
     * Representa el servicio que consume la API.
     */
    public interface EarthquakeCatalogService {

        @GET("query?format=geojson&starttime=2018-12-1&limit=100&orderby=time-asc")
        Call<List<EarthquakeData>> getCatalog();
    }

    /**
     * ...
     */
    private static final HttpLoggingInterceptor interceptor  = new HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.HEADERS);

    /**
     * ...
     */
    private static final OkHttpClient cliente = new OkHttpClient.Builder()
            .addInterceptor(interceptor).build();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(List.class, new MyDeserializer<List<EarthquakeData>>())
            .create();

    /**
     * Instancia de retrofit.
     */
    private static final Retrofit retro = new Retrofit.Builder()
            .baseUrl("https://earthquake.usgs.gov/fdsnws/event/1/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(cliente)
            .build();

    /**
     * Crea mi servicio declarado en la interface a traves de retrofit.
     */
    private static final EarthquakeCatalogService servicio = retro.create(EarthquakeCatalogService.class);

    public static List<EarthquakeData> getEarthquakeCatalog() throws IOException {
        Call<List<EarthquakeData>> earthquakeCatalogCall = servicio.getCatalog();
        List<EarthquakeData> earthquakeDataList = earthquakeCatalogCall.execute().body();

        Log.d("TAG", "-> SIZE " + earthquakeDataList.size());

        return earthquakeDataList;
    }

}
