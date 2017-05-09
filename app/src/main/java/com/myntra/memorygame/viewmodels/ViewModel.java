package com.myntra.memorygame.viewmodels;

import android.widget.Toast;

import utils.Utility;
import com.myntra.memorygame.models.datamodels.FlickrImageModel;
import com.myntra.memorygame.models.responsemodels.FlickrApiResponseModel;
import com.myntra.memorygame.networking.NetworkApis;
import com.myntra.memorygame.networking.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Retrofit;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import com.myntra.memorygame.constants.Constants;
import static com.myntra.memorygame.constants.Constants.IMAGE_GRID_SIZE;
import static com.myntra.memorygame.constants.Constants.JSON_FORMAT;


public class ViewModel {

    private Retrofit retrofit;
    private NetworkApis networkApis;
    private PublishSubject<List<FlickrImageModel>> imagesSubject = PublishSubject.create();
    private PublishSubject<GuessResultsStates> guessResultSubject = PublishSubject.create();
    private int randomImageIndex;
    private List<Integer> imagesClickedList = new ArrayList<Integer>();
    public static enum GuessResultsStates { CORRECT_GUESS, WRONG_GUESS, GAME_OVER };
    public static enum GameStates { BOARD_LOADING, TIMER_RUNNING, GUESSING_TIME,CORRECT_GUESS_PAUSE_STATE };
    private GameStates gameState;

    public ViewModel() {
        this.retrofit = RetrofitClient.getInstance();
        this.networkApis = retrofit.create(NetworkApis.class);
    }

    /**
     * Retrieving all image data from
     * @see <a href="https://api.flickr.com/services/feeds/photos_public.gne?format=json&nojsoncallback=1"> Flickr Api link/a>
     */
    public void getImages()
    {
        setGameState(GameStates.BOARD_LOADING);
        networkApis.getImages(JSON_FORMAT,true).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FlickrApiResponseModel>() {
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        imagesSubject.onError(e);
                    }
                    @Override
                    public void onNext(FlickrApiResponseModel flickrApiResponseModel) {
                        imagesSubject.onNext(flickrApiResponseModel.getImagesList().subList(0, IMAGE_GRID_SIZE));
                    }
                });

    }

    /**
     * Relies on {@code imagesSubject} which is Rx-Java PublishObject . Whenever we do onNext() on {@code imagesSubject}
     * this will transmit data to its subscriber
     * @return an observable containing the list of image data
     */
    public Observable<List<FlickrImageModel>> postsImagesFetchingObservable()
    {
        return imagesSubject;
    }

    /**
     * Relies on {@code guessResultSubject} whose value is set in {@link #evaluatePlayerTap}
     * @return an observable containing the tap result
     */
    public Observable<GuessResultsStates> getGameResult()
    {
        return guessResultSubject;
    }

    /**
     * Randomly selects a integer between 0 and {@link Constants#IMAGE_GRID_SIZE -1}
     * @return the random number generated above
     */
    public int setAndReturnRandomImageIndex() {
        randomImageIndex = Utility.getRandomNumber(0, IMAGE_GRID_SIZE -1);
        return randomImageIndex;
    }

    /**
     * Matches image of tile which was clicked with the image displayed
     * @param position
     *          is the index of the tile clicked
     */
    public void evaluatePlayerTap(int position) {
        if(position == randomImageIndex && imagesClickedList.size() != IMAGE_GRID_SIZE - 1) {
            guessResultSubject.onNext(GuessResultsStates.CORRECT_GUESS);
        }

        else {
            guessResultSubject.onNext(GuessResultsStates.WRONG_GUESS);
            if(!imagesClickedList.contains(position)) {
                imagesClickedList.add(position);
                if(imagesClickedList.size() == IMAGE_GRID_SIZE) {
                    guessResultSubject.onNext(GuessResultsStates.GAME_OVER);
                }
            }
        }

    }

    /**
     * Once the player has tapped the correct tile {@code imagesClickedList} is cleared
     * {@code imagesClickedList} contains tile indices which already have been clicked
     * while guessing a particular image
     */
    public void clearGuessedTilesList() {
        imagesClickedList.clear();
    }

    public GameStates getGameState() {
        return gameState;
    }

    public void setGameState(GameStates gameState) {
        this.gameState = gameState;
    }

}
