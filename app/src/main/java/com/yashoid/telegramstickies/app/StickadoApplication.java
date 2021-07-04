package com.yashoid.telegramstickies.app;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.yashoid.mmv.Managers;
import com.yashoid.office.task.DefaultTaskManager;
import com.yashoid.telegramstickies.app.model.sticker.Sticker;
import com.yashoid.telegramstickies.app.model.stickerpack.AnimatedStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpack.StaticStickerPack;
import com.yashoid.telegramstickies.app.model.stickerpacklist.AnimatedStickerPackList;
import com.yashoid.telegramstickies.app.model.stickerpacklist.StaticStickerPackList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class StickadoApplication extends Application {

    private static final String LOG_FILE = "report.txt";

    public static File getLogFile(Context context) {
        return new File(context.getExternalCacheDir(), LOG_FILE);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                File file = getLogFile(StickadoApplication.this);
                OutputStream output = new FileOutputStream(file);
                PrintWriter writer = new PrintWriter(output);
                e.printStackTrace(writer);
                writer.write("\nEnd of report ------------\n\n\n");
                writer.flush();
                output.close();
            } catch (Throwable error) { }

            defaultHandler.uncaughtException(t, e);
        });

        Managers.addTypeProvider(new Sticker.StickerTypeProvider());
        Managers.addTypeProvider(new StaticStickerPack.StaticStickerPackTypeProvider());
        Managers.addTypeProvider(new AnimatedStickerPack.AnimatedStickerPackTypeProvider());
        Managers.addTypeProvider(new StaticStickerPackList.StaticStickerPackListTypeProvider());
        Managers.addTypeProvider(new AnimatedStickerPackList.AnimatedStickerPackListTypeProvider());

        Managers.enableCache(this, DefaultTaskManager.getInstance());
    }

}
