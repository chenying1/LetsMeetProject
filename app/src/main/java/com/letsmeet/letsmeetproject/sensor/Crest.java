package com.letsmeet.letsmeetproject.sensor;

public class Crest {
    double value;
    long timestamp;

    public Crest(){}

    public Crest(Crest crest){
        this.value = crest.value;
        this.timestamp = crest.timestamp;
    }

    public Crest(double value, long timestamp){
        this.value = value;
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
