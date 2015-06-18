package de.stm.oses.helper;

public class FunktionenClass {

	private int id;
    private String funktion;

    public FunktionenClass(int id, String funktion) {
        this.id = id;
        this.funktion = funktion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFunktion() {
        return funktion;
    }

    public void setFunktion(String funktion) {
        this.funktion = funktion;
    }
}