package de.stm.oses.schichten;


import java.io.Serializable;

public class SchichtenClass implements Serializable {

	private int id;
    private String schicht;
    private String fpla;
    private String gv;
    private String gb;
    private String db;
    private String de;
    private String est;
    private int estid;
    private String funktion;
    private int funktionid;
    private int gbid;
    private String pause;
    private String baureihen;
    private String pause_ort;
    private String az;
    private String kommentar;

    private int aufdb;
    private int aufde;
    private int aufdz;

    private boolean selected = false;


    public SchichtenClass() {

    }
    
    public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getSchicht() {
		return schicht;
	}
	public void setSchicht(String schicht) {
		this.schicht = schicht;
	}
	public String getFpla() {
		return fpla;
	}
	public void setFpla(String fpla) {
		this.fpla = fpla;
	}
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
    public String getGb() {
        return gb;
    }
    public void setGb(String gb) {
        this.gb = gb;
    }
    public String getGv() {
        return gv;
    }
    public void setGv(String gv) {
        this.gv = gv;
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
	public String getAz() {
		return az;
	}
	public void setAz(String az) {
		this.az = az;
	}
    public String getPauseOrt() {
        return pause_ort;
    }
    public void setPauseOrt(String pause_ort) {
        this.pause_ort = pause_ort;
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    public String getKommentar() {
        return kommentar;
    }
    public void setKommentar(String kommentar) {
        this.kommentar = kommentar;
    }
    public int getEstid() {
        return estid;
    }
    public void setEstid(int estid) {
        this.estid = estid;
    }
    public int getFunktionid() {
        return funktionid;
    }
    public void setFunktionid(int funktionid) {
        this.funktionid = funktionid;
    }
    public int getGbid() {
        return gbid;
    }
    public void setGbid(int gbid) {
        this.gbid = gbid;
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
}