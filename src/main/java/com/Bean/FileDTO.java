package com.Bean;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.DateTime;

import java.io.Serializable;

public class FileDTO implements Serializable {

    private static final long serialVersionUID = 8201171847853428168L;

    private Long id;
    private String fileName;
    private DateTime backUpDate;
    private byte[] content;

    public FileDTO() {
    }

    public FileDTO(Long id, String fileName) {
        this(id, fileName, null, null);
    }

    public FileDTO(Long id, DateTime backUpDate) {
        this(id, null, null, backUpDate);
    }

    public FileDTO(String fileName, byte[] content) {
        this(fileName, content, null);
    }

    public FileDTO(String fileName, byte[] content, DateTime backUpDate) {
        this(null, fileName, content, backUpDate);
    }

    public FileDTO(Long id, String fileName, byte[] content, DateTime backUpDate) {
        this.id = id;
        this.fileName = fileName;
        this.content = content;
        this.backUpDate = backUpDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContent() {
        return content;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public DateTime getBackUpDate() {
        return backUpDate;
    }

    public void setBackUpDate(DateTime backUpDate) {
        this.backUpDate = backUpDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("fileName", fileName)
                .append("content", content)
                .append("backUpDate", backUpDate)
                .toString();
    }
}
