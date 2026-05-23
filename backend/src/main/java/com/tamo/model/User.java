package com.tamo.model;

/**
 * Represents a User entity in the system.
 */
public class User {
    /**
     * The id of the entity.
     */
    private String id;
    /**
     * The name of the entity.
     */
    private String name;
    /**
     * The email of the entity.
     */
    private String email;
    /**
     * The password of the entity.
     */
    private String password;
    /**
     * The phone of the entity.
     */
    private String phone;
    private String role;       // applicant | organiser | admin
    /**
     * The department of the entity.
     */
    private String department;
    /**
     * The avatar of the entity.
     */
    private String avatar;
    private String status;     // active | suspended | inactive
    /**
     * The lastLogin of the entity.
     */
    private String lastLogin;
    /**
     * The createdAt of the entity.
     */
    private String createdAt;

    /**
     * Gets the id.
     * @return the id
     */
    public String getId() { return id; }
    /**
     * Sets the id.
     * @param id the id to set
     */
    public void setId(String id) { this.id = id; }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() { return name; }
    /**
     * Sets the name.
     * @param name the name to set
     */
    public void setName(String name) { this.name = name; }

    /**
     * Gets the email.
     * @return the email
     */
    public String getEmail() { return email; }
    /**
     * Sets the email.
     * @param email the email to set
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Gets the password.
     * @return the password
     */
    public String getPassword() { return password; }
    /**
     * Sets the password.
     * @param password the password to set
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Gets the phone.
     * @return the phone
     */
    public String getPhone() { return phone; }
    /**
     * Sets the phone.
     * @param phone the phone to set
     */
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * Gets the role.
     * @return the role
     */
    public String getRole() { return role; }
    /**
     * Sets the role.
     * @param role the role to set
     */
    public void setRole(String role) { this.role = role; }

    /**
     * Gets the department.
     * @return the department
     */
    public String getDepartment() { return department; }
    /**
     * Sets the department.
     * @param department the department to set
     */
    public void setDepartment(String department) { this.department = department; }

    /**
     * Gets the avatar.
     * @return the avatar
     */
    public String getAvatar() { return avatar; }
    /**
     * Sets the avatar.
     * @param avatar the avatar to set
     */
    public void setAvatar(String avatar) { this.avatar = avatar; }

    /**
     * Gets the status.
     * @return the status
     */
    public String getStatus() { return status; }
    /**
     * Sets the status.
     * @param status the status to set
     */
    public void setStatus(String status) { this.status = status; }

    /**
     * Gets the lastlogin.
     * @return the lastlogin
     */
    public String getLastLogin() { return lastLogin; }
    /**
     * Sets the lastlogin.
     * @param lastLogin the lastlogin to set
     */
    public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }

    /**
     * Gets the createdat.
     * @return the createdat
     */
    public String getCreatedAt() { return createdAt; }
    /**
     * Sets the createdat.
     * @param createdAt the createdat to set
     */
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
