package cl.ucn.disc.dsm.cafa.quakemap.models;

import java.util.List;

import lombok.Getter;

public class EarthquakeData {

    @Getter
    private String id;

    @Getter
    private Properties properties;

    @Getter
    private Geometry geometry;

    public class Geometry{
        List<Double> coordinates;

        @Override
        public String toString() {
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return "Longitud: "+ getLongitude() +
                        ", Latitud: "+ getLatitude() +
                        ", Profundidad: " + getDepth() + "km";
            return "No coordinates";
        }

        public Double getLongitude(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(0);
            return 0.0;
        }

        public Double getLatitude(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(1);
            return 0.0;
        }

        public Double getDepth(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(2);
            return 0.0;
        }
    }
}
