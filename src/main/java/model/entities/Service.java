package model.entities;

public class Service {

    private int id;
    private String name;

    public Service() {
    }

    public Service(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public boolean isMobilePhone() {
        return "Telefonía móvil".equalsIgnoreCase(name);
    }

    public boolean isCable() {
        return "Cable".equalsIgnoreCase(name);
    }

    public boolean isInternet() {
        return "Internet".equalsIgnoreCase(name);
    }

    public boolean isLandlinePhone() {
        return "Telefonía fija".equalsIgnoreCase(name);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}