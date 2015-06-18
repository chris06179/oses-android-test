package de.stm.oses.helper;

import de.stm.oses.R;

public class ListClass {

	private int id;
    private String title;
    private int icon = -1;
    private String color = "";
    private String ident = null;

    private boolean selected = false;

    private boolean isHeader = false;
    private String HeaderText;

    public ListClass(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public ListClass(int id, String title, int icon) {
        this.id = id;
        this.title = title;
        this.icon = icon;
    }

    public ListClass(int id, String title, int icon, String color) {
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.color = color;
    }

    public ListClass(String ident , String title, String color) {
        this.title = title;
        this.icon =  R.drawable.ic_blank;
        this.ident = ident;
        this.color = color;
    }

    public ListClass(String ident , String title) {
        this.title = title;
        this.ident = ident;
    }

    public ListClass(boolean isHeader, String headerText) {
        this.isHeader = isHeader;
        this.HeaderText = headerText;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    public String getHeaderText() {
        return HeaderText;
    }

    public void setHeaderText(String headerText) {
        HeaderText = headerText;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIdent() {
        return ident;
    }

    public void setIdent(String ident) {
        this.ident = ident;
    }
}