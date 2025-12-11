package Model;

public class ContactInfo {
    public String email;
    public String phone;
    public Address address;

    public ContactInfo() {}

    public ContactInfo(String email, String phone, Address address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}