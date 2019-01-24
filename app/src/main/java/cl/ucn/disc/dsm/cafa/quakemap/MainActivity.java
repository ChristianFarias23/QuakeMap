package cl.ucn.disc.dsm.cafa.quakemap;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cl.ucn.disc.dsm.cafa.quakemap.controllers.EarthquakeCatalogController;
import cl.ucn.disc.dsm.cafa.quakemap.models.EarthquakeData;

import static cl.ucn.disc.dsm.cafa.quakemap.controllers.EarthquakeCatalogController.MAX_LIMIT;

public class MainActivity extends AppCompatActivity {

    /**
     * La cantidad de dias atras que se piensa descargar. (Ver EarthquakeCatalogController).
     */
    public static final int DEFAULT_DAYS_AGO = 31;

    /**
     * La vista del mapa OSM.
     */
    private MapView map;

    /**
     * Lista de terremotos obtenida al descargar.
     */
    private List<EarthquakeData> earthquakeDataList;

    /**
     * El punto inicial.
     */
    private final GeoPoint initialPoint = new GeoPoint(-23.6812, -70.4102);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string

        //inflate and create the map
        setContentView(R.layout.activity_main);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Configurar mapa:
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.getController().setZoom(15);
        map.setVerticalMapRepetitionEnabled(false);
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude, -TileSystem.MaxLatitude, 0);

        // Punto por defecto: UCN.
        map.getController().setCenter(initialPoint);

        map.getController().animateTo(
                initialPoint,
                5.0,
                3000L
        );

        downloadData();
    }

    /**
     * Metodo que se encarga de obtener todos los terremotos hace DEFAULT_DAYS_AGO dias atras
     * y guardarlos en la lista earthquakeDataList.
     */
    private void downloadData() {
        Toast.makeText(this, "Descargando datos...", Toast.LENGTH_SHORT).show();

        AsyncTask.execute(() -> {

            List<EarthquakeData> earthquakesData = null;

            try {
                // Obtiene todos los terremotos y los guarda en la lista earthquakesData.
                Date d = getCurrentDateDaysAgo(DEFAULT_DAYS_AGO);
                earthquakesData = EarthquakeCatalogController.getEarthquakeCatalogByStartTime(formatDate(d));

            } catch (Exception e) {
                // Ocurrio un error.
                Log.d("TAG", "ERROR: " + e.getMessage() + "\n" + e.getStackTrace());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Ocurrio un error al descargar los datos...", Toast.LENGTH_SHORT).show();
                });

                return;
            }

            earthquakeDataList = earthquakesData;
            createMarkers(earthquakeDataList);

            runOnUiThread(() -> {
                Toast.makeText(this, "Descarga completa", Toast.LENGTH_SHORT).show();
            });
        });
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /**
     * Importante!
     * Maneja la seleccion de un item. Los filtros son excluyentes,
     * esto es, no se puede filtrar por fecha y magnitud a la vez.
     * @param item: El item seleccionado.
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.item_last_one:

                // Ir al ultimo terremoto.
                goToTheLastEarthquake();

                return true;
            case R.id.item_last_24_hours:

                // Ahora - 1 dia = 24 horas atras.
                long last24Hours = getCurrentDateDaysAgo(1).getTime();
                createMarkersAfterLongDate(last24Hours);

                return true;
            case R.id.item_last_3_days:

                // 3 dias atras.
                long last3Days = getCurrentDateDaysAgo(3).getTime();
                createMarkersAfterLongDate(last3Days);

                return true;
            case R.id.item_last_week:

                // Ahora - 7 dias = Una semana atras.
                long lastWeek = getCurrentDateDaysAgo(7).getTime();
                createMarkersAfterLongDate(lastWeek);

                return true;
            case R.id.item_last_2_weeks:

                // 2 semanas atras.
                long last2Weeks = getCurrentDateDaysAgo(14).getTime();
                createMarkersAfterLongDate(last2Weeks);

                return true;
            case R.id.item_all:

                // Todos los disponibles.
                long lastMonth = getCurrentDateDaysAgo(DEFAULT_DAYS_AGO).getTime();
                createMarkersAfterLongDate(lastMonth);

                return true;
            case R.id.item_mag_3:

                createMarkersBetweenMagnitud(0,3);
                return  true;
            case R.id.item_mag_3_5:

                createMarkersBetweenMagnitud(3,5);
                return  true;
            case R.id.item_mag_5_7:

                createMarkersBetweenMagnitud(5,7);
                return  true;
            case R.id.item_mag_7:
                createMarkersBetweenMagnitud(7,10);
                return  true;
            case R.id.item_refresh:

                downloadData();
                return  true;
            default:
                return true;
        }
    }

    /**
     * Crea los marcadores de los terremotos que se encuentren entre las magnitudes indicadas por parametro.
     * @param lower: La magnitud menor.
     * @param upper: La magnitud mayor.
     */
    private void createMarkersBetweenMagnitud(int lower, int upper) {
        List<EarthquakeData> betweenEarthquakes = new ArrayList<>();

        if (earthquakeDataList != null && !earthquakeDataList.isEmpty()) {
            for (EarthquakeData eq : earthquakeDataList) {
                if (eq.getProperties().getMag() >= lower && eq.getProperties().getMag() < upper) {
                    betweenEarthquakes.add(eq);
                }
            }
            if (!betweenEarthquakes.isEmpty()) {
                if (betweenEarthquakes.size() == MAX_LIMIT) {
                    Toast.makeText(this,
                            "Se muestran todos los terremotos disponibles (" + betweenEarthquakes.size() + ")",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            "Se muestran " + betweenEarthquakes.size() + " terremotos entre estas magnitudes",
                            Toast.LENGTH_SHORT).show();
                }

                createMarkers(betweenEarthquakes);

            } else {
                Toast.makeText(this, "No hay terremotos entre estas magnitudes.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "La lista de terremotos se encuentra vacia.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Mueve la pantalla hacia el ultimo terremoto.
     */
    public void goToTheLastEarthquake(){
        if (earthquakeDataList != null && !earthquakeDataList.isEmpty()) {
            Toast.makeText(this, "Dirigiendo al ultimo terremoto...", Toast.LENGTH_SHORT).show();

            runOnUiThread(() -> {

                // El primero en la lista sera el mas reciente.
                EarthquakeData last = earthquakeDataList.get(0);

                // Mover hacia el ultimo terremoto...
                map.getController().animateTo(
                        new GeoPoint(last.getGeometry().getLatitude(), last.getGeometry().getLongitude()),
                        8.0,
                        3000L
                );
                // pSpeed esta en milisegundos!!!

            });
        } else {
            Toast.makeText(this, "La lista de terremotos se encuentra vacia.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Crea los marcadores de los terremotos que se encuentren entre hoy y la fecha indicada por parametro.
     * @param longDate: La fecha.
     */
    public void createMarkersAfterLongDate(long longDate){
        List<EarthquakeData> afterEarthquakes = new ArrayList<>();

        if (earthquakeDataList != null && !earthquakeDataList.isEmpty()) {
            // Mostrar solo los terremotos de hoy.
            for (EarthquakeData eq : earthquakeDataList) {
                if (eq.getProperties().getTime() >= longDate) {
                    afterEarthquakes.add(eq);
                }
            }
            if (!afterEarthquakes.isEmpty()) {
                if (afterEarthquakes.size() == MAX_LIMIT) {
                    Toast.makeText(this,
                            "Se muestran todos los terremotos disponibles (" + afterEarthquakes.size() + ")",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            "Se muestran " + afterEarthquakes.size() + " terremotos para este periodo",
                            Toast.LENGTH_SHORT).show();
                }

                createMarkers(afterEarthquakes);

            } else {
                // Uhm??
                Toast.makeText(this, "No hay terremotos en este periodo.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "La lista de terremotos se encuentra vacia.", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Crea los marcadores dados los terremotos en la lista indicada por parametro.
     * @param dataList: La lista de terremotos.
     */
    public void createMarkers(List<EarthquakeData> dataList){

        // Si la lista pasada por parametro tiene elementos...
        if (dataList != null && !dataList.isEmpty()) {

            // Vacia los marcadores actuales del mapa.
            map.getOverlays().clear();

            // y crea nuevos marcadores.
            for (EarthquakeData ed : dataList) {

                // Crea un marcador con los datos del terremoto.
                Marker marker = createEarthquakeMarker(ed);

                // Lo agrega al mapa.
                map.getOverlays().add(marker);
            }

            map.invalidate();
        }
    }

    /**
     * Crea un marcador en el mapa que representa un terremoto.
     * @param data: Los datos del terremoto para crear el marcador.
     * @return
     */
    private Marker createEarthquakeMarker(EarthquakeData data) {
        final Marker marker = new Marker(map);
        // Con esto se podia cambiar el icono, pero no queda tiempo.
        // Los iconos estan en el directorio drawables.
        //marker.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.ic_eq_green));
        marker.setTitle(data.getProperties().getTitle());
        marker.setSnippet(data.getGeometry().toString());
        marker.setSubDescription(dateToSpanish(new Date(data.getProperties().getTime())));

        final GeoPoint point = new GeoPoint(data.getGeometry().getLatitude(), data.getGeometry().getLongitude());
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        return marker;
    }

    //earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-01-02

    //------------------

    private static SimpleDateFormat formatter = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss", Locale.forLanguageTag("es-ES"));


    /**
     * Formatea la fecha a un string en espaniol.
     * @param date: La fecha a formatear.
     * @return
     */
    private String dateToSpanish(Date date) {
        return formatter.format(date);
    }


    /**
     * Obtiene la fecha actual menos la cantidad de dias indicadas por parametro.
     * @param daysAgo: La cantidad de dias a restar.
     * @return
     */
    private Date getCurrentDateDaysAgo(int daysAgo) {

        if (Math.signum(daysAgo) == -1)
            daysAgo *= -1;

        Calendar cal = Calendar.getInstance();

        // Restar la cantidad de dias...
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);

        return cal.getTime();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

}