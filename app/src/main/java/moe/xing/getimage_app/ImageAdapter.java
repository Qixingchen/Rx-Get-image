package moe.xing.getimage_app;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import java.io.File;

import moe.xing.getimage_app.databinding.ItemFileBinding;
import moe.xing.rvutils.BaseRecyclerViewAdapter;

/**
 * Created by Qi Xingchen on 16-11-30.
 */

class ImageAdapter extends BaseRecyclerViewAdapter<File, ImageAdapter.ViewHolder> {

    ImageAdapter() {
        super(File.class);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemFileBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_file, parent, false);
        return new ViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Glide.with(holder.itemView.getContext()).load(datas.get(position)).centerCrop().into(holder.mBinding.image);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ItemFileBinding mBinding;

        ViewHolder(View itemView) {
            super(itemView);
            mBinding = DataBindingUtil.findBinding(itemView);
        }
    }
}
