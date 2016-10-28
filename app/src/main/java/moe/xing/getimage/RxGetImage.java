package moe.xing.getimage;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.io.File;

import moe.xing.baseutils.Init;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by hehanbo on 16-9-29.
 * <p>
 * 获取图片
 */

@SuppressWarnings("WeakerAccess")
public class RxGetImage {

    private static RxGetImage sSingleton;
    private SparseArray<Subscriber<? super File>> mSubscribers = new SparseArray<>();


    public RxGetImage() {

    }

    /**
     * 获取单例
     */
    public static RxGetImage getInstance() {
        if (sSingleton == null) {
            synchronized (RxGetImage.class) {
                if (sSingleton == null) {
                    sSingleton = new RxGetImage();
                }
            }
        }
        return sSingleton;
    }

    /**
     * 获取图片
     *
     * @return Observable<File>
     */
    @NonNull
    public Observable<File> getImage() {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                synchronized (RxGetImage.class) {
                    int i = 1;
                    while (mSubscribers.get(i) != null) {
                        i++;
                    }

                    Intent intent = GetImageActivity.getStartIntent(Init.getApplication(), i);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Init.getApplication().startActivity(intent);
                    mSubscribers.append(i, subscriber);
                }
            }
        });
    }

    /**
     * 设置返回的图片
     *
     * @param file 返回的图片 可能为空(用户放弃)
     */
    void onAns(@Nullable File file, int subscriberID) {
        Subscriber<? super File> subscriber = mSubscribers.get(subscriberID);
        if (subscriber != null && file != null) {
            subscriber.onNext(file);
        }
    }

    /**
     * 设置返回错误
     *
     * @param message 错误信息
     */
    void onError(Throwable message, int subscriberID) {
        Subscriber<? super File> subscriber = mSubscribers.get(subscriberID);
        subscriber.onError(message);
    }
}
