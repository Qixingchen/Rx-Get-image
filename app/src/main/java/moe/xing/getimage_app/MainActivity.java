package moe.xing.getimage_app;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import moe.xing.baseutils.Init;
import moe.xing.getimage.RxGetImage;
import moe.xing.getimage_app.databinding.ActivityMainBinding;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init.getInstance(getApplication(), true, "1", "");
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false);
        setContentView(mBinding.getRoot());

        mAdapter = new ImageAdapter();
        mBinding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBinding.recyclerView.setAdapter(mAdapter);

        final Subscriber<File> fileSubscriber = new Subscriber<File>() {
            @Override
            public void onCompleted() {
                Toast.makeText(MainActivity.this, "complete", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this, "error:" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(File file) {
                mAdapter.addData(file);
                Toast.makeText(MainActivity.this, "image add", Toast.LENGTH_SHORT).show();
            }
        };


        mBinding.singleCorp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxGetImage.newBuilder().needCorp(true).isSingle(true).build().subscribe(fileSubscriber);

            }
        });

        mBinding.single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxGetImage.newBuilder().needCorp(false).isSingle(true).build().subscribe(fileSubscriber);
            }
        });

        mBinding.multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxGetImage.newBuilder().isSingle(false).build().subscribe(fileSubscriber);
            }
        });

        mBinding.take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxGetImage.newBuilder().isTakePhoto(true).needCorp(false).build().subscribe(fileSubscriber);
            }
        });

        mBinding.takeAndCorp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxGetImage.newBuilder().isTakePhoto(true).needCorp(true).build().subscribe(fileSubscriber);
            }
        });

    }
}
