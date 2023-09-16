package com.dinhtc.taskmaster.common.widgets.image_picker;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static android.util.Config.DEBUG;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.ViewCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.dinhtc.taskmaster.BuildConfig;
import com.dinhtc.taskmaster.R;
import com.dinhtc.taskmaster.utils.DialogFactory;
import com.dinhtc.taskmaster.utils.Utils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BSVideoPicker extends BottomSheetDialogFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int LOADER_ID = 1000;
    private static final int PERMISSION_READ_STORAGE = 2001;
    private static final int PERMISSION_WRITE_STORAGE = 2003;
    private static final int REQUEST_SELECT_FROM_VIDEO = 3003;
    private static final int REQUEST_TAKE_VIDEO = 3004;

    //Views
    private RecyclerView recyclerView;
    private View bottomBarView;
    private TextView tvDone, tvMultiSelectMessage, tvEmptyView;
    private BottomSheetBehavior bottomSheetBehavior;
    //Components
    private VideoTileAdapter adapter;
    private String tag = "";
    private VideoLoaderDelegate videoLoaderDelegate;
    //States
    private boolean isMultiSelection = false;
    private boolean dismissOnSelect = true;
    private Uri currentVideoUri;
    //Configurations
    private int maximumDisplayingVideo = Integer.MAX_VALUE;
    private int peekHeight = Utils.dp2px(360);
    private int maximumMultiSelectCount = Integer.MAX_VALUE;
    private String providerAuthority;
    private boolean showCameraTile = true;
    private boolean showGalleryTile = true;
    private int spanCount = 3;
    private int gridSpacing = Utils.dp2px(2);
    private int multiSelectBarBgColor = android.R.color.white;
    private int multiSelectTextColor = R.color.primary_text;
    private int multiSelectDoneTextColor = R.color.multiselect_done;
    private boolean isOpenFrontCamera = false;
    private OnSingleVideoSelectedListener onSingleVideoSelectedListener;
    public interface OnSingleVideoSelectedListener {
        void onSingleVideoSelected(Uri uri);
    }
    public interface VideoLoaderDelegate {
        void loadVideo(Uri imageUri, ImageView ivImage);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSingleVideoSelectedListener) {
            onSingleVideoSelectedListener = (OnSingleVideoSelectedListener) context;
        }
        if (context instanceof VideoLoaderDelegate) {
            videoLoaderDelegate = (VideoLoaderDelegate) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadConfigFromBuilder();
        if (Utils.isReadStorageGranted(getContext())) {
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, BSVideoPicker.this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Utils.checkPermission(BSVideoPicker.this, Manifest.permission.READ_MEDIA_IMAGES, PERMISSION_READ_STORAGE);
            } else {
                Utils.checkPermission(BSVideoPicker.this, Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_STORAGE);
            }
        }
        if (savedInstanceState != null) {
            currentVideoUri = savedInstanceState.getParcelable("currentVideoUri");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_imagepicker_sheet, container, false);

        if (getParentFragment() != null && getParentFragment() instanceof OnSingleVideoSelectedListener) {
            onSingleVideoSelectedListener = (OnSingleVideoSelectedListener) getParentFragment();
        }
        if (getParentFragment() != null && getParentFragment() instanceof VideoLoaderDelegate) {
            videoLoaderDelegate = (VideoLoaderDelegate) getParentFragment();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerView();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialog1 -> {
            //Get the BottomSheetBehavior
            BottomSheetDialog d = (BottomSheetDialog) dialog1;
            FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
                bottomSheetBehavior.setPeekHeight(peekHeight);
                bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                    @Override
                    public void onStateChanged(@NonNull View bottomSheet, int newState) {
                        switch (newState) {
                            case BottomSheetBehavior.STATE_HIDDEN:
                                dismiss();
                                break;
                        }
                    }

                    @Override
                    public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                        if (bottomBarView != null) {
                            bottomBarView.setAlpha(slideOffset < 0 ? (1 + slideOffset) : 1);
                        }
                    }
                });
            }
        });

        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
//        if (onSelectImageCancelledListener != null) {
//            onSelectImageCancelledListener.onCancelled(isMultiSelection, tag);
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && adapter != null) {
            List<Uri> savedUriList = savedInstanceState.getParcelableArrayList("selectedImages");
            if (savedUriList != null) {
                adapter.setSelectedFiles(savedUriList);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadConfigFromBuilder();
        if (Utils.isReadStorageGranted(getContext())) {
            LoaderManager.getInstance(this).initLoader(LOADER_ID, null, BSVideoPicker.this);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Utils.checkPermission(BSVideoPicker.this, Manifest.permission.READ_MEDIA_IMAGES, PERMISSION_READ_STORAGE);
            } else {
                Utils.checkPermission(BSVideoPicker.this, Manifest.permission.READ_EXTERNAL_STORAGE, PERMISSION_READ_STORAGE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Utils.isCameraGranted(getContext())) {
                        launchCameraForVideo();
                    } else {
                        Utils.checkPermission(this, Manifest.permission.CAMERA, PERMISSION_READ_STORAGE);
                    }
                }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TAKE_VIDEO:
                if (resultCode == RESULT_OK) {
                    // Khi quay video thành công và trả về kết quả
                    notifyGalleryVideo();
                    // Lấy URI của tệp video từ intent trả về
                    Log.e("SSSSSSSSSSS","videoUri: "+ currentVideoUri);

                    if (onSingleVideoSelectedListener != null) {
                        onSingleVideoSelectedListener.onSingleVideoSelected(currentVideoUri);
                        if (dismissOnSelect) dismiss();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    // Nếu người dùng hủy quay video
                    // Xử lý ở đây nếu cần thiết
                } else {
                    // Nếu có lỗi xảy ra khi quay video
                    // Xử lý lỗi ở đây nếu cần thiết
                }
                break;
            case REQUEST_SELECT_FROM_VIDEO:
                if (resultCode == RESULT_OK) {
                    try {
                        if (isVideoLengthValid(data.getData())) {
                            // Video có độ dài hợp lệ (<= 10 giây)
                            if (onSingleVideoSelectedListener != null) {
                                onSingleVideoSelectedListener.onSingleVideoSelected(data.getData());
                                if (dismissOnSelect) dismiss();
                            }
                        }else {
                            DialogFactory.createMessageDialogWithYesNo(
                                    getContext(),
                                    "Bạn chỉ được chọn video dưới 10s.",
                                    "Chọn Lại",
                                    "Hủy",
                                    () -> {
                                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                                        startActivityForResult(intent, REQUEST_SELECT_FROM_VIDEO);
                                        return null;
                                    },
                                    () -> {
                                        return null;
                                    }
                            );
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void notifyGalleryVideo() {
        if (getContext() == null) return;
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(currentVideoUri);
        getContext().sendBroadcast(mediaScanIntent);
    }

    private void bindViews(View rootView) {
        recyclerView = rootView.findViewById(R.id.picker_recyclerview);
        tvEmptyView = rootView.findViewById(R.id.tv_picker_empty_view);
    }

    private void setupRecyclerView() {
        GridLayoutManager gll = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(gll);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        recyclerView.addItemDecoration(new GridItemSpacingDecoration(spanCount, gridSpacing, false));
        if (adapter == null) {
            adapter = new VideoTileAdapter(getContext(),
                    videoLoaderDelegate,
                    isMultiSelection,
                    showCameraTile,
                    showGalleryTile);
            adapter.setMaximumSelectionCount(maximumMultiSelectCount);

            adapter.setVideoClickListener(v -> launchCameraForVideo());

            adapter.setGalleryTileOnClickListener(v -> {
                if (!isMultiSelection) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, REQUEST_SELECT_FROM_VIDEO);
                }
            });

            adapter.setImageTileOnClickListener(v -> {
                if (v.getTag() != null && v.getTag() instanceof Uri && onSingleVideoSelectedListener != null) {
                    onSingleVideoSelectedListener.onSingleVideoSelected((Uri) v.getTag());
                    if (dismissOnSelect) dismiss();
                }
            });
        }
        recyclerView.setAdapter(adapter);
    }

    private void launchCameraForVideo() {
        if (getContext() == null) return;
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getContext().getPackageManager()) != null) {
            File videoFile = null;
            try {
                videoFile = createVideoFile();
            } catch (IOException e) {
                if (DEBUG) e.printStackTrace();
            }
            if (videoFile != null) {
                Uri videoURI = FileProvider.getUriForFile(requireContext(),
                        BuildConfig.APPLICATION_ID + ".provider",
                        videoFile);
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoURI);
                if (isOpenFrontCamera) {
                    // Add any necessary extra parameters to open the front-facing camera (if possible)
                    // Similar to what was done in the image capture case.
                    // ...
                }
                List<ResolveInfo> resolvedIntentActivities = getContext().getPackageManager().queryIntentActivities(takeVideoIntent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolvedIntentInfo : resolvedIntentActivities) {
                    String packageName = resolvedIntentInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, videoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivityForResult(takeVideoIntent, REQUEST_TAKE_VIDEO);
            }
        }
    }

    private File createVideoFile() throws IOException {
        // Thư mục lưu trữ video (đường dẫn tùy chỉnh)
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), "YourVideoFolder");

        // Tạo thư mục nếu nó chưa tồn tại
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        // Tạo tên tệp video duy nhất, có thể sử dụng timestamp để tạo tên duy nhất
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String videoFileName = "VIDEO_" + timeStamp + ".mp4";

        // Tạo tệp video mới
        File videoFile = new File(storageDir, videoFileName);
        currentVideoUri = Uri.fromFile(videoFile);
        // Trả về tệp để camera có thể lưu video vào đó
        return videoFile;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("selectedImages", (ArrayList<Uri>) adapter.selectedFiles);
        outState.putParcelable("currentVideoUri", currentVideoUri);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_ID && getContext() != null) {
            Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] projection = new String[]{MediaStore.Video.Media._ID};
            String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";
            return new CursorLoader(getContext(), uri, projection, null, null, sortOrder);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            List<Uri> uriList = new ArrayList<>();
            try {
                int index = 0;
                while (cursor.moveToNext() && index < maximumDisplayingVideo) {
                    int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                    Uri videoUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    if (isVideoLengthValid(videoUri)) {
                        uriList.add(videoUri);  // Add the valid video URI to the list
                    }
                    index++;
                }
            } catch (RuntimeException | IOException e) {
                e.printStackTrace();
            } finally {
                cursor.close();  // Close the cursor when done with it
            }
            adapter.setVideoList(uriList);

            if (uriList.isEmpty() && !showCameraTile && !showGalleryTile) {
                tvEmptyView.setVisibility(View.VISIBLE);
                if (bottomBarView != null) {
                    bottomBarView.setVisibility(View.GONE);
                }
            } else {
                tvEmptyView.setVisibility(View.INVISIBLE);
                if (bottomBarView != null) {
                    bottomBarView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private boolean isVideoLengthValid(Uri videoUri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(getContext(), videoUri);
        String durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        retriever.release();

        long duration = Long.parseLong(durationString != null ? durationString : "0");
        long maxDuration = 10000; // 10 giây trong milliseconds

        return duration <= maxDuration;
    }

    @Override
    public void onLoaderReset(Loader loader) {
        adapter.setVideoList(null);
    }

    private void loadConfigFromBuilder() {
        try {
            providerAuthority = getArguments().getString("providerAuthority");
            tag = getArguments().getString("tag");
            isMultiSelection = getArguments().getBoolean("isMultiSelect");
            dismissOnSelect = getArguments().getBoolean("dismissOnSelect");
            maximumDisplayingVideo = getArguments().getInt("maximumDisplayingImages");
            maximumMultiSelectCount = getArguments().getInt("maximumMultiSelectCount");
            if (isMultiSelection) {
                showCameraTile = false;
                showGalleryTile = false;
            } else {
                showCameraTile = getArguments().getBoolean("showCameraTile");
                showGalleryTile = getArguments().getBoolean("showGalleryTile");
            }
            spanCount = getArguments().getInt("spanCount");
            peekHeight = getArguments().getInt("peekHeight");
            gridSpacing = getArguments().getInt("gridSpacing");
            multiSelectBarBgColor = getArguments().getInt("multiSelectBarBgColor");
            multiSelectTextColor = getArguments().getInt("multiSelectTextColor");
            multiSelectDoneTextColor = getArguments().getInt("multiSelectDoneTextColor");
            isOpenFrontCamera = getArguments().getBoolean("isOpenFrontCamera");
        } catch (Exception e) {
            if (DEBUG) e.printStackTrace();
        }
    }

    public static class Builder {
        private String providerAuthority;
        private String tag;
        private boolean dismissOnSelect = true;
        private int maximumDisplayingImages = Integer.MAX_VALUE;
        private int minimumMultiSelectCount = 1;
        private int maximumMultiSelectCount = Integer.MAX_VALUE;
        private boolean showCameraTile = true;
        private boolean showGalleryTile = true;
        private int peekHeight = Utils.dp2px(360);
        private int spanCount = 3;
        private int gridSpacing = Utils.dp2px(2);
        private int multiSelectBarBgColor = android.R.color.white;
        private int multiSelectTextColor = R.color.primary_text;
        private int multiSelectDoneTextColor = R.color.multiselect_done;
        private boolean showOverSelectMessage = true;
        private int overSelectTextColor = R.color.error_text;
        private boolean isOpenFrontCamera = false;

        public Builder(String providerAuthority) {
            this.providerAuthority = providerAuthority;
        }


        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public BSVideoPicker build() {
            Bundle args = new Bundle();
            args.putString("providerAuthority", providerAuthority);
            args.putString("tag", tag);
            args.putBoolean("dismissOnSelect", dismissOnSelect);
            args.putInt("maximumDisplayingImages", maximumDisplayingImages);
            args.putInt("minimumMultiSelectCount", minimumMultiSelectCount);
            args.putInt("maximumMultiSelectCount", maximumMultiSelectCount);
            args.putBoolean("showCameraTile", showCameraTile);
            args.putBoolean("showGalleryTile", showGalleryTile);
            args.putInt("peekHeight", peekHeight);
            args.putInt("spanCount", spanCount);
            args.putInt("gridSpacing", gridSpacing);
            args.putInt("multiSelectBarBgColor", multiSelectBarBgColor);
            args.putInt("multiSelectTextColor", multiSelectTextColor);
            args.putInt("multiSelectDoneTextColor", multiSelectDoneTextColor);
            args.putBoolean("showOverSelectMessage", showOverSelectMessage);
            args.putInt("overSelectTextColor", overSelectTextColor);
            args.putBoolean("isOpenFrontCamera", isOpenFrontCamera);

            BSVideoPicker fragment = new BSVideoPicker();
            fragment.setArguments(args);
            return fragment;
        }

    }
}
