package com.myntra.memorygame.models.datamodels;

import com.squareup.moshi.Json;

public class Media {

    @Json(name = "m")
    private String link;
    public String getLink() {
        return link;
    }

}
