package de.stm.oses.helper;


public class MenuClass{
 
    private int icon;
    private String title;
    private int id;
    private String counter;
 
    private boolean isGroupHeader = false;
    private boolean selected = false;
 
    public MenuClass(String title) {
        this(-1,title,0,null);
        isGroupHeader = true;
    }
    public MenuClass(int icon, String title, int id, String counter) {
        this.icon = icon;
        this.title = title;
        this.id = id;
        this.counter = counter;
    }

    public MenuClass(int icon, String title, int id, String counter, boolean selected) {
        this.icon = icon;
        this.title = title;
        this.id = id;
        this.counter = counter;
        this.selected = true;
    }
    
    public int getIcon() {
		return icon;
	}
	public void setIcon(int icon) {
		this.icon = icon;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getID() {
		return id;
	}
    public void setID(int id) {
		this.id = id;
	}
	public String getCounter() {
		return counter;
	}
	public void setCounter(String counter) {
		this.counter = counter;
	}
	public boolean isGroupHeader() {
		return isGroupHeader;
	}
	public void setGroupHeader(boolean isGroupHeader) {
		this.isGroupHeader = isGroupHeader;
	}
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

//gettters & setters...
}