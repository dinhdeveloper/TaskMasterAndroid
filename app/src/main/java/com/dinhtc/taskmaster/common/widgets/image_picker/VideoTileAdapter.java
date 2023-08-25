package com.dinhtc.taskmaster.common.widgets.image_picker;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.dinhtc.taskmaster.R;
import com.dinhtc.taskmaster.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class VideoTileAdapter  extends RecyclerView.Adapter<VideoTileAdapter.BaseViewHolder> {

    private static final int VIEWTYPE_GALLERY = 102;
    private static final int VIEWTYPE_IMAGE = 103;
    private static final int VIEWTYPE_DUMMY = 104;
    private static final int VIEWTYPE_BOTTOM_SPACE = 105;
    private static final int VIEWTYPE_VIDEO = 106;

    protected Context context;
    protected List<Uri> imageList;
    protected boolean isMultiSelect;
    protected List<Uri> selectedFiles;
    protected int maximumSelectionCount = Integer.MAX_VALUE;
    protected int nonListItemCount;
    private boolean showCameraTile;
    private boolean showGalleryTile;
    private BSVideoPicker.VideoLoaderDelegate imageLoaderDelegate;

    private View.OnClickListener cameraTileOnClickListener;
    private View.OnClickListener galleryTileOnClickListener;
    private View.OnClickListener videoClickListener;
    private View.OnClickListener imageTileOnClickListener;
    private OnSelectedCountChangeListener onSelectedCountChangeListener;
    private OnOverSelectListener onOverSelectListener;
    public interface OnSelectedCountChangeListener {
        void onSelectedCountChange(int currentCount);
    }
    public interface OnOverSelectListener {
        void onOverSelect();
    }

    public VideoTileAdapter(Context context, BSVideoPicker.VideoLoaderDelegate imageLoaderDelegate, boolean isMultiSelect, boolean showCameraTile, boolean showGalleryTile) {
        super();
        this.context = context;
        this.isMultiSelect = isMultiSelect;
        selectedFiles = new ArrayList<>();
        this.showCameraTile = showCameraTile;
        this.showGalleryTile = showGalleryTile;
        this.imageLoaderDelegate = imageLoaderDelegate;
        if (isMultiSelect) {
            nonListItemCount = 0;
        } else {
            if (showCameraTile && showGalleryTile) {
                nonListItemCount = 2;
            } else if (showCameraTile || showGalleryTile) {
                nonListItemCount = 1;
            } else {
                nonListItemCount = 0;
            }
        }
    }

    @Override
    public VideoTileAdapter.BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
           case VIEWTYPE_VIDEO:
                return new VideoTileAdapter.VideoTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_picker_video_tile, parent, false));
            case VIEWTYPE_GALLERY:
                return new VideoTileAdapter.GalleryTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_picker_gallery_tile, parent, false));
            case VIEWTYPE_DUMMY:
                return new VideoTileAdapter.DummyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_picker_dummy_tile, parent, false));
            case VIEWTYPE_BOTTOM_SPACE:
                View view = new View(context);
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dp2px(48));
                view.setLayoutParams(lp);
                return new VideoTileAdapter.DummyViewHolder(view);
            default:
                return new ImageVideoTileViewHolder(LayoutInflater.from(context).inflate(R.layout.item_picker_video_selected, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(VideoTileAdapter.BaseViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        if (isMultiSelect) {
            return imageList == null ? 10 : nonListItemCount + imageList.size();
        } else {
            return imageList == null ? 10 : imageList.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (!isMultiSelect) {
            switch (position) {
                case 0:
                    if (showCameraTile) {
                        return VIEWTYPE_VIDEO;
                    } else if (showGalleryTile) {
                        return VIEWTYPE_GALLERY;
                    } else {
                        return imageList == null ? VIEWTYPE_DUMMY : VIEWTYPE_IMAGE;
                    }
                case 1:
                    return (showCameraTile && showGalleryTile) ? VIEWTYPE_GALLERY : (imageList == null ? VIEWTYPE_DUMMY : VIEWTYPE_IMAGE);
                default:
                    return imageList == null ? VIEWTYPE_DUMMY : VIEWTYPE_IMAGE;
            }
        } else {
            if (position == getItemCount() - 1) return VIEWTYPE_BOTTOM_SPACE;
            return imageList == null ? VIEWTYPE_DUMMY : VIEWTYPE_IMAGE;
        }
    }

    public void setSelectedFiles(List<Uri> selectedFiles) {
        this.selectedFiles = selectedFiles;
        notifyDataSetChanged();
        if (onSelectedCountChangeListener != null)
            onSelectedCountChangeListener.onSelectedCountChange(selectedFiles.size());
    }

    public void setVideoList(List<Uri> imageList) {
        this.imageList = imageList;
        notifyDataSetChanged();
    }

    public abstract static class BaseViewHolder extends RecyclerView.ViewHolder {

        public BaseViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void bind(int position);

    }

    public class CameraTileViewHolder extends BaseViewHolder {

        public CameraTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(cameraTileOnClickListener);
        }

        @Override
        public void bind(int position) {

        }
    }
    public class VideoTileViewHolder extends BaseViewHolder {

        public VideoTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(videoClickListener);
        }

        @Override
        public void bind(int position) {

        }
    }

    public class GalleryTileViewHolder extends BaseViewHolder {

        public GalleryTileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(galleryTileOnClickListener);
        }

        @Override
        public void bind(int position) {

        }
    }

    public class ImageVideoTileViewHolder extends BaseViewHolder {

        View darken;
        AppCompatImageView ivImage, ivTick;

        public ImageVideoTileViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.item_imageTile);
            darken = itemView.findViewById(R.id.imageTile_selected_darken);
            ivTick = itemView.findViewById(R.id.imageTile_selected);
            if (!isMultiSelect) {
                itemView.setOnClickListener(imageTileOnClickListener);
            } else {
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri thisFile = imageList.get(getAdapterPosition());
                        if (selectedFiles.contains(thisFile)) {
                            selectedFiles.remove(thisFile);
                            notifyItemChanged(getAdapterPosition());
                        } else {
                            if (selectedFiles.size() == maximumSelectionCount) {
                                if (onOverSelectListener != null)
                                    onOverSelectListener.onOverSelect();
                                return;
                            } else {
                                selectedFiles.add(thisFile);
                                notifyItemChanged(getAdapterPosition());
                            }
                        }
                        if (onSelectedCountChangeListener != null) {
                            onSelectedCountChangeListener.onSelectedCountChange(selectedFiles.size());
                        }
                    }
                });
            }
        }

        public void bind(int position) {
            if (imageList == null) return;
            Uri imageFile = imageList.get(position - nonListItemCount);
            itemView.setTag(imageFile);
            imageLoaderDelegate.loadVideo(imageFile, ivImage);
            darken.setVisibility(selectedFiles.contains(imageFile) ? View.VISIBLE : View.INVISIBLE);
            ivTick.setVisibility(selectedFiles.contains(imageFile) ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public class DummyViewHolder extends BaseViewHolder {


        public DummyViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void bind(int position) {

        }
    }

    public void setGalleryTileOnClickListener(View.OnClickListener galleryTileOnClickListener) {
        this.galleryTileOnClickListener = galleryTileOnClickListener;
    }
    public void setVideoClickListener(View.OnClickListener videoClickListener) {
        this.videoClickListener = videoClickListener;
    }

    public void setImageTileOnClickListener(View.OnClickListener imageTileOnClickListener) {
        this.imageTileOnClickListener = imageTileOnClickListener;
    }

    public void setMaximumSelectionCount(int maximumSelectionCount) {
        this.maximumSelectionCount = maximumSelectionCount;
    }

    public void setOnOverSelectListener(OnOverSelectListener onOverSelectListener) {
        this.onOverSelectListener = onOverSelectListener;
    }
}
