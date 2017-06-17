package models;

/**
 * Created by PaulM on 17/06/2017.
 */
// Might have to change arrays to arraylists
public class Country {
    private String name;
    private String capital;
    private String region;
    private String demonym;
    private String currencies;
    private String languages;
    private Double lat;
    private Double lng;

    public Country(){}

    public Country(String name, String capital, String region, String demonym, String currencies, String languages, Double lat, Double lng) {
        this.name = name;
        this.capital = capital;
        this.region = region;
        this.demonym = demonym;
        this.currencies = currencies;
        this.languages = languages;
        this.lat = lat;
        this.lng = lng;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDemonym() {
        return demonym;
    }

    public void setDemonym(String demonym) {
        this.demonym = demonym;
    }

    public String getCurrencies() {
        return currencies;
    }

    public void setCurrencies(String currencies) {
        this.currencies = currencies;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
