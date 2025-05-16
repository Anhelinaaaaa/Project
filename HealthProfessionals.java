package one;
import java.util.Objects;

public class HealthProfessional {
    private String name;
    private String profession;
    private String location;


    public HealthProfessional(String name, String profession, String location) 
    {
        this.name = name;
        this.profession = profession;
        this.location = location;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getProfession() {
        return profession;
    }

    public String getLocation() {
        return location;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() 
    {
        return "HealthProfessional{name='" + name + "', profession='" + profession + "', location='" + location + "'}";
    }


    @Override
    public int hashCode() {
        return Objects.hash(name, profession, location);
    }
}
