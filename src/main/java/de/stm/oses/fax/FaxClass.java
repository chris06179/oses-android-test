package de.stm.oses.fax;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class FaxClass implements Serializable {

	private int id;
    private String gb;
    private String beschreibung;
    private LatLng position;
    private String fax;
    private double distance;
    private String ril100;
    private String name;
    private Marker marker;

    private boolean selected = false;

    public FaxClass() {
       // leerer Konstruktor zur Nutzung mit setter
    }

    public FaxClass(int id, String gb, String beschreibung, LatLng position, String fax, double distance, String ril100, String name, Marker marker) {
        this.id = id;
        this.gb = gb;
        this.beschreibung = beschreibung;
        this.position = position;
        this.fax = fax;
        this.distance = distance;
        this.ril100 = ril100;
        this.name = name;
        this.marker = marker;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGb() {
        return gb;
    }

    public void setGb(String gb) {
        this.gb = gb;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public void setBeschreibung(String beschreibung) {
        this.beschreibung = beschreibung;
    }

    public LatLng getPosition() {
        return position;
    }

    public void setPosition(LatLng position) {
        this.position = position;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getRil100() {
        return ril100;
    }

    public void setRil100(String ril100) {
        this.ril100 = ril100;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public float getDistanceTo(LatLng to) {

        float[] result = new float[3];
        Location.distanceBetween(position.latitude, position.longitude, to.latitude, to.longitude, result);

        if (result.length > 0)
            return result[0];
        else
            return 0;

    }
}