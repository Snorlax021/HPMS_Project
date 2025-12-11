package DTO;

public class ContactInfoDTO {
    private String email;
    private String phone;
    private AddressDTO address;

    public ContactInfoDTO() {}

    public ContactInfoDTO(String email, String phone, AddressDTO address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public AddressDTO getAddress() { return address; }
    public void setAddress(AddressDTO address) { this.address = address; }
}
