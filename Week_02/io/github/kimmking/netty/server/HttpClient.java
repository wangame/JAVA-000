package io.github.kimmking.netty.server;


import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @Author: wangheng
 * @Date: 2020/10/25
 * @Description:
 */
public class HttpClient {
    //访问url
    private static String url = "http://localhost:8808/test";

    public static void main(String[] args) {

        doPost(url);
    }

    /**
     * http post
     * @param url
     */
    public static void doPost(String url) {
        // 创建Httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();
        // 创建http POST请求
        HttpPost httpPost = new HttpPost(url);
        CloseableHttpResponse response = null;
        try {
            // 执行请求
            response = httpClient.execute(httpPost);
            // 判断返回状态是否为200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println("响应内容为：" + content);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (response != null) {
                    response.close();
                }
                httpClient.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            
        }
    }

}
