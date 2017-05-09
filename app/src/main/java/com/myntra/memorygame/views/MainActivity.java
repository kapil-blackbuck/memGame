package com.myntra.memorygame.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.myntra.memorygame.CustomProgressDialog;
import com.myntra.memorygame.adapters.GameImagesAdapter;
import com.myntra.memorygame.R;
import com.myntra.memorygame.constants.Constants;
import com.myntra.memorygame.viewmodels.ViewModel;
import com.myntra.memorygame.viewmodels.ViewModel.GuessResultsStates;
import com.myntra.memorygame.databinding.ActivityMainBinding;
import com.squareup.picasso.Picasso;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

import static com.myntra.memorygame.constants.Constants.MILLI_SECS_TO_WAIT_BEFORE_SHOWING_NEXT_IMAGE;
import static com.myntra.memorygame.constants.Constants.NO_OF_GRID_COLUMNS;
import static com.myntra.memorygame.constants.Constants.SECONDS_TO_MEMORISE;


public class MainActivity extends AppCompatActivity {

    private GameImagesAdapter gameImagesAdapter;
    private CompositeSubscription subscriptions;
    ActivityMainBinding activityMainBinding;
    private ViewModel viewModel;
    private CustomProgressDialog mCustomProgressDialog;
    private int currentCountDownTimeValue = Constants.SECONDS_TO_MEMORISE;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModel();
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        gameImagesAdapter = new GameImagesAdapter(this, viewModel);
        bindViews();
        subscriptions = new CompositeSubscription();
        getImages();
        registerSubscriptions();
        setClickListeners();
    }

    private void setClickListeners() {
        activityMainBinding.contentMain.restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartGame();
            }
        });
    }

    private void bindViews() {
        activityMainBinding.contentMain.tilesRecyclerView.setLayoutManager(new GridLayoutManager(this, NO_OF_GRID_COLUMNS));
        activityMainBinding.contentMain.tilesRecyclerView.setAdapter(gameImagesAdapter);
    }

    private void getImages() {
        showProgressDialog(getResources().getString(R.string.loading));
        viewModel.getImages();
    }

    /**
     * Registering all subscriptions
     */
    private void registerSubscriptions() {
        subscriptions.addAll(

                /**
                 * Subscription when images data get fetched from the server {@link ViewModel#getImages()}
                 */
                viewModel.postsImagesFetchingObservable().observeOn(AndroidSchedulers.mainThread()).
                        subscribe(flickrImageModels -> {
                                    if (flickrImageModels.isEmpty()) {
                                        return;
                                    }
                                    gameImagesAdapter.setItems(flickrImageModels);
                                },
                                throwable -> {
                                    Toast.makeText(this,getResources().getString(R.string.no_internet),Toast.LENGTH_SHORT);
                                    hideProgressDialog();
                                }),
                /**
                 *   Subscription to get the result from computed by {@link ViewModel#evaluatePlayerTap} after
                 *   the person taps a tile
                 */
                viewModel.getGameResult().observeOn(AndroidSchedulers.mainThread()).
                        subscribe(guessState -> {
                            evaluateGameResult(guessState);
                        }),

                /**
                 *   Subscription to check whether all images have been successfully loaded or not {@link GameImagesAdapter#showImageOnTile}
                 */
                gameImagesAdapter.areAllImagesLoadedInGridView().subscribe(
                        status -> {
                            hideProgressDialog();
                            startCountDownTimer();
                            viewModel.setGameState(ViewModel.GameStates.TIMER_RUNNING);
                        },
                        throwable -> {
                            hideProgressDialog();
                        }
                ),

                /**
                 *   Subscription to get index of the tile tapped set in {@link GameImagesAdapter#onBindViewHolder}
                 */
                gameImagesAdapter.getClickedTileIndex().subscribe(
                        index -> {
                            viewModel.evaluatePlayerTap(index);
                        }
                )
        );
    }

    /**
     * Un subscribing all subscriptions
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscriptions.unsubscribe();
    }

    /**
     * {@code countDownTimer} is stopped and value of {@code currentCountDownTimeValue}
     */
    @Override
    protected void onPause() {
        super.onPause();
        if(viewModel.getGameState() == ViewModel.GameStates.TIMER_RUNNING) {
            countDownTimer.cancel();
        }
    }

    /**
     * Timer is started depending on the value of {@code currentCountDownTimeValue} only if the state of
     * the game is TIMER_RUNNING {@link ViewModel.GameStates}
     */
    @Override
    protected void onResume() {
        super.onResume();
        if(viewModel.getGameState() == ViewModel.GameStates.TIMER_RUNNING) {
           startCountDownTimer();
        }
    }

    /**
     * Starts the timer and maintains the value of {@code currentCountDownTimeValue} so that we can resume the timer
     * whenever the user pauses the app
     */
    private void startCountDownTimer() {
        setstatusTextView();
        countDownTimer = new CountDownTimer(currentCountDownTimeValue * 1000, 1000) {

            @Override
            public void onTick(long milliSecUntilFinished) {
                currentCountDownTimeValue = (int) (milliSecUntilFinished / 1000);
                activityMainBinding.contentMain.statusTextView.setText(Integer.toString(currentCountDownTimeValue));
            }

            @Override
            public void onFinish() {
                currentCountDownTimeValue = Constants.SECONDS_TO_MEMORISE;
                displayNewGuessImage();
            }
        };

        countDownTimer.start();
    }

    /**
     * See {@link GameImagesAdapter#toggleAllTiles(boolean)} for more information
     */
    private void hideTiles() {
        gameImagesAdapter.toggleAllTiles(true);
    }

    public void showProgressDialog(String message) {
        if (mCustomProgressDialog == null) {
            mCustomProgressDialog = new CustomProgressDialog(this, message);
        }
        if (!mCustomProgressDialog.isShowing()) {
            mCustomProgressDialog.setMessage(message);
            mCustomProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (mCustomProgressDialog != null && mCustomProgressDialog.isShowing() && !this.isFinishing()) {
            mCustomProgressDialog.dismiss();
        }
    }

    /**
     * Retrieves the state from {@link ViewModel#evaluatePlayerTap}
     * {@code CORRECT_GUESS} is the case when the player has tapped the correct tile, there will be a
     * {@link Constants#MILLI_SECS_TO_WAIT_BEFORE_SHOWING_NEXT_IMAGE} milliseconds pause
     * before showing the next image to guess
     * @param guessResultState
     *              is the result evaluated by the {@link ViewModel#evaluatePlayerTap(int)} depending upon tile clicked
     */
    private void evaluateGameResult(GuessResultsStates guessResultState) {

        switch (guessResultState) {

            case CORRECT_GUESS:
                viewModel.setGameState(ViewModel.GameStates.CORRECT_GUESS_PAUSE_STATE);
                setTextOnCorrectGuess();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        displayNewGuessImage();
                    }
                }, MILLI_SECS_TO_WAIT_BEFORE_SHOWING_NEXT_IMAGE);
                break;

            case WRONG_GUESS:
                setTextOnWrongGuess();
                break;

            case GAME_OVER:
                showGameOverDialog();
                break;
        }
    }

    /**
     * Will be called to assign an image to the image view which will hold the image to be gussed
     * @param tileIndex
     *              randomly generated index from the list holding the retrieved images
     */
    private void assignGuessingImageWithNewIndex(int tileIndex) {
        String imageLink = gameImagesAdapter.getItem(tileIndex).getMedia().getLink();
        Picasso.with(this)
                .load(imageLink)
                .placeholder(R.drawable.tile_background_placeholder)
                .into(activityMainBinding.contentMain.guessImageView);
        showGuessingImage();
    }

    private void showGuessingImage() {
        activityMainBinding.contentMain.guessImageView.setVisibility(View.VISIBLE);
    }

    /**
     * Will be called when the game gets over, i.e the player has flipped back all the tiles
     * The player would be presented with two options :- 1) to play again, 2) exit
     */
    public void showGameOverDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialogStyle);
        alertDialogBuilder.setNegativeButton(getResources().getString(R.string.play_again), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartGame();
            }
        });

        alertDialogBuilder.setPositiveButton(getResources().getString(R.string.exit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setMessage(getResources().getString(R.string.game_over_message));
        alertDialog.setTitle(getResources().getString(R.string.game_over_title));
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        if (!(MainActivity.this.isFinishing()) && alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    /**
     * Would be called when the player taps the correct tile
     * statusTextView's color would be changed to green and font size would get adjusted
     */
    private void setTextOnCorrectGuess() {
        activityMainBinding.contentMain.statusTextView.setText(getResources().getString(R.string.correct_guess_message));
        activityMainBinding.contentMain.statusTextView.setTextSize(getResources().getDimension(R.dimen.correct_guess_text_size));
        activityMainBinding.contentMain.statusTextView.setTextColor(Color.GREEN);
    }

    /**
     * Would be called when the player taps the wrong tile
     * statusTextView's color would be changed to red and font size would get adjusted
     */
    private void setTextOnWrongGuess() {
        activityMainBinding.contentMain.statusTextView.setText(getResources().getString(R.string.wrong_guess_message));
        activityMainBinding.contentMain.statusTextView.setTextSize(getResources().getDimension(R.dimen.wrong_guess_text_size));
        activityMainBinding.contentMain.statusTextView.setTextColor(Color.RED);
    }

    /**
     * Would be called when the timer has stopped and its player's turn to tap the tile
     * Will display the message which would instruct the user to guess tap the correct tile
     * statusTextView's color would be changed to black and font size would get adjusted
     */
    private void setGuessText() {
        activityMainBinding.contentMain.statusTextView.setText(getResources().getString(R.string.guessing_message));
        activityMainBinding.contentMain.statusTextView.setTextSize(getResources().getDimension(R.dimen.guess_text_size));
        activityMainBinding.contentMain.statusTextView.setTextColor(Color.BLACK);
    }

    /**
     * Would be called when the timer starts
     * Will display the updated timer's value
     * statusTextView's color would be changed to black and font size would get adjusted
     */
    private void setstatusTextView() {
        activityMainBinding.contentMain.statusTextView.setTextSize(getResources().getDimension(R.dimen.timer_text_size));
        activityMainBinding.contentMain.statusTextView.setTextColor(Color.BLACK);
    }

    /**
     * Restarting the game
     */
    private void restartGame() {
        startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }

    /**
     * Would be called to assign an image from the retrieved set of Images.
     * The player would have to tap the file having this image
     */
    private void displayNewGuessImage() {
        assignGuessingImageWithNewIndex(viewModel.setAndReturnRandomImageIndex());
        viewModel.clearGuessedTilesList();
        hideTiles();
        setGuessText();
        viewModel.setGameState(ViewModel.GameStates.GUESSING_TIME);

    }
}
