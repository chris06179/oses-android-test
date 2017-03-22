package de.stm.oses.verwendung;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.stm.oses.helper.ArbeitsauftragBuilder;


public class VerwendungClass implements Serializable {

    private int id;
    private String kat;
    private String bezeichner;
    private String fpla;
    private long datum;
    private String db;
    private String de;
    private String est;
    private int estid;
    private String funktion;
    private int funktionId;
    private String pause;
    private int pauseint;
    private String pauseRil;
    private String baureihen;
    private String adb;
    private String ade;
    private String dbr;
    private String der;
    private double adban;
    private double adean;
    private String apause;
    private String apauser;
    private String az;
    private int oaz;
    private String aufart;
    private String aufdb;
    private String aufde;
    private int aufdz;
    private String info;
    private String notiz;
    private String msoll;
    private String mist;
    private String mdifferenz;
    private boolean allowSDL = false;
    private boolean ShowTimeError = false;

    private boolean isVerwendungSummary = false;
    private String label;
    private String azg;
    private int urlaub;
    private int schichten;
    private String monat;
    private String jahr;

    private int arbeitsauftrag = ArbeitsauftragBuilder.TYPE_NONE;
    private File arbeitsauftragDilocFile;
    private File arbeitsauftragCacheFile;

    public VerwendungClass(boolean isVerwendungSummary) {
        this.isVerwendungSummary = isVerwendungSummary;
        this.urlaub = 0;
        this.schichten = 0;
    }

    public VerwendungClass() {
        // to be removed
    }

    public VerwendungClass(JSONObject schicht, Context context) throws JSONException {

        this.setId(schicht.getInt("id"));

        this.setKat(schicht.getString("kat"));

        this.setBezeichner(schicht.getString("bezeichner"));
        this.setFpla(schicht.getString("fpla"));

        this.setDb(schicht.getString("db"));
        this.setDe(schicht.getString("de"));
        this.setAdb(schicht.getString("adb"));
        this.setAde(schicht.getString("ade"));

        this.setAdban(schicht.optDouble("adb_an", 1));
        this.setAdean(schicht.optDouble("ade_an", 1));

        this.setPause(schicht.getString("pause"));
        this.setPauseInt(schicht.getInt("pauseint"));
        this.setPauseRil(schicht.getString("pause_ril"));
        this.setApause(schicht.getString("apause"));

        this.setDatum(schicht.getInt("datum"));
        this.setBaureihen(schicht.getString("baureihen"));

        this.setAz(schicht.getString("az"));
        this.setOaz(schicht.optInt("o_az", 0));

        this.setAufart(schicht.getString("auf_art"));
        this.setAufdb(schicht.getString("aufdb"));
        this.setAufde(schicht.getString("aufde"));
        this.setAufdz(schicht.optInt("aufdz", 0));

        this.setMdifferenz(schicht.getString("mdifferenz"));

        this.setEst(schicht.getString("est"));
        this.setEstId(schicht.optInt("estid", 0));

        this.setMsoll(schicht.getString("msoll"));

        this.setMist(schicht.getString("mist"));

        this.setFunktion(schicht.getString("funktion"));
        this.setFunktionId(schicht.getInt("funktionid"));

        this.setDbr(schicht.getString("dbr"));
        this.setDer(schicht.getString("der"));
        this.setApauser(schicht.getString("apauser"));


        if (schicht.isNull("notiz"))
            this.setNotiz(null);
        else
            this.setNotiz(schicht.optString("notiz", null));

        this.setInfo(schicht.optString("info", ""));

        if (schicht.has("AllowSDL"))
            this.setAllowSDL(schicht.getBoolean("AllowSDL"));

        if (schicht.has("ShowTimeError"))
            this.setShowTimeError(schicht.getBoolean("ShowTimeError"));

        this.processArbeitsauftrag(context);

    }

