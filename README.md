Rx-Get-image

[![Release](https://jitpack.io/v/Qixingchen/Rx-Get-image.svg?style=flat-square)](https://jitpack.io/#Qixingchen/Rx-Get-image)
[![Build Status](https://travis-ci.org/Qixingchen/Rx-Get-image.svg?branch=master)](https://travis-ci.org/Qixingchen/Rx-Get-image)
[![Coverage Status](https://coveralls.io/repos/github/Qixingchen/Rx-Get-image/badge.svg)](https://coveralls.io/github/Qixingchen/Rx-Get-image)

---
### download

 use [jitpack](https://jitpack.io/#Qixingchen/Rx-Get-image)

### how to use

``` java
    RxGetImage.getInstance().getImage()
    .subscribe(new Subscriber<File>() {
        @Override
        public void onCompleted() {
             // todo
        }

        @Override
        public void onError(Throwable e) {
            // todo
        }

        @Override
        public void onNext(File file) {
             // todo
        }
    });
```

read javadoc in [jitpack](https://jitpack.io/com/github/Qixingchen/Rx-Get-image/-SNAPSHOT/javadoc/)
