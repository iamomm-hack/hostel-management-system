package hotelmanagement.model;

import java.io.Serializable;

/** A hotel guest. userId is 0 for walk-in customers who have no login. */
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;

    private int customerId;
    private int userId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private String idProof;

    public Customer() {
    }

    public Customer(String fullName, String phone, String email, String address, String idProof) {
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.idProof = idProof;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    @Override
    public String toString() {
        return customerId + " - " + fullName;
    }
}
