package com.myntra.memorygame.models.datamodels;

import com.squareup.moshi.Json;

/**
 * Created by kapilbakshi on 08/05/17.
 */

public class Media {

    @Json(name = "m")
    private String link;
    public String getLink() {
        return link;
    }

}
