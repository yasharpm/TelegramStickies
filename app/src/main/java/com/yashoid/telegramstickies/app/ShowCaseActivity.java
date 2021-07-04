package com.yashoid.telegramstickies.app;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.yashoid.telegramstickies.StickerColor;
import com.yashoid.telegramstickies.StickyThingy;

import org.json.JSONException;

import java.util.Set;

public class ShowCaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showcase);

        LottieAnimationView lottie = findViewById(R.id.lottie);

        try {
            StickyThingy stickyThingy = new StickyThingy(Samples.DART_LOOP);

            // Time shift doesn't work right now.
//            stickyThingy.scaleDuration(stickyThingy.getRealDuration());
//            stickyThingy.shiftTime(120);

            stickyThingy.translate(stickyThingy.getWidth() / 4, 0);
            stickyThingy.rotateBy(45);
            stickyThingy.resize(0f, 0f, stickyThingy.getWidth() / 2f, stickyThingy.getHeight());

//            Set<StickerColor> colors = stickyThingy.getColors();
//            int[] fakeColors = new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.WHITE, Color.YELLOW, Color.CYAN, Color.MAGENTA };
//            int colorIndex = 0;
//            for (StickerColor color: colors) {
//                stickyThingy.replaceColor(color, new StickerColor(fakeColors[colorIndex % fakeColors.length]));
//                colorIndex++;
//            }

            StickyThingy dart = stickyThingy;
            StickyThingy download = new StickyThingy(Samples.IC_DOWNLOAD);

            stickyThingy = new StickyThingy();
            stickyThingy.add(dart);
            stickyThingy.add(download);
            stickyThingy.updateInfo();

            lottie.setAnimationFromJson(stickyThingy.toJSON().toString(), "sadasd");
            lottie.setRepeatCount(LottieDrawable.INFINITE);
            lottie.setRepeatMode(LottieDrawable.RESTART);
            lottie.playAnimation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}