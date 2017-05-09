package com.myntra.memorygame.networking;

import com.myntra.memorygame.models.responsemodels.FlickrApiResponseModel;

import retrofit2.http.GET;
import rx.Observable;
import retrofit2.http.Query;

/**
 * Created by kapilbakshi on 07/05/17.
 */

public interface NetworkApis {

    @GET("/services/feeds/photos_public.gne")
    Observable<FlickrApiResponseModel> getImages(@Query("format") String format,
                                                 @Query("nojsoncallback") boolean jsonCallBack);
}
