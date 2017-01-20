package moe.xing.getimage;

import android.content.Intent;
import android.support.annotation.IntDef;
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

    public static final int MODE_SINGLE = 1;
    public static final int MODE_SINGLE_AND_CORP = 2;
    public static final int MODE_MULTIPLE = 3;
    public static final int MODE_TAKE_PHOTO = 4;
    public static final int MODE_TAKE_PHOTO_AND_CORP = 5;
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
    public Observable<File> getImage(final boolean needCorp) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                synchronized (RxGetImage.class) {
                    int i = 1;
                    while (mSubscribers.get(i) != null) {
                        i++;
                    }

                    Intent intent = GetImageActivity.getStartIntent(Init.getApplication(), i,
                            needCorp ? MODE_SINGLE_AND_CORP : MODE_SINGLE, 1);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Init.getApplication().startActivity(intent);
                    mSubscribers.append(i, subscriber);
                }
            }
        });
    }

    /**
     * 获取多张图片
     *
     * @param maxSize 最大数量
     * @return Observable<File>
     */
    @NonNull
    public Observable<File> getMultipleImage(final int maxSize) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                synchronized (RxGetImage.class) {
                    int i = 1;
                    while (mSubscribers.get(i) != null) {
                        i++;
                    }
                    Intent intent = GetImageActivity.getStartIntent(Init.getApplication(), i, MODE_MULTIPLE, maxSize);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Init.getApplication().startActivity(intent);
                    mSubscribers.append(i, subscriber);
                }
            }
        });
    }

    /**
     * 拍摄图片
     *
     * @return Observable<File>
     */
    @NonNull
    public Observable<File> takeImage(final boolean needCorp) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                synchronized (RxGetImage.class) {
                    int i = 1;
                    while (mSubscribers.get(i) != null) {
                        i++;
                    }
                    Intent intent = GetImageActivity.getStartIntent(Init.getApplication(), i,
                            needCorp ? MODE_TAKE_PHOTO_AND_CORP : MODE_TAKE_PHOTO, 1);
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
        if (subscriber != null) {
            subscriber.onError(message);
        }
    }

    void onComplete(int subscriberID) {
        Subscriber<? super File> subscriber = mSubscribers.get(subscriberID);
        if (subscriber != null) {
            subscriber.onCompleted();
        }
    }

    @IntDef({MODE_SINGLE, MODE_MULTIPLE, MODE_SINGLE_AND_CORP, MODE_TAKE_PHOTO, MODE_TAKE_PHOTO_AND_CORP})
    public @interface SelectMode {
    }
}
