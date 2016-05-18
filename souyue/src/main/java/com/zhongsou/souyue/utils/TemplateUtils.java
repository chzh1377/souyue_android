package com.zhongsou.souyue.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

//import org.beetl.core.Configuration;
//import org.beetl.core.GroupTemplate;
//import org.beetl.core.Template;
//import org.beetl.core.resource.FileResourceLoader;

import com.zhongsou.souyue.MainApplication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author YanBin
 * @version V1.0
 * @project trunk
 * @Description Class Description
 * @date 2016/3/26
 */
public class TemplateUtils {
    public static final String TAG = "TemplateUtils";

    /**
     * 调试使用，模板文件优先存放在/data/data/<packName>/file下
     *
     * @param context
     * @return
     */
    public static String getTemplatePath(Context context) {
        String dirName = "template";
        String path = null;
        if (context != null) {
            path = context.getFilesDir().getPath() + File.separator + dirName;
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory() + File.separator + "souyue" + File.separator + dirName;
        }
        return path;
    }

    /**
     * 调试使用，模板文件优先存放在sd卡
     *
     * @param context
     * @return
     */
    public static String getTemplateTestPath(Context context) {
        String dirName = "template";
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                || !Environment.isExternalStorageRemovable()) {
            path = Environment.getExternalStorageDirectory() + File.separator + "souyue" + File.separator + dirName;
        } else if (context != null) {
            path = context.getFilesDir().getPath() + File.separator + dirName;
        }
        return path;
    }

    /**
     * 测试、联调使用
     * @param strHtml
     * @param fileName
     */
    public static void writeHtmlToSD(String strHtml, String fileName) {
        FileWriter fw = null;
        try{
            String path = getTemplateTestPath(MainApplication.getInstance()) + File.separator + "output";
            File dir = new File(path);
            if(!dir.exists()) dir.mkdirs();
            File file = new File(dir,fileName);
            if(!file.exists()) file.createNewFile();
            fw = new FileWriter(file);
            fw.write(strHtml);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void unZipTemplate(InputStream inputStream, String destinationPath) {
        if (inputStream == null) return;
        Log.d("Template", "start unzip template");
//      服务端格式：  "templateVersion": "modle_0!interest_content_000"
        if (StringUtils.isEmpty(destinationPath)) {
           return;
        }
        if (!destinationPath.endsWith(File.separator)) {
            destinationPath = destinationPath + File.separator;
        }
        FileOutputStream fileOut = null;
        ZipInputStream zipIn = null;
        ZipEntry zipEntry = null;
        File file = null;
        int readLength = 0;
        byte buf[] = new byte[4096];
        try {
            zipIn = new ZipInputStream(new BufferedInputStream(inputStream));
            while ((zipEntry = zipIn.getNextEntry()) != null) {
                file = new File(destinationPath + zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    file.mkdirs();
                } else {
                    // 如果指定文件的目录不存在,则创建之.
                    File parent = file.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    fileOut = new FileOutputStream(file);
                    while ((readLength = zipIn.read(buf)) > 0) {
                        fileOut.write(buf, 0, readLength);
                    }
                    fileOut.close();
                }
                zipIn.closeEntry();
            }
        } catch (IOException ioe) {
            Log.d("Template", "unzip template failed");
            ioe.printStackTrace();
        }
    }
}
