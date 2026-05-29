package model.entities;

public class Supervisor {

    private int id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String email;
    private String password;
    private int serviceId;

    public Supervisor() {
    }

    public Supervisor(int id, String name, String firstSurname, String secondSurname,
                      String email, String password, int serviceId) {
        this.id = id;
        this.name = name;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.email = email;
        this.password = password;
        this.serviceId = serviceId;
    }

    public boolean verifyLogin(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public void assignIssue(Issue issue, Supporter supporter) {
        issue.setSupporterId(supporter.getId());
        issue.setStatus("Asignado");
    }

    public String getFullName() {
        return name + " " + firstSurname + " " + secondSurname;
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


    public String getFirstSurname() {
        return firstSurname;
    }

    public void setFirstSurname(String firstSurname) {
        this.firstSurname = firstSurname;
    }


    public String getSecondSurname() {
        return secondSurname;
    }

    public void setSecondSurname(String secondSurname) {
        this.secondSurname = secondSurname;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public String toString() {
        return "Supervisor{" + "id=" + id + ", name=" + name + ", firstSurname="
                + firstSurname + ", secondSurname=" + secondSurname + ", email="
                + email + ", serviceId=" + serviceId + '}';
    }
}