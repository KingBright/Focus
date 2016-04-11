package name.kingbright.android.focus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import name.kingbright.android.brilliant.app.BaseActivity;
import name.kingbright.android.brilliant.widgets.ImageView;
import name.kingbright.android.focus.wallpaper.BingWallpaper;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;


/**
 * @author Jin Liang
 * @since 16/4/11
 */
public class SplashActivity extends BaseActivity {

    @Bind(R.id.splash_image)
    ImageView imageView;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.splash_activity);
    }

    @Override
    protected void onViewCreated() {
        Uri uri = Uri.parse("res://" + getPackageName() + "/" + R.raw.splash);
        imageView.setImageURI(uri, this);

        Intent intent = new Intent(getApplicationContext(), BingWallpaper.class);
        startService(intent);

        imageView.animate().scaleX(1.0f).scaleXBy(0.2f).scaleY(1.0f).scaleYBy(0.2f).setDuration(3000).setInterpolator(new DecelerateInterpolator()).start();
        imageView.animate().setStartDelay(3000).alphaBy(-1.0f).setInterpolator(new DecelerateInterpolator()).setDuration(1000).start();

        Observable.timer(4, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).compose(this.<Long>bindToLifecycle()).subscribe(new Observer<Long>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Long aLong) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);

                finish();
            }
        });
    }

}
