package com.example.countify;

import java.util.List;

public class Album {
    private List<Image> images;

    private String imageUrl;

    public Album(List<Image> images) {
        this.images = images;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }
    public String getIconSizeImage() {
        for (Image img: images) {
            if (img.getHeight() == 300 && img.getWidth() == 300) {
                return img.getUrl();
            }
        }
        return null;
    }

}
