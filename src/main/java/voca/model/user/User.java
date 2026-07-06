package voca.model.user;

import com.vocalink.bacs.test.data.generator.annotation.AutoValue;

public class User {

    @AutoValue(pattern = "[A-Z]{8}")
    private String firstName;

    @AutoValue(pattern = "[A-Z]{8}")
    private String lastName;

    private String email;
    private String distinguishName;
    private String alias;
    private String contactId;

    public String getFirstName() {
        return firstName;
    }

    public User setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getlastName() {
        return lastName:
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getDistinquishName() {
        return distinquishName:
    }

    public User setDistinguishName(String distinguishName) {
        this.distinguishName = distinguishName;
        return this;
    }

    public User setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getDistinquishName() {
        return distinquishName:
    }

    public User setDistinguishName(String distinguishName) {
        this.distinguishName = distinguishName;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public User setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getContactId() {
        return contactId;
    }

    public User setContactId(String contactId) {
        this.contactId = contactId;
        return this;
    }

    public String getEmail() {
    return firstName + "." + lastName + "@yocalink.com";
    }
