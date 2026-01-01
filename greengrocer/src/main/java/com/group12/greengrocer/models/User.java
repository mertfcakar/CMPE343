package com.group12.greengrocer.models;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private String email;        // Yeni: ContactDetails yerine geldi
    private String phoneNumber;  // Yeni: ContactDetails yerine geldi
    private String neighborhood;

    // Boş Constructor
    public User() {}

    // Tam Parametreli Constructor
    public User(int id, String username, String password, String role, String address, String email, String phoneNumber, String neighborhood) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.neighborhood = neighborhood;
    }

    // GETTER & SETTER METOTLARI
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    
    // Eski kodların patlamaması için geçici bir uyumluluk metodu (İstersen kaldırabilirsin)
    public String getContactDetails() { return email + " / " + phoneNumber; }
}