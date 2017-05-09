package com.myntra.memorygame.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.myntra.memorygame.constants.Constants;
import com.myntra.memorygame.R;
import com.myntra.memorygame.viewmodels.ViewModel;
import com.myntra.memorygame.models.datamodels.FlickrImageModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;


public class GameImagesAdapter extends RecyclerView.Adapter<GameImagesAdapter.TileImageViewHolder>{

    private List<FlickrImageModel> imagesList = new ArrayList<>();
    private PublishSubject allImagesLoadedPublishSubject = PublishSubject.create();
    private PublishSubject<Integer> tileIndexPublishSubject = PublishSubject.create();
    private int imageLoadingCounter;
    private ViewModel viewModel;
    private Context context;

    public GameImagesAdapter(Context context, ViewModel viewModel) {
        this.context = context;
        this.viewModel = viewModel;
    }
    @Override
    public TileImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tile_layout, parent, false);
        return new TileImageViewHolder(view);
    }

    @SuppressLint("NewApi")
    @Override
    public void onBindViewHolder(TileImageViewHolder holder, int position) {

        FlickrImageModel imageModel = getItem(position);

        holder.getCellView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(viewModel.getGameState() == ViewModel.GameStates.GUESSING_TIME) {
                    showImageOnTile(imageModel,holder.getTileImageView());
                    tileIndexPublishSubject.onNext(position);
                }
            }
        });

        if (imageModel.isHideImage()) {
            hideImageOnTile(holder.getTileImageView());

        } else {
            showImageOnTile(imageModel,holder.getTileImageView());
        }
    }

    @Override
    public int getItemCount()
    {
        return imagesList.size();
    }
    public FlickrImageModel getItem(int position)
    {
        return imagesList.get(position);
    }

    class TileImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView tileImageView;
        private View cellView;
        TileImageViewHolder(View rowView) {
            super(rowView);
            tileImageView = (ImageView) rowView.findViewById(R.id.tile_image_view);
            this.cellView = rowView;
        }

        public ImageView getTileImageView() {
            return tileImageView;
        }
        public View getCellView() {return cellView;}
    }

    public void setItems(List<FlickrImageModel> imageModelList)
    {
        if (imageModelList == null)
        {
            return;
        }
        this.imagesList = new ArrayList<>(imageModelList);
        notifyDataSetChanged();
    }

    public void toggleAllTiles(boolean hideAllTiles) {
        for(FlickrImageModel flickrImageMode:imagesList) {
            flickrImageMode.setHideImage(hideAllTiles);
        }
        notifyDataSetChanged();
    }

    public Observable areAllImagesLoadedInGridView() {
        return allImagesLoadedPublishSubject;
    }

    public Observable<Integer> getClickedTileIndex() {
        return tileIndexPublishSubject;
    }

    private void showImageOnTile(FlickrImageModel imageModel,ImageView imageView) {

        Picasso.with(context)
                .load(imageModel.getMedia().getLink())
                .placeholder(R.drawable.tile_background_placeholder)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        if(viewModel.getGameState() != ViewModel.GameStates.BOARD_LOADING) {
                            return;
                        }
                        imageLoadingCounter++;
                        if(imageLoadingCounter == Constants.IMAGE_GRID_SIZE) {
                            allImagesLoadedPublishSubject.onNext(true);
                        }
                    }

                    @Override
                    public void onError() {
                        allImagesLoadedPublishSubject.onError(new Throwable());
                    }
                });
    }

    private void hideImageOnTile(ImageView imageView) {
        Picasso.with(context)
                .load(R.drawable.tile_background_placeholder)
                .into(imageView);
    }

}