    public static ArrayList<VerwendungClass> getNewList(String jsonInput, Context context) throws JSONException {

        ArrayList<VerwendungClass> list = new ArrayList<>();

        JSONObject verwendung = new JSONObject(jsonInput);
        JSONObject monat = verwendung.getJSONObject(verwendung.names().getString(0));

        JSONArray schichten = monat.getJSONArray("data");

        for (int i = 0; i < schichten.length(); i++) {

            JSONObject schicht = schichten.getJSONObject(i);

            list.add(new VerwendungClass(schicht, context));

        }

        // Zusammenfassung

        SharedPreferences AppSettings = PreferenceManager.getDefaultSharedPreferences(context);
        String position = AppSettings.getString("sumPosition", "BOTTOM");

        if (!position.equals("NONE")) {

            VerwendungClass sumData = new VerwendungClass(true);

            sumData.setLabel(verwendung.names().getString(0));
            if (monat.has("schichten"))
                sumData.setSchichten(monat.getInt("schichten"));
            if (monat.has("urlaub"))
                sumData.setUrlaub(monat.getInt("urlaub"));
            sumData.setMsoll(monat.getString("msoll"));
            sumData.setAzg(monat.getString("azg"));
            sumData.setMdifferenz(monat.getString("mdifferenz"));

            if (position.equals("BOTTOM") || position.equals("BOTH"))
                list.add(sumData);

            if (position.equals("TOP") || position.equals("BOTH"))
                list.add(0, sumData);
        }

        return list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKat() {
        return kat;
    }

    public void setKat(String kat) {
        this.kat = kat;
    }

    public String getBezeichner() {
        return bezeichner;
    }

    public void setBezeichner(String schicht) {
        this.bezeichner = schicht;
    }

    public String getFpla() {
        return fpla;
    }

    public void setFpla(String fpla) {
        this.fpla = fpla;
    }

    public long getDatum() {
        return datum;
    }

    public String getDatumFormatted(String format) {

        long unixSeconds = datum;
        Date date = new Date((unixSeconds + 3600) * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.GERMANY); // the format of your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
        return sdf.format(date);

    }

    public Date getDatumDate() {

        long unixSeconds = datum;
        return new Date((unixSeconds + 3600) * 1000L);

    }

    public void setDatum(int datum) {
        this.datum = datum;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getDe() {
        return de;
    }

    public void setDe(String de) {
        this.de = de;
    }

    public String getEst() {
        return est;
    }

    public void setEst(String est) {
        this.est = est;
    }

    public String getFunktion() {
        return funktion;
    }

    public void setFunktion(String funktion) {
        this.funktion = funktion;
    }

    public String getPause() {
        return pause;
    }

    public void setPause(String pause) {
        this.pause = pause;
    }

    public String getBaureihen() {
        return baureihen;
    }

    public void setBaureihen(String baureihen) {
        this.baureihen = baureihen;
    }

    public String getAdb() {
        return adb;
    }

    public void setAdb(String adb) {
        this.adb = adb;
    }

    public String getAde() {
        return ade;
    }

    public void setAde(String ade) {
        this.ade = ade;
    }

    public String getDbr() {
        return dbr;
    }

    public void setDbr(String dbr) {
        this.dbr = dbr;
    }

    public String getDer() {
        return der;
    }

    public void setDer(String der) {
        this.der = der;
    }

    public String getApause() {
        return apause;
    }

    public void setApause(String apause) {
        this.apause = apause;
    }

    public String getApauser() {
        return apauser;
    }

    public void setApauser(String apauser) {
        this.apauser = apauser;
    }

    public String getAz() {
        return az;
    }

    public void setAz(String az) {
        this.az = az;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getNotiz() {
        return notiz;
    }

    public void setNotiz(String notiz) {
        this.notiz = notiz;
    }

    public String getMsoll() {
        return msoll;
    }

    public void setMsoll(String msoll) {
        this.msoll = msoll;
    }

    public String getMist() {
        return mist;
    }

    public void setMist(String mist) {
        this.mist = mist;
    }

    public String getMdifferenz() {
        return mdifferenz;
    }

    public void setMdifferenz(String mdifferenz) {
        this.mdifferenz = mdifferenz;
    }

    public boolean isVerwendungSummary() {
        return isVerwendungSummary;
    }

    public void setVerwendungSummary(boolean isVerwendungSummary) {
        this.isVerwendungSummary = isVerwendungSummary;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAzg() {
        return azg;
    }

    public void setAzg(String azg) {
        this.azg = azg;
    }

    public String getMonat() {
        return monat;
    }

    public void setMonat(String monat) {
        this.monat = monat;
    }

    public String getJahr() {
        return jahr;
    }

    public void setJahr(String jahr) {
        this.jahr = jahr;
    }

    public boolean isAllowSDL() {
        return allowSDL;
    }

    public void setAllowSDL(boolean allowSDL) {
        this.allowSDL = allowSDL;
    }

    public boolean isShowTimeError() {
        return ShowTimeError;
    }

    public void setShowTimeError(boolean ShowTimeError) {
        this.ShowTimeError = ShowTimeError;
    }

    public int getUrlaub() {
        return urlaub;
    }

    public void setUrlaub(int urlaub) {
        this.urlaub = urlaub;
    }

    public int getSchichten() {
        return schichten;
    }

    public void setSchichten(int schichten) {
        this.schichten = schichten;
    }

    public int getOaz() {
        return oaz;
    }

    public void setOaz(int oaz) {
        this.oaz = oaz;
    }

    public int getEstId() {
        return estid;
    }

    public void setEstId(int estid) {
        this.estid = estid;
    }

    public int getPauseInt() {
        return pauseint;
    }

    public void setPauseInt(int pauseint) {
        this.pauseint = pauseint;
    }

    public String getPauseRil() {
        return pauseRil;
    }

    public void setPauseRil(String pauseRil) {
        this.pauseRil = pauseRil;
    }

    public String getAufart() {
        return aufart;
    }

    public void setAufart(String aufart) {
        this.aufart = aufart;
    }

    public String getAufdb() {
        return aufdb;
    }

    public void setAufdb(String aufdb) {
        this.aufdb = aufdb;
    }

    public String getAufde() {
        return aufde;
    }

    public void setAufde(String aufde) {
        this.aufde = aufde;
    }

    public int getAufdz() {
        return aufdz;
    }

    public void setAufdz(int aufdz) {
        this.aufdz = aufdz;
    }

    public double getAdban() {
        return adban;
    }

    public void setAdban(double adban) {
        this.adban = adban;
    }

    public double getAdean() {
        return adean;
    }

    public void setAdean(double adean) {
        this.adean = adean;
    }

    public int getFunktionId() {
        return funktionId;
    }

    public void setFunktionId(int funktionId) {
        this.funktionId = funktionId;
    }

    public int getArbeitsauftragType() {
        return arbeitsauftrag;
    }

    public void processArbeitsauftrag(Context context) {

        if (!getKat().equals("S")) {
            this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_NONE;
            return;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_NONE;
            return;
        }

        ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(this, context);
        this.arbeitsauftragDilocFile = auftrag.getDilocSourceFile();
        this.arbeitsauftragCacheFile = auftrag.getExtractedCacheFile();

        if (auftrag.getExtractedCacheFile() != null) {
            this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_CACHED;
            return;
        }

        if (this.arbeitsauftragDilocFile != null) {
            this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_DILOC;
            return;
        }

    }

    public File getArbeitsauftragDilocFile() {
        return arbeitsauftragDilocFile;
    }

    public File getArbeitsauftragCacheFile() {
        return arbeitsauftragCacheFile;
    }

    public void setArbeitsauftragCacheFile(File file) {
        this.arbeitsauftragCacheFile = file;
        this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_CACHED;
    }
}