package com.Bean;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Component
public class ScanSettings implements Serializable {

    private static final long serialVersionUID = -1072499244609887553L;

    private long scanTimeOut;
    private TimeUnit timeUnit;
    private String workDirectory;
    private String fileExtension;
    private volatile Locale locale;

    public ScanSettings() {
    }

    public long getScanTimeOut() {
        return scanTimeOut;
    }

    public void setScanTimeOut(long scanTimeOut) {
        this.scanTimeOut = scanTimeOut;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public String getWorkDirectory() {
        return workDirectory;
    }

    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public long getScanTimeOutInMills() {
        return timeUnit.toMillis(scanTimeOut);
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("scanTimeOut", scanTimeOut)
                .append("workDirectory", workDirectory)
                .append("fileExtension", fileExtension)
                .append("locale", locale)
                .toString();
    }
}
