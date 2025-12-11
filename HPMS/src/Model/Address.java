package Model;

public class Address {
    public String line1;
    public String line2;
    public String city;
    public String state;
    public String postalCode;
    public String country;

    public Address() {}
    public Address(String line1, String line2, String city, String state, String postalCode, String country) {
        this.line1 = line1;
        this.line2 = line2;
        this.city = city;
        this.state = state;
        this.postalCode = postalCode;
        this.country = country;
    }
}