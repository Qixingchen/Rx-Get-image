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

    private SparseArray<Subscriber<? super File>> mSubscribers = new SparseArray<>();

    public RxGetImage() {

    }

    public static Builder newBuilder() {
        return new Builder();
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

    private Observable<File> fromBuild(final Builder builder) {
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                synchronized (RxGetImage.class) {
                    //get Subscribers ID
                    int i = 1;
                    while (mSubscribers.get(i) != null) {
                        i++;
                    }
                    //add intent
                    Intent intent = GetImageActivity.getStartIntent(Init.getApplication(), i,
                            builder.isTakePhoto, builder.isSingle,
                            builder.needCompress, builder.needCorp,
                            builder.maxArraySize, builder.maxSizeInKib,
                            builder.maxWidthInPx, builder.maxHeightInPx
                    );
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Init.getApplication().startActivity(intent);
                    mSubscribers.append(i, subscriber);
                }
            }
        });
    }

    @SuppressWarnings("SameParameterValue")
    public static final class Builder {
        private boolean needCorp;
        private boolean needCompress;
        private boolean isSingle;
        private boolean isTakePhoto;
        private int maxArraySize;
        private int maxSizeInKib;
        private int maxWidthInPx;
        private int maxHeightInPx;

        public Builder() {
            needCorp = false;
            needCompress = true;
            isSingle = true;
            isTakePhoto = false;

            maxArraySize = Integer.MAX_VALUE;
            maxSizeInKib = 150;
            maxWidthInPx = 1920;
            maxHeightInPx = 1920;
        }

        @NonNull
        public Builder needCorp(boolean val) {
            needCorp = val;
            return this;
        }

        @NonNull
        public Builder needCompress(boolean val) {
            needCompress = val;
            return this;
        }

        @NonNull
        public Builder isSingle(boolean val) {
            isSingle = val;
            return this;
        }

        @NonNull
        public Builder isTakePhoto(boolean val) {
            isTakePhoto = val;
            return this;
        }

        @NonNull
        public Builder maxArraySize(int val) {
            maxArraySize = val;
            return this;
        }

        @NonNull
        public Builder maxSizeInKib(int val) {
            maxSizeInKib = val;
            return this;
        }

        @NonNull
        public Builder maxWidthInPx(int val) {
            maxWidthInPx = val;
            return this;
        }

        @NonNull
        public Builder maxHeightInPx(int val) {
            maxHeightInPx = val;
            return this;
        }

        @NonNull
        public Builder maxSideInPx(int val) {
            maxHeightInPx = val;
            maxWidthInPx = val;
            return this;
        }

        @NonNull
        public Observable<File> build() {
            return RxGetImage.getInstance().fromBuild(this);
        }
    }


}
