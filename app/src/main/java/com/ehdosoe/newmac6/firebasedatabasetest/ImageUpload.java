package com.ehdosoe.newmac6.firebasedatabasetest;

public class ImageUpload {
    public String name;
    public String url;

    public ImageUpload(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
