package com.retailsonar.retailsonar.entities;

/**
 *
 * Pand ~ identiek aan serverside pand entity
 * Created by aaron on 3/24/2018.
 */

public class Pand {

    private long id;

    private String straat;

    private String land;

    private String postcode;

    private String provincie;

    private String stad;

    private String winkel;

    private String commercieleActiviteit;

    private double oppervlakte;

    private double lengtevoorgevel;

    private int parking;

    private int education;

    private int publiekTransport;

    private double lat;
    private double longi;

    private byte[] afbeelding;

    private	int bouwjaar;

    private int passage;

    private int toegankelijkheid;

    private int microtoegankelijkheid;

    private int shopareaappreciation;

    private int correctiefactor;

    private int lokaalmonopolie;

    private int completed;


    public Pand(long id, String straat, String land, String postcode, String provincie, String stad, String winkel, String commercieleActiviteit, double oppervlakte, double lengtevoorgevel, int parking, int education, int publiekTransport, double lat, double longi, byte[] afbeelding, int bouwjaar, int passage, int toegankelijkheid, int microtoegankelijkheid, int shopareaappreciation, int correctiefactor, int lokaalmonopolie, int completed) {
        this.id = id;
        this.straat = straat;
        this.land = land;
        this.postcode = postcode;
        this.provincie = provincie;
        this.stad = stad;
        this.winkel = winkel;
        this.commercieleActiviteit = commercieleActiviteit;
        this.oppervlakte = oppervlakte;
        this.lengtevoorgevel = lengtevoorgevel;
        this.parking = parking;
        this.education = education;
        this.publiekTransport = publiekTransport;
        this.lat = lat;
        this.longi = longi;
        this.afbeelding = afbeelding;
        this.bouwjaar = bouwjaar;
        this.passage = passage;
        this.toegankelijkheid = toegankelijkheid;
        this.microtoegankelijkheid = microtoegankelijkheid;
        this.shopareaappreciation = shopareaappreciation;
        this.correctiefactor = correctiefactor;
        this.lokaalmonopolie = lokaalmonopolie;
        this.completed=completed;
    }

    // copyconstructor
    public Pand(Pand pand) {
        this.id = pand.id;
        this.straat = pand.straat;
        this.land = pand.land;
        this.postcode = pand.postcode;
        this.provincie = pand.provincie;
        this.stad = pand.stad;
        this.winkel = pand.winkel;
        this.oppervlakte = pand.oppervlakte;
        this.lengtevoorgevel = pand.lengtevoorgevel;
        this.parking = pand.parking;
        this.commercieleActiviteit=pand.commercieleActiviteit;
        this.publiekTransport=pand.publiekTransport;
        this.afbeelding=pand.afbeelding;
        this.lat=pand.lat;
        this.longi=pand.longi;
        this.bouwjaar = pand.bouwjaar;
        this.passage = pand.passage;
        this.toegankelijkheid = pand.toegankelijkheid;
        this.microtoegankelijkheid = pand.microtoegankelijkheid;
        this.shopareaappreciation = pand.shopareaappreciation;
        this.correctiefactor = pand.correctiefactor;
        this.lokaalmonopolie = pand.lokaalmonopolie;
        this.completed=pand.completed;
    }

    // getters en setters


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }




    public byte[] getAfbeelding() {
        return afbeelding;
    }

    public void setAfbeelding(byte[] afbeelding) {
        this.afbeelding = afbeelding;
    }


    public int getCompleted() {
        return completed;
    }

    public void setCompleted(int completed) {
        this.completed = completed;
    }

    public double getOppervlakte() {
        return oppervlakte;
    }


    public void setOppervlakte(double oppervlakte) {
        this.oppervlakte = oppervlakte;
    }


    public double getLengtevoorgevel() {
        return lengtevoorgevel;
    }


    public void setLengtevoorgevel(double lengtevoorgevel) {
        this.lengtevoorgevel = lengtevoorgevel;
    }


    public int getBouwjaar() {
        return bouwjaar;
    }

    public void setBouwjaar(int bouwjaar) {
        this.bouwjaar = bouwjaar;
    }

    public int getPassage() {
        return passage;
    }

    public void setPassage(int passage) {
        this.passage = passage;
    }

    public int getToegankelijkheid() {
        return toegankelijkheid;
    }

    public void setToegankelijkheid(int toegankelijkheid) {
        this.toegankelijkheid = toegankelijkheid;
    }

    public int getMicrotoegankelijkheid() {
        return microtoegankelijkheid;
    }

    public void setMicrotoegankelijkheid(int microtoegankelijkheid) {
        this.microtoegankelijkheid = microtoegankelijkheid;
    }

    public int getShopareaappreciation() {
        return shopareaappreciation;
    }

    public void setShopareaappreciation(int shopareaappreciation) {
        this.shopareaappreciation = shopareaappreciation;
    }

    public int getCorrectiefactor() {
        return correctiefactor;
    }

    public void setCorrectiefactor(int correctiefactor) {
        this.correctiefactor = correctiefactor;
    }

    public int getLokaalmonopolie() {
        return lokaalmonopolie;
    }

    public void setLokaalmonopolie(int lokaalmonopolie) {
        this.lokaalmonopolie = lokaalmonopolie;
    }

    public String getCommercieleActiviteit() {
        return commercieleActiviteit;
    }




    public void setCommercieleActiviteit(String commercieleActiviteit) {
        this.commercieleActiviteit = commercieleActiviteit;
    }




    public void setId(Long id) {
        this.id = id;
    }

    public String getWinkel() {
        return winkel;
    }

    public void setWinkel(String winkel) {
        this.winkel = winkel;
    }

    public String getStraat() {
        return straat;
    }

    public void setStraat(String straat) {
        this.straat = straat;
    }

    public String getLand() {
        return land;
    }

    public void setLand(String land) {
        this.land = land;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public String getProvincie() {
        return provincie;
    }

    public void setProvincie(String provincie) {
        this.provincie = provincie;
    }

    public String getStad() {
        return stad;
    }

    public void setStad(String stad) {
        this.stad = stad;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLongi() {
        return longi;
    }

    public void setLongi(double longi) {
        this.longi = longi;
    }

    public int getEducation() {
        return education;
    }


    public void setEducation(int education) {
        this.education = education;
    }

    public int getPubliekTransport(){
        return publiekTransport;
    }

    public void setPubliekTransport(int publiekTransport){
        this.publiekTransport=publiekTransport;
    }


    public int getParking() {
        return parking;
    }




    public void setParking(int parking) {
        this.parking = parking;
    }


    // eindresultaat in meter, afstand tussen pand en gegeven coordinaten
    public double distance( double lat2,
                                  double lon2) {

        final int R = 6371; // Radius of the earth
        double lat1=this.lat;
        double lon1=this.longi;

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters



        return distance;
    }



}
