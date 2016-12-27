package moe.xing.getimage;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import moe.xing.baseutils.Init;
import moe.xing.baseutils.utils.FileUtils;
import moe.xing.baseutils.utils.IntentUtils;
import moe.xing.rx_utils.RxFileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by hehanbo on 16-9-29.
 * <p>
 * 获取图像
 */

public class GetImageActivity extends Activity {

    private static final int SELECT_PHOTO = 10;
    private static final int TAKE_PHOTO = 20;
    private static final int CORP_PHOTO = 30;
    private static final String SUBSCRIBER_ID = "SubscriberID";
    private static final String SELECT_MODE = "select_mode";
    private static final String MAX_SIZE = "max_size";
    private Uri corpedImage;

    /**
     * 获取启动 intent
     *
     * @param subscriberID subscriberID
     * @param selectMode   选择模式(单选/多选)
     */
    public static Intent getStartIntent(Context context, int subscriberID, @RxGetImage.SelectMode int selectMode,
                                        int maxSize) {
        Intent intent = new Intent(context, GetImageActivity.class);
        intent.putExtra(SUBSCRIBER_ID, subscriberID);
        intent.putExtra(SELECT_MODE, selectMode);
        intent.putExtra(MAX_SIZE, maxSize);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            doSelect();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        doSelect();
    }

    /**
     * 选择图片
     */
    private void doSelect() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        if (RxGetImage.MODE_MULTIPLE == getIntent().getIntExtra(SELECT_MODE, RxGetImage.MODE_SINGLE)) {
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        IntentUtils.startIntentForResult(photoPickerIntent, this, SELECT_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_PHOTO:
                    if ((RxGetImage.MODE_MULTIPLE == getSelectMode())) {
                        getMultipleImages(data, getIntent().getIntExtra(MAX_SIZE, 1));
                    } else {
                        getSingleImage(data.getData());
                    }
                    break;
                case CORP_PHOTO:
                    if (corpedImage != null) {
                        File corped = new File(URI.create(corpedImage.toString()));
                        RxGetImage.getInstance().onAns(corped, getSubscriberID());
                        RxGetImage.getInstance().onComplete(getSubscriberID());
                        finish();
                    } else {
                        RxGetImage.getInstance().onError(new Throwable("空文件"), getSubscriberID());
                        finish();
                    }
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
                    finish();
            }
        } else {
            RxGetImage.getInstance().onError(new Throwable("用户放弃"), getSubscriberID());
            finish();
        }
    }

    /**
     * 获取多个图片
     */
    private void getMultipleImages(Intent data, int max) {
        final ClipData clipData = data.getClipData();
        if (clipData != null) {
            int size = data.getClipData().getItemCount();
            if (size > max) {
                Toast.makeText(this, String.format(Locale.getDefault(), "选择数量超过%d张,%d张未保存",
                        max, size - max), Toast.LENGTH_LONG).show();
            }
            final int finalSize = Math.min(size, max);
            Observable.create(new Observable.OnSubscribe<Uri>() {
                @Override
                public void call(Subscriber<? super Uri> subscriber) {
                    for (int i = 0; i < finalSize; i++) {
                        subscriber.onNext(clipData.getItemAt(i).getUri());
                    }
                    subscriber.onCompleted();
                }
            }).lift(new Observable.Operator<File, Uri>() {
                @Override
                public Subscriber<? super Uri> call(final Subscriber<? super File> subscriber) {

                    return new Subscriber<Uri>() {
                        @Override
                        public void onCompleted() {
                            subscriber.onCompleted();
                        }

                        @Override
                        public void onError(Throwable e) {
                            subscriber.onError(e);
                        }

                        @Override
                        public void onNext(Uri uri) {
                            RxFileUtils.getFileUrlWithAuthority(Init.getApplication(), uri)
                                    .subscribe(new Action1<File>() {
                                        @Override
                                        public void call(File file) {
                                            subscriber.onNext(file);
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            subscriber.onError(throwable);
                                        }
                                    });
                        }
                    };
                }
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<File>() {
                        @Override
                        public void onCompleted() {
                            RxGetImage.getInstance().onComplete(getSubscriberID());
                            finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            RxGetImage.getInstance().onError(e, getSubscriberID());
                        }

                        @Override
                        public void onNext(File file) {
                            RxGetImage.getInstance().onAns(file, getSubscriberID());
                        }
                    });

        } else {
            getSingleImage(data.getData());
        }
    }

    /**
     * 获取单张图片
     */
    private void getSingleImage(Uri uri) {
        RxFileUtils.getFileUrlWithAuthority(Init.getApplication(), uri)
                .first()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<File>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        RxGetImage.getInstance().onError(e, getSubscriberID());
                        finish();
                    }

                    @Override
                    public void onNext(File s) {
                        if (RxGetImage.MODE_SINGLE_AND_CORP == getSelectMode()) {
                            toCorp(s);
                        } else {
                            RxGetImage.getInstance().onAns(s, getSubscriberID());
                            RxGetImage.getInstance().onComplete(getSubscriberID());
                            finish();
                        }
                    }
                });
    }


    private int getSubscriberID() {
        return getIntent().getIntExtra(SUBSCRIBER_ID, 0);
    }

    @RxGetImage.SelectMode
    private int getSelectMode() {
        //noinspection WrongConstant
        return getIntent().getIntExtra(SELECT_MODE, RxGetImage.MODE_SINGLE);
    }

    /**
     * 去裁剪图片
     */
    private void toCorp(File image) {
        File ans;
        try {
            ans = FileUtils.getCacheFile("corp-" + image.getName());
            Crop.of(Uri.fromFile(image), Uri.fromFile(ans))
                    .withAspect(1, 1).start(this, CORP_PHOTO);
            corpedImage = Uri.fromFile(ans);
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        corpedImage = null;
    }
}
