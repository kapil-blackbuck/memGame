package com.myntra.memorygame.models.datamodels;

import com.squareup.moshi.Json;

/**
 * Created by kapilbakshi on 07/05/17.
 */

public class FlickrImageModel {

    private Media media;
    private boolean hideImage;

    public Media getMedia() {
        return media;
    }

    public boolean isHideImage() {
        return hideImage;
    }

    public void setHideImage(boolean hideImage) {
        this.hideImage = hideImage;
    }

}


