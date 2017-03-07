package moe.xing.getimage;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.shaohui.advancedluban.Luban;
import moe.xing.baseutils.Init;
import moe.xing.baseutils.utils.FileUtils;
import moe.xing.baseutils.utils.IntentUtils;
import moe.xing.rx_utils.RxFileUtils;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
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
    //    @Deprecated
//    private static final String SELECT_MODE = "select_mode";
    private static final String MAX_ARRAY_SIZE = "MAX_ARRAY_SIZE";
    private static final String IS_SINGLE = "IS_SINGLE";
    private static final String IS_TAKE_PHOTO = "IS_TAKE_PHOTO";
    private static final String NEED_COMPRESS = "NEED_COMPRESS";
    private static final String NEED_CORP = "NEED_CORP";
    private static final String MAX_SIZE_IN_KIB = "MAX_SIZE_IN_KIB";
    private static final String MAX_WIDTH_IN_PX = "MAX_WIDTH_IN_PX";
    private static final String MAX_HEIGHT_IN_PX = "MAX_HEIGHT_IN_PX";
    private Uri corpedImage;
    @Nullable
    private File takenFile = null;

//    /**
//     * 获取启动 intent
//     *
//     * @param subscriberID subscriberID
//     * @param selectMode   选择模式(单选/多选)
//     */
//    public static Intent getStartIntent(Context context, int subscriberID,
//                                        @RxGetImage.SelectMode int selectMode,
//                                        int maxSize) {
//        Intent intent = new Intent(context, GetImageActivity.class);
//        intent.putExtra(SUBSCRIBER_ID, subscriberID);
//        intent.putExtra(SELECT_MODE, selectMode);
//        intent.putExtra(MAX_ARRAY_SIZE, maxSize);
//        return intent;
//    }

    /**
     * 获取启动 intent
     *
     * @param context       上下文
     * @param subscriberID  subscriberID
     * @param isSingle      是否是单图
     * @param isTakePhoto   是否是拍摄
     * @param needCompress  是否需要压缩
     * @param needCorp      是否需要裁剪
     * @param maxArraySize  获取的最大图片数量
     * @param maxSizeInKib  图片压缩后的最大大小(如果选了压缩)
     * @param maxWidthInPx  图片压缩后的最大宽度(如果选了压缩)
     * @param maxHeightInPx 图片压缩后的最大高度(如果选了压缩)
     */
    public static Intent getStartIntent(Context context, int subscriberID, boolean isTakePhoto,
                                        boolean isSingle, boolean needCompress, boolean needCorp,
                                        int maxArraySize, int maxSizeInKib,
                                        int maxWidthInPx, int maxHeightInPx) {
        Intent intent = new Intent(context, GetImageActivity.class);
        intent.putExtra(SUBSCRIBER_ID, subscriberID);
        intent.putExtra(IS_TAKE_PHOTO, isTakePhoto);
        intent.putExtra(IS_SINGLE, isSingle);
        intent.putExtra(NEED_COMPRESS, needCompress);
        intent.putExtra(NEED_CORP, needCorp);
        intent.putExtra(MAX_ARRAY_SIZE, maxArraySize);
        intent.putExtra(MAX_SIZE_IN_KIB, maxSizeInKib);
        intent.putExtra(MAX_WIDTH_IN_PX, maxWidthInPx);
        intent.putExtra(MAX_HEIGHT_IN_PX, maxHeightInPx);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            doGet();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        doGet();
    }

    /**
     * 选择图片
     */
    private void doGet() {

        if (getIntent().getBooleanExtra(IS_TAKE_PHOTO, false)) {
            doTake();
        } else {
            doSelect();
        }
    }

    private void doSelect() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        if (!getIntent().getBooleanExtra(IS_SINGLE, true)) {
            photoPickerIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        IntentUtils.startIntentForResult(photoPickerIntent, this, SELECT_PHOTO);
    }

    private void doTake() {
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            takenPhotoFile();
        } catch (IOException ioe) {
            RxGetImage.getInstance().onError(new Throwable("无法创建相片,可能空间已满"), getSubscriberID());
            finish();
            return;
        }
        if (takenFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    Init.getApplication().getPackageName() + ".fileProvider",
                    takenFile);
            takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            IntentUtils.startIntentForResult(takeIntent, this, TAKE_PHOTO);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SELECT_PHOTO:
                    if (getIntent().getBooleanExtra(IS_SINGLE, true)) {
                        getSingleImage(data.getData());
                    } else {
                        getMultipleImages(data, getIntent().getIntExtra(MAX_ARRAY_SIZE, 1));
                    }
                    break;
                case CORP_PHOTO:
                    if (corpedImage != null) {
                        File corped = new File(URI.create(corpedImage.toString()));
                        sendSingleAnsAndFinish(corped);
                    } else {
                        RxGetImage.getInstance().onError(new Throwable("空文件"), getSubscriberID());
                        finish();
                    }
                    break;
                case TAKE_PHOTO:
                    if (takenFile != null && takenFile.exists() && takenFile.length() > 0) {
                        if (getIntent().getBooleanExtra(NEED_CORP, false)) {
                            toCorp(takenFile);
                        } else {
                            sendSingleAnsAndFinish(takenFile);
                        }
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
            }).flatMap(new Func1<File, Observable<File>>() {
                @Override
                public Observable<File> call(File file) {
                    if (getIntent().getBooleanExtra(NEED_COMPRESS, true)) {
                        return Luban.compress(GetImageActivity.this, file)
                                .setMaxSize(getIntent().getIntExtra(MAX_SIZE_IN_KIB, 150))
                                .setMaxWidth(getIntent().getIntExtra(MAX_WIDTH_IN_PX, 1920))
                                .setMaxHeight(getIntent().getIntExtra(MAX_HEIGHT_IN_PX, 1920))
                                .putGear(Luban.CUSTOM_GEAR).asObservable();
                    } else {
                        return Observable.just(file);
                    }
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
                            finish();
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
                        if (getIntent().getBooleanExtra(NEED_CORP, false)) {
                            toCorp(s);
                        } else {
                            sendSingleAnsAndFinish(s);
                        }
                    }
                });
    }

    private int getSubscriberID() {
        return getIntent().getIntExtra(SUBSCRIBER_ID, 0);
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

    private File takenPhotoFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File image = FileUtils.getCacheFile(timeStamp);
        takenFile = image;

        return image;
    }

    private void sendSingleAnsAndFinish(@NonNull File image) {
        if (getIntent().getBooleanExtra(NEED_COMPRESS, true)) {
            Luban.compress(this, image)
                    .setMaxSize(getIntent().getIntExtra(MAX_SIZE_IN_KIB, 150))
                    .setMaxWidth(getIntent().getIntExtra(MAX_WIDTH_IN_PX, 1920))
                    .setMaxHeight(getIntent().getIntExtra(MAX_HEIGHT_IN_PX, 1920))
                    .putGear(Luban.CUSTOM_GEAR).asObservable()
                    .subscribe(new Subscriber<File>() {
                        @Override
                        public void onCompleted() {
                            RxGetImage.getInstance().onComplete(getSubscriberID());
                            finish();
                        }

                        @Override
                        public void onError(Throwable e) {
                            RxGetImage.getInstance().onError(e, getSubscriberID());
                            finish();
                        }

                        @Override
                        public void onNext(File file) {
                            RxGetImage.getInstance().onAns(file, getSubscriberID());
                        }
                    });
        } else {
            RxGetImage.getInstance().onAns(image, getSubscriberID());
            RxGetImage.getInstance().onComplete(getSubscriberID());
            finish();
        }
    }
}
