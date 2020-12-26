package com.github.audio_video;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by lvming on 12/24/20 6:26 PM.
 * Email: lvming@guazi.com
 * Description:
 */
public class Utils {

    public static void writePCM(byte[] frame) {
        String filePath = Environment.getExternalStorageDirectory() + "/test.pcm";
        File file = new File(filePath);
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                fos = new FileOutputStream(file);
            }else{
                fos = new FileOutputStream(file,true);
            }
            fos.write(frame);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
