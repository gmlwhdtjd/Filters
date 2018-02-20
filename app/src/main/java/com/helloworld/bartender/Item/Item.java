package com.helloworld.bartender.Item;

/**
 * Created by 김현식 on 2018-02-05.
 */

public class Item {
    private long id;
    private String filter_name;
    private float blur;
    private float focus;
    private float aberation;
    private float noiseSize;

    public Item(){

    }

    public Item(long id, String filter_name, float blur, float focus, float aberation, float noiseSize, float noiseIntensity) {
        this.id = id;
        this.filter_name = filter_name;
        this.blur = blur;
        this.focus = focus;
        this.aberation = aberation;
        this.noiseSize = noiseSize;
        this.noiseIntensity = noiseIntensity;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFilter_name() {
        return filter_name;
    }

    public void setFilter_name(String filter_name) {
        this.filter_name = filter_name;
    }

    public float getBlur() {
        return blur;
    }

    public void setBlur(float blur) {
        this.blur = blur;
    }

    public float getFocus() {
        return focus;
    }

    public void setFocus(float focus) {
        this.focus = focus;
    }

    public float getAberation() {
        return aberation;
    }

    public void setAberation(float aberation) {
        this.aberation = aberation;
    }

    public float getNoiseSize() {
        return noiseSize;
    }

    public void setNoiseSize(float noiseSize) {
        this.noiseSize = noiseSize;
    }

    public float getNoiseIntensity() {
        return noiseIntensity;
    }

    public void setNoiseIntensity(float noiseIntensity) {
        this.noiseIntensity = noiseIntensity;
    }

    private float noiseIntensity;




}
