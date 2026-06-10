package model.entities;

import java.time.LocalDateTime;

public class Issue {

    private int id;
    private String reference;
    private String classification;
    private String status;
    private LocalDateTime issueTimestamp;
    private String resolutionComment;
    private String description;
    private String contactAddress;
    private String contactPhone;
    private String contactEmail;
    private int serviceId;
    private String serviceName;
    private int supporterId;
    private int supervisorId;

    public Issue() {
        this.classification = "Media";
        this.status = "Ingresado";
        this.issueTimestamp = LocalDateTime.now();
    }

    public Issue(int id, String reference, String classification, String status,
                 LocalDateTime issueTimestamp, String resolutionComment,
                 String description, String contactAddress, String contactPhone,
                 String contactEmail, int serviceId, String serviceName,
                 int supporterId, int supervisorId) {

        this.id = id;
        this.reference = reference;
        this.classification = classification;
        this.status = status;
        this.issueTimestamp = issueTimestamp;
        this.resolutionComment = resolutionComment;
        this.description = description;
        this.contactAddress = contactAddress;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.serviceId = serviceId;
        this.serviceName = serviceName;
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


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getContactAddress() {
        return contactAddress;
    }

    public void setContactAddress(String contactAddress) {
        this.contactAddress = contactAddress;
    }


    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }


    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }


    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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
        return "Issue{"
                + "id=" + id
                + ", reference='" + reference + '\''
                + ", classification='" + classification + '\''
                + ", status='" + status + '\''
                + ", issueTimestamp=" + issueTimestamp
                + ", resolutionComment='" + resolutionComment + '\''
                + ", description='" + description + '\''
                + ", contactAddress='" + contactAddress + '\''
                + ", contactPhone='" + contactPhone + '\''
                + ", contactEmail='" + contactEmail + '\''
                + ", serviceId=" + serviceId
                + ", serviceName='" + serviceName + '\''
                + ", supporterId=" + supporterId
                + ", supervisorId=" + supervisorId
                + '}';
    }
}