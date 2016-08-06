package com.example.prest.simpletodo;

public class simpleDateString {

    private String month, day, year;

    public simpleDateString(String month, String day, String year) {
        this.month = month;
        this.day = day;
        this.year = year;
    }

    /*
    format as 11/11/2011
     */
    public simpleDateString(String date) {
        String[] values;
        try{
            values = date.split("/");
            this.month = values[0];
            this.day = values[1];
            this.year = values[2];
        } catch (Exception n){
            this.month = "00";
            this.day = "00";
            this.year = "0000";
        }
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return month + "/" + day + "/" + year;
    }
}
