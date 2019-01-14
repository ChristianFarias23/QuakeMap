package cl.ucn.disc.dsm.cafa.quakemap;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cl.ucn.disc.dsm.cafa.quakemap.controllers.EarthquakeCatalogController;
import cl.ucn.disc.dsm.cafa.quakemap.models.EarthquakeData;

public class MainActivity extends AppCompatActivity {

    MapView map;

    List<EarthquakeData> earthquakeDataList;

    GeoPoint initialPoint = new GeoPoint(-23.6812, -70.4102);

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
        map.setScrollableAreaLimitLatitude(TileSystem.MaxLatitude,-TileSystem.MaxLatitude, 0);

        // Punto por defecto: UCN.
        map.getController().setCenter(initialPoint);

        /*
        // Marcador de ejemplo:
        Marker initialMarker = new Marker(map);
        initialMarker.setTitle("Este es un Titulo");
        initialMarker.setSnippet("Este es un Snippet");
        initialMarker.setSubDescription("Esta es una SubDescripcion");

        initialMarker.setPosition(initialPoint);
        initialMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        // Anadir marcador al mapa.
        map.getOverlays().add(initialMarker);
        */

        map.getController().animateTo(
                initialPoint,
                5.0,
                3000L
        );

        downloadData();
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    private void createMarkers() {
        if (earthquakeDataList != null && !earthquakeDataList.isEmpty()) {
            for (EarthquakeData ed : earthquakeDataList) {
                createEarthquakeMarker(ed);
            }
        }
    }

    private void createEarthquakeMarker(EarthquakeData data){
        Marker marker = new Marker(map);
        marker.setTitle(data.properties.title);
        marker.setSnippet(data.geometry.toString());

        GeoPoint point = new GeoPoint(data.geometry.getLatitude(), data.geometry.getLongitude());
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
    }

    private void downloadData()
    {
        Toast.makeText(this, "Descargando informacion...", Toast.LENGTH_SHORT).show();

        AsyncTask.execute(() -> {

            Log.d("TAG", "-------------------");
            Log.d("TAG", "Descargando informacion...");
            Log.d("TAG", "-------------------");

            List<EarthquakeData> earthquakesData = null;

            try {
                Date d = getCurrentDateDaysAgo(3);
                earthquakesData = EarthquakeCatalogController.getEarthquakeCatalogByStartTime(formatDate(d));

            } catch (Exception e) {
                // Ocurrio un error.
                Log.d("TAG", "ERROR: " + e.getMessage() + "\n" + e.getStackTrace());

                runOnUiThread(()->{
                    Toast.makeText(this, "Ocurrio un error al descargar la informacion...", Toast.LENGTH_SHORT).show();
                });
            }

            if (earthquakesData != null) {

                earthquakeDataList = earthquakesData;

                createMarkers();

                runOnUiThread(()->{

                    Toast.makeText(this, "Dirigiendo al ultimo terremoto...", Toast.LENGTH_SHORT).show();

                    EarthquakeData last = earthquakeDataList.get(0);

                    map.getController().animateTo(
                            new GeoPoint(last.geometry.getLatitude(), last.geometry.getLongitude()),
                            8.0,
                            3000L
                    );
                    // pSpeed esta en milisegundos!!!
                });

                for (EarthquakeData earthquakeData : earthquakesData) {
                    Log.d(".", "..............................................");
                    Log.d("EQ", "Title: "+earthquakeData.properties.title);
                    Log.d("EQ", "Date: "+new Date(earthquakeData.properties.time));
                    Log.d("EQ", "Coordinates: " + earthquakeData.geometry.toString());
                }
                Log.d(".", "..............................................");
            }
        });
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    //earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2014-01-01&endtime=2014-01-02


    private Date getCurrentDateDaysAgo(int daysAgo){

        if (Math.signum(daysAgo) == -1)
            daysAgo*=-1;

        Calendar cal = Calendar.getInstance();

        // Restar la cantidad de meses..
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo);

        return cal.getTime();
    }

    private String formatDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

}