package Hello;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;


/**
 * @Author: wangheng
 * @Date: 2020/10/17
 * @Description:
 */
public class HelloClassLoader extends ClassLoader {

    public static void main(String[] args) {

        HelloClassLoader loader = new HelloClassLoader();
        try {
            Class<?>  aClass = loader.findClass("Hello");
            Object obj = aClass.newInstance();
            Method method = aClass.getMethod("hello");
            method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        byte[] bytes = null;
        try {
            bytes = fileToByte("/Users/wangheng/github/learn/math/src/main/java/Hello/Hello.xlass");

        }catch (Exception e){
            e.printStackTrace();
        }
        if(null == bytes){
            throw new ClassNotFoundException("文件为空");
        }

        for (int i=0;i<bytes.length;i++){
            bytes[i] = (byte)(255-bytes[i]);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    /**
     * 文件转字节数组
     * @param filePath
     * @return
     * @throws IOException
     */
    public static byte[] fileToByte(String filePath) throws IOException {
        byte[] bytes = null;
        FileInputStream fis = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bytes = new byte[(int) file.length()];
            fis.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if(null != fis){
                fis.close();
            }
        }
        return bytes;

    }
}
