package model.entities;

import java.time.LocalDateTime;

public class HomeVisit {

    private int id;
    private int issueId;
    private int supporterId;
    private LocalDateTime visitDateTime;
    private String address;
    private String contactPhone;
    private String status; // Visita programada, en proceso, completada, Cancelada**// Nota: agregar a la base de datos
    private String observations;

    public HomeVisit() {
        this.status = "Programada";
    }

    public HomeVisit(int id, int issueId, int supporterId,
                     LocalDateTime visitDateTime, String address,
                     String contactPhone, String status, String observations) {
        this.id = id;
        this.issueId = issueId;
        this.supporterId = supporterId;
        this.visitDateTime = visitDateTime;
        this.address = address;
        this.contactPhone = contactPhone;
        this.status = status;
        this.observations = observations;
    }

    public boolean isScheduled() {
        return "Programada".equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return "Completada".equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return "Cancelada".equalsIgnoreCase(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }


    public int getSupporterId() {
        return supporterId;
    }

    public void setSupporterId(int supporterId) {
        this.supporterId = supporterId;
    }


    public LocalDateTime getVisitDateTime() {
        return visitDateTime;
    }

    public void setVisitDateTime(LocalDateTime visitDateTime) {
        this.visitDateTime = visitDateTime;
    }


    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    @Override
    public String toString() {
        return "HomeVisit{" +
                "id=" + id +
                ", issueId=" + issueId +
                ", supporterId=" + supporterId +
                ", visitDateTime=" + visitDateTime +
                ", address='" + address + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", status='" + status + '\'' +
                ", observations='" + observations + '\'' +
                '}';
    }
    
}