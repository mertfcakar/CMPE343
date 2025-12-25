package com.group12.greengrocer.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private String contactDetails;
    private String neighborhood;

    // BOŞ CONSTRUCTOR (Hata almamak için mutlaka dursun)
    public User() {}

    // TÜM PARAMETRELİ CONSTRUCTOR (UserDAO bunu kullanacak)
    public User(int id, String username, String password, String role, String address, String contactDetails, String neighborhood) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.contactDetails = contactDetails;
        this.neighborhood = neighborhood;
    }

    // GETTER VE SETTERLAR
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getContactDetails() { return contactDetails; }
    public void setContactDetails(String contactDetails) { this.contactDetails = contactDetails; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
}