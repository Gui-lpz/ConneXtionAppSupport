package model.entities;

import java.time.LocalDateTime;

public class Issue {

    private int id;
    private String reference;
    private String classification;
    private String status;
    private LocalDateTime issueTimestamp;
    private String resolutionComment;
    private int serviceId;
    private int supporterId;
    private int supervisorId;

    public Issue() {
        this.classification = "Media";
        this.status = "Ingresado";
        this.issueTimestamp = LocalDateTime.now();
    }

    public Issue(int id, String reference, String classification, String status,
                 LocalDateTime issueTimestamp, String resolutionComment,
                 int serviceId, int supporterId, int supervisorId) {
        this.id = id;
        this.reference = reference;
        this.classification = classification;
        this.status = status;
        this.issueTimestamp = issueTimestamp;
        this.resolutionComment = resolutionComment;
        this.serviceId = serviceId;
        this.supporterId = supporterId;
        this.supervisorId = supervisorId;
    }

    public boolean isPending() {
        return "Ingresado".equalsIgnoreCase(status);
    }

    public boolean isAssigned() {
        return "Asignado".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "En Progreso".equalsIgnoreCase(status);
    }

    public boolean isResolved() {
        return "Resuelto".equalsIgnoreCase(status);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }


    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public LocalDateTime getIssueTimestamp() {
        return issueTimestamp;
    }

    public void setIssueTimestamp(LocalDateTime issueTimestamp) {
        this.issueTimestamp = issueTimestamp;
    }


    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }


    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }


    public int getSupporterId() {
        return supporterId;
    }

    public void setSupporterId(int supporterId) {
        this.supporterId = supporterId;
    }


    public int getSupervisorId() {
        return supervisorId;
    }

    public void setSupervisorId(int supervisorId) {
        this.supervisorId = supervisorId;
    }

    @Override
    public String toString() {
        return "Issue{" + "id=" + id + ", reference=" + reference
                + ", classification=" + classification + ", status=" + status
                + ", issueTimestamp=" + issueTimestamp + ", resolutionComment="
                + resolutionComment + ", serviceId=" + serviceId
                + ", supporterId=" + supporterId + ", supervisorId="
                + supervisorId + '}';
    }
}