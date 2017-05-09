package com.myntra.memorygame.models.responsemodels;

import com.myntra.memorygame.models.datamodels.FlickrImageModel;
import com.squareup.moshi.Json;

import java.util.List;

public class FlickrApiResponseModel {

    @Json(name = "items")
    private List<FlickrImageModel> imagesList;

    public List<FlickrImageModel> getImagesList() {
        return imagesList;
    }
}
