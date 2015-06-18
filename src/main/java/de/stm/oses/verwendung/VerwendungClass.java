package de.stm.oses.verwendung;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


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

    public VerwendungClass(boolean isVerwendungSummary) {
        this.isVerwendungSummary = isVerwendungSummary;
        this.urlaub = 0;
        this.schichten = 0;
    }
    public VerwendungClass() {

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
        Date date = new Date((unixSeconds+3600)*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.GERMAN); // the format of your date
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+1"));
		String formattedDate = sdf.format(date);
		return formattedDate;
		
	}
    public Date getDatumDate() {

        long unixSeconds = datum;
        Date date = new Date((unixSeconds+3600)*1000L); // *1000 is to convert seconds to milliseconds
        return date;

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
}