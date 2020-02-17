package de.stm.oses.verwendung;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.stm.oses.arbeitsauftrag.ArbeitsauftragBuilder;

public class VerwendungClass implements Parcelable {

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

    private int aufdb;
    private int aufde;
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
    private File arbeitsauftragCacheFile;

    public VerwendungClass(boolean isVerwendungSummary) {
        this.isVerwendungSummary = isVerwendungSummary;
        this.urlaub = 0;
        this.schichten = 0;
    }

    public VerwendungClass() {
        // to be removed
    }

    protected VerwendungClass(Parcel in) {
        id = in.readInt();
        kat = in.readString();
        bezeichner = in.readString();
        fpla = in.readString();
        datum = in.readLong();
        db = in.readString();
        de = in.readString();
        est = in.readString();
        estid = in.readInt();
        funktion = in.readString();
        funktionId = in.readInt();
        pause = in.readString();
        pauseint = in.readInt();
        pauseRil = in.readString();
        baureihen = in.readString();
        adb = in.readString();
        ade = in.readString();
        dbr = in.readString();
        der = in.readString();
        adban = in.readDouble();
        adean = in.readDouble();
        apause = in.readString();
        apauser = in.readString();
        az = in.readString();
        oaz = in.readInt();
        aufdb = in.readInt();
        aufde = in.readInt();
        aufdz = in.readInt();
        info = in.readString();
        notiz = in.readString();
        msoll = in.readString();
        mist = in.readString();
        mdifferenz = in.readString();
        allowSDL = in.readByte() != 0x00;
        ShowTimeError = in.readByte() != 0x00;
        isVerwendungSummary = in.readByte() != 0x00;
        label = in.readString();
        azg = in.readString();
        urlaub = in.readInt();
        schichten = in.readInt();
        monat = in.readString();
        jahr = in.readString();
        arbeitsauftrag = in.readInt();

        String path = in.readString();
        if (path.equals(""))
            arbeitsauftragCacheFile = null;
        else
            arbeitsauftragCacheFile = new File(path);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(kat);
        dest.writeString(bezeichner);
        dest.writeString(fpla);
        dest.writeLong(datum);
        dest.writeString(db);
        dest.writeString(de);
        dest.writeString(est);
        dest.writeInt(estid);
        dest.writeString(funktion);
        dest.writeInt(funktionId);
        dest.writeString(pause);
        dest.writeInt(pauseint);
        dest.writeString(pauseRil);
        dest.writeString(baureihen);
        dest.writeString(adb);
        dest.writeString(ade);
        dest.writeString(dbr);
        dest.writeString(der);
        dest.writeDouble(adban);
        dest.writeDouble(adean);
        dest.writeString(apause);
        dest.writeString(apauser);
        dest.writeString(az);
        dest.writeInt(oaz);
        dest.writeInt(aufdb);
        dest.writeInt(aufde);
        dest.writeInt(aufdz);
        dest.writeString(info);
        dest.writeString(notiz);
        dest.writeString(msoll);
        dest.writeString(mist);
        dest.writeString(mdifferenz);
        dest.writeByte((byte) (allowSDL ? 0x01 : 0x00));
        dest.writeByte((byte) (ShowTimeError ? 0x01 : 0x00));
        dest.writeByte((byte) (isVerwendungSummary ? 0x01 : 0x00));
        dest.writeString(label);
        dest.writeString(azg);
        dest.writeInt(urlaub);
        dest.writeInt(schichten);
        dest.writeString(monat);
        dest.writeString(jahr);
        dest.writeInt(arbeitsauftrag);

        if (arbeitsauftragCacheFile != null)
            dest.writeString(arbeitsauftragCacheFile.getAbsolutePath());
        else
            dest.writeString("");
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<VerwendungClass> CREATOR = new Parcelable.Creator<VerwendungClass>() {
        @Override
        public VerwendungClass createFromParcel(Parcel in) {
            return new VerwendungClass(in);
        }

        @Override
        public VerwendungClass[] newArray(int size) {
            return new VerwendungClass[size];
        }
    };

    private static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
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

        this.setAufdb(schicht.optInt("aufdb", 0));
        this.setAufde(schicht.optInt("aufde", 0));
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


        // Arbeitsauftrag

        if (getKat().equals("S") && ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            ArbeitsauftragBuilder auftrag = new ArbeitsauftragBuilder(this);

            arbeitsauftragCacheFile = auftrag.getExtractedCacheFile();

            if (arbeitsauftragCacheFile != null) {
                arbeitsauftrag = ArbeitsauftragBuilder.TYPE_CACHED;
            }

            if (arbeitsauftragCacheFile != null && schicht.optBoolean("hasSharedArbeitsauftrag", false)) {

                String hash = calculateMD5(arbeitsauftragCacheFile);

                if (hash != null && !hash.equals(schicht.getString("SharedArbeitsauftragHash"))) {
                    arbeitsauftrag = ArbeitsauftragBuilder.TYPE_ONLINE;
                    return;
                }
            }

            if (arbeitsauftragCacheFile == null && schicht.optBoolean("hasSharedArbeitsauftrag", false)) {
                this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_ONLINE;
            }

        } else {
            if (schicht.optBoolean("hasSharedArbeitsauftrag", false)) {
                arbeitsauftrag = ArbeitsauftragBuilder.TYPE_ONLINE;
            } else {
                arbeitsauftrag = ArbeitsauftragBuilder.TYPE_NONE;
            }
        }

    }

    public static ArrayList<VerwendungClass> getNewListFromJSON(String jsonInput, Context context) throws JSONException {

        ArrayList<VerwendungClass> list = new ArrayList<>();

        JSONObject verwendung = new JSONObject(jsonInput);
        if (verwendung.names() == null)
            return list;
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
        Date date = new Date((unixSeconds) * 1000L); // *1000 is to convert seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.GERMANY); // the format of your date
        return sdf.format(date);

    }

    public Date getDatumDate() {

        long unixSeconds = datum;
        return new Date((unixSeconds) * 1000L);

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

    public int getAufdb() {
        return aufdb;
    }

    public void setAufdb(int aufdb) {
        this.aufdb = aufdb;
    }

    public int getAufde() {
        return aufde;
    }

    public void setAufde(int aufde) {
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

    public File getArbeitsauftragCacheFile() {
        return arbeitsauftragCacheFile;
    }

    public void setArbeitsauftragCacheFile(File file) {
        arbeitsauftragCacheFile = file;
        arbeitsauftrag = ArbeitsauftragBuilder.TYPE_CACHED;
    }

    public void setArbeitsauftragExtracting() {
        this.arbeitsauftrag = ArbeitsauftragBuilder.TYPE_EXTRACTING;
    }

    public void setArbeitsauftragType(int type) {
        arbeitsauftrag = type;
    }
}