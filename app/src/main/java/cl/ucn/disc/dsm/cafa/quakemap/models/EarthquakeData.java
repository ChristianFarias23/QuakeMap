package cl.ucn.disc.dsm.cafa.quakemap.models;

import java.util.List;

public class EarthquakeData {

    public String id;
    public Properties properties;
    public Geometry geometry;

    public class Geometry{
        public List<Double> coordinates;

        @Override
        public String toString() {
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return "Lat: "+this.coordinates.get(0) + ", Lon: "+this.coordinates.get(1) + ", Alt: " + this.coordinates.get(2);
            return "No coordinates";
        }
        public Double getLatitude(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(0);
            return 0.0;
        }

        public Double getLongitude(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(1);
            return 0.0;
        }

        public Double getAltitude(){
            if (this.coordinates != null && !this.coordinates.isEmpty())
                return this.coordinates.get(2);
            return 0.0;
        }

    }
}
