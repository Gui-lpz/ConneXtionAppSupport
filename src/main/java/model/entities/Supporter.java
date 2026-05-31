package model.entities;

public class Supporter {

    private int id;
    private String name;
    private String firstSurname;
    private String secondSurname;
    private String email;
    private String password;
    private int serviceId;
    private int supervisorId;

    public Supporter() {
    }

    public Supporter(int id, String name, String firstSurname, String secondSurname,
                     String email, String password, int serviceId, int supervisorId) {
        this.id = id;
        this.name = name;
        this.firstSurname = firstSurname;
        this.secondSurname = secondSurname;
        this.email = email;
        this.password = password;
        this.serviceId = serviceId;
        this.supervisorId = supervisorId;
    }

    public boolean verifyLogin(String email, String password) {
        return this.email.equals(email) && this.password.equals(password);
    }

    public void startIssue(Issue issue) {
        issue.setStatus("En Progreso");
    }

    public void resolveIssue(Issue issue, String resolutionComment) {
        issue.setStatus("Resuelto");
        issue.setResolutionComment(resolutionComment);
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


    public int getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    @Override
    public String toString() {
        return "Supporter{" + "id=" + id + ", name=" + name + ", firstSurname="
                + firstSurname + ", secondSurname=" + secondSurname + ", email="
                + email + ", serviceId=" + serviceId + ", supervisorId="
                + supervisorId + '}';
    }
}