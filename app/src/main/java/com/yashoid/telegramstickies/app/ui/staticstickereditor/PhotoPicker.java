package com.yashoid.telegramstickies.app.ui.staticstickereditor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.office.task.TaskManager;
import com.yashoid.telegramstickies.app.BitmapLoader;
import com.yashoid.telegramstickies.app.ui.BottomSheetFragment;
import com.yashoid.telegramstickies.app.ui.widget.RoundedCornersImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoPicker extends BottomSheetFragment {

    private static final float MARGIN_HORIZONTAL = 16;
    private static final float ITEM_PADDING = 6;

    public interface OnPhotoSelectedListener {

        void onPhotoSelected(Uri uri);

    }

    private final ActivityResultLauncher<String> mRequestPermissionLauncher;

    private OnPhotoSelectedListener mOnPhotoSelectedListener = null;

    private RecyclerView mList;
    private PhotoAdapter mAdapter = null;

    private Uri mSelectedPhoto = null;

    public PhotoPicker() {
        mRequestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> { });
    }

    public void setOnPhotoSelectedListener(OnPhotoSelectedListener listener) {
        mOnPhotoSelectedListener = listener;
    }

    @Override
    protected View createView(LayoutInflater inflater) {
        mList = new RecyclerView(inflater.getContext());
        mList.setLayoutManager(new GridLayoutManager(inflater.getContext(), 3, RecyclerView.VERTICAL, false));

        int horizontalMargin = (int) (inflater.getContext().getResources().getDisplayMetrics().density * MARGIN_HORIZONTAL);
        mList.setPadding(horizontalMargin, horizontalMargin, horizontalMargin, horizontalMargin);

        mAdapter = new PhotoAdapter();
        mList.setAdapter(mAdapter);

        return mList;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mRequestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        else {
            mAdapter.loadData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        for (int i = 0; i < mList.getChildCount(); i++) {
            if (mList.getChildAt(i) instanceof ImageView) {
                Drawable drawable = ((ImageView) mList.getChildAt(i)).getDrawable();

                if (drawable instanceof BitmapDrawable) {
                    ((BitmapDrawable) drawable).getBitmap().recycle();
                }
            }
        }

        mList = null;
        mAdapter = null;
    }

    @Override
    public void onClosed() {
        super.onClosed();

        if (mSelectedPhoto != null && mOnPhotoSelectedListener != null) {
            mOnPhotoSelectedListener.onPhotoSelected(mSelectedPhoto);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<Uri> mPhotoUris = null;

        public void loadData() {
            DefaultTaskManager.getInstance().runTask(TaskManager.DATABASE_READ, () -> {
                Cursor cursor = getContext().getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        new String[] { MediaStore.Images.Media._ID },
                        null, null,
                        MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 300"
                );

                if (cursor != null) {
                    List<Uri> photoUris = new ArrayList<>(cursor.getCount());

                    if (cursor.getCount() > 0) {
                        cursor.moveToFirst();

                        while (!cursor.isAfterLast()) {
                            photoUris.add(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getString(0)));
                            cursor.moveToNext();
                        }
                    }

                    cursor.close();

                    DefaultTaskManager.getInstance().runTask(TaskManager.MAIN, () -> {
                        mPhotoUris = photoUris;
                        notifyDataSetChanged();
                    }, 0);
                }
            }, 0);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView view = new RoundedCornersImageView(parent.getContext());
            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = (int) (getContext().getResources().getDisplayMetrics().density * ITEM_PADDING);
            params.topMargin = params.rightMargin = params.bottomMargin = params.leftMargin;
            view.setLayoutParams(params);

            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(view) { };

            view.setOnClickListener(v -> {
                if (mOnPhotoSelectedListener != null) {
                    mSelectedPhoto = mPhotoUris.get(holder.getBindingAdapterPosition());
                    dismiss();
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            ImageView view = (ImageView) holder.itemView;

            Drawable drawable = view.getDrawable();

            view.setImageDrawable(null);

            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                bitmap.recycle();
            }

            BitmapLoader.loadBitmap(getContext(), mPhotoUris.get(position), getResources().getDisplayMetrics().widthPixels / 3, bitmap -> {
                if (getContext() == null) {
                    return;
                }

                if (holder.getBindingAdapterPosition() == position) {
                    view.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                }
                else {
                    bitmap.recycle();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPhotoUris == null ? 0 : mPhotoUris.size();
        }

    }

}
