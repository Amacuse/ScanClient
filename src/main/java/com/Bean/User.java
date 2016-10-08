package com.Bean;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class User implements Serializable {
    private static final long serialVersionUID = -4276871359547291944L;

    private Long id;
    private String name;
    private String email;
    private LocalDate birthday;
    private char[] password;

    public User() {
    }

    public User(String name, String email, String birthday) {
        this(null, name, email, birthday);
    }

    public User(Long id, String name, String email, String birthday) {
        this(id, name, email, birthday, null);
    }

    public User(Long id, String name, String email, String birthday, char[] password) {
        this.id = id;
        this.name = name;
        this.email = email;
        if (birthday == null || birthday.isEmpty()) {
            this.birthday = null;
        } else {
            this.birthday = LocalDate.parse(birthday);
        }
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("name", name)
                .append("email", email)
                .append("birthday", birthday)
                .toString();
    }
}
