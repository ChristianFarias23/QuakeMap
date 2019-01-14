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

        /**
         * Obtiene un catalogo de terremotos dada una fecha pasada.
         * El limite de la API es de 20000 resultados. Por temas de rendimiento y estetica,
         * el limite maximo sera MAX_LIMIT.
         * @param startTime : Fecha desde la que se quiere obtener resultados (anio-mes-dia).
         * @return Call.
         */
        @GET("query?format=geojson&orderby=time&limit="+MAX_LIMIT)
        Call<List<EarthquakeData>> getCatalogByStartTime(
                @Query("starttime") String startTime);


        /**
         * Obtiene un catalogo de terremotos con la cantidad especificada.
         * @param limit : Cantidad de resultados que se quieren obtener.
         * @return Call.
         */
        @GET("query?format=geojson&orderby=time")
        Call<List<EarthquakeData>> getCatalogByLimit(
                @Query("limit") int limit);


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


    //----------------------------------------------------------------------------------------------

    static final int MIN_LIMIT = 10;
    static final int MAX_LIMIT = 1000;

    public static List<EarthquakeData> getEarthquakeCatalogByStartTime(String startTime) throws IOException {
        Call<List<EarthquakeData>> earthquakeCatalogCall = servicio.getCatalogByStartTime(startTime);
        List<EarthquakeData> earthquakeDataList = earthquakeCatalogCall.execute().body();

        Log.d("TAG", "-> SIZE " + earthquakeDataList.size());

        return earthquakeDataList;
    }

    public static List<EarthquakeData> getEarthquakeCatalogByLimit(int limit) throws IOException {

        if (limit < MIN_LIMIT)
            limit = MIN_LIMIT;

        if (limit > MAX_LIMIT)
            limit = MAX_LIMIT;

        Call<List<EarthquakeData>> earthquakeCatalogCall = servicio.getCatalogByLimit(limit);
        List<EarthquakeData> earthquakeDataList = earthquakeCatalogCall.execute().body();

        Log.d("TAG", "-> SIZE " + earthquakeDataList.size());

        return earthquakeDataList;
    }



}
