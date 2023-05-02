package github.suzume.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * @Author 铃芽
 * @Date 2023/04/29 18:58
 * @Describe 文件辅助类
 */

public class Util {
    
    public static byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4096);
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        byte[] ret = output.toByteArray();
        output.close();
        return ret;
    }
    
    public static InputStream open(String file){
        return open(new File(file));
    }
    
    public static InputStream open(File file){
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
    
    public static void sleep(long t){
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {}
    }
    
}
