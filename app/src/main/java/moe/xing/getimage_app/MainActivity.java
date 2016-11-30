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
import moe.xing.getimage.BuildConfig;
import moe.xing.getimage.RxGetImage;
import moe.xing.getimage_app.databinding.ActivityMainBinding;
import rx.Subscriber;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private ImageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init.getInstance(getApplication(), BuildConfig.DEBUG, "1", "");
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_main, null, false);
        setContentView(mBinding.getRoot());

        mAdapter = new ImageAdapter();
        mBinding.recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mBinding.recyclerView.setAdapter(mAdapter);

        mBinding.single.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxGetImage.getInstance().getImage(RxGetImage.MODE_SINGLE).subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(v.getContext(), "complete", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(v.getContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(File file) {
                        mAdapter.addData(file);
                    }
                });

            }
        });

        mBinding.multiple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                RxGetImage.getInstance().getMultipleImage(Integer.MAX_VALUE).subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                        Toast.makeText(v.getContext(), "complete", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(v.getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(File file) {
                        mAdapter.addData(file);
                    }
                });

            }
        });

    }
}
