package model.entities;

import java.time.LocalDateTime;

public class Note {

    private int id;
    private String description;
    private LocalDateTime noteTimestamp;
    private int issueId;
    private int authorId;
    private String authorType; // SUPERVISOR o SUPPORTER

    public Note() {
        this.noteTimestamp = LocalDateTime.now();
    }

    public Note(int id, String description, LocalDateTime noteTimestamp,
                int issueId, int authorId, String authorType) {
        this.id = id;
        this.description = description;
        this.noteTimestamp = noteTimestamp;
        this.issueId = issueId;
        this.authorId = authorId;
        this.authorType = authorType;
    }

    public boolean isEmpty() {
        return description == null || description.trim().isEmpty();
    }

    public boolean wasWrittenBySupervisor() {
        return "SUPERVISOR".equalsIgnoreCase(authorType);
    }

    public boolean wasWrittenBySupporter() {
        return "SUPPORTER".equalsIgnoreCase(authorType);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public LocalDateTime getNoteTimestamp() {
        return noteTimestamp;
    }

    public void setNoteTimestamp(LocalDateTime noteTimestamp) {
        this.noteTimestamp = noteTimestamp;
    }


    public int getIssueId() {
        return issueId;
    }

    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }


    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }


    public String getAuthorType() {
        return authorType;
    }

    public void setAuthorType(String authorType) {
        this.authorType = authorType;
    }

    @Override
    public String toString() {
        return "Note{" + "id=" + id + ", description=" + description
                + ", noteTimestamp=" + noteTimestamp + ", issueId=" + issueId
                + ", authorId=" + authorId + ", authorType=" + authorType + '}';
    }
}