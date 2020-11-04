package io.github.kimmking.gateway.outbound.httpclient4;

import io.netty.handler.codec.http.FullHttpRequest;
import org.apache.http.*;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @author wangheng
 * HttpClient
 *
 */
public class HttpClientUtil {
	private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);
	/**
	 * HTTP内容类型。相当于form表单的形式，提交数据
	 */
	public static final String CONTENT_TYPE_JSON_URL = "application/json;charset=utf-8";

	/** 默认重试次数 */
	private static final Integer DEFAULT_RETRY_COUNT = 1;

	/** 数据传输超时时间 */
	private static final Integer DEFAULT_SOCKET_TIMEOUT = 2000;

	/** 请求超时时间 */
	private static final Integer DEFAULT_CONNECT_TIMEOUT = 2000;

	/**
	 * utf-8字符编码
	 */
	public static final String CHARSET_UTF_8 = "utf-8";

	/**
	 * http get
	 * @param url 请求地址
	 * @return
	 */
	public static HttpResponse httpGet(FullHttpRequest inbound,String url) {

		// 创建Httpclient对象
		CloseableHttpClient httpClient = getHttpClient(DEFAULT_RETRY_COUNT,DEFAULT_SOCKET_TIMEOUT,DEFAULT_CONNECT_TIMEOUT);
		CloseableHttpResponse response = null;
		String resultString = "";
		try {
			// 创建Http Get请求
			HttpGet httpGet = new HttpGet(url);
			//循环赋值header
			io.netty.handler.codec.http.HttpHeaders httpHeaders = inbound.headers();
			List<Map.Entry<String, String>> list = httpHeaders.entries();

			for(Map.Entry<String, String> entry : list){
				httpGet.setHeader(entry.getKey(),entry.getValue());
			}

			// 创建参数列表
			httpGet.setConfig(requestConfig(DEFAULT_SOCKET_TIMEOUT,DEFAULT_CONNECT_TIMEOUT));
			httpGet.setHeader(HTTP.CONN_DIRECTIVE, HTTP.CONN_KEEP_ALIVE);
			// 执行http请求
			response = httpClient.execute(httpGet);
			return response;
		} catch (Exception e){
			logger.error("服务调用异常",e);
		}
		return null;
	}
	/**
	 * 配置超时时间
	 * @return
	 */

	private static RequestConfig requestConfig(int socketTimeout,int connectTimeout) {
		// 根据默认超时限制初始化requestConfig
		RequestConfig config = RequestConfig.custom()
				// 创建连接的最长时间
				.setConnectTimeout(connectTimeout)
				// 数据传输的最长时间
				.setSocketTimeout(socketTimeout)
				.build();
		return config;
	}

	/**
	 * 获取httpclient对象
	 * @param count
	 * @param socketTimeout
	 * @param connectTimeout
	 * @return
	 */

	public static CloseableHttpClient getHttpClient(int count,int socketTimeout,int connectTimeout) {
		HttpRequestRetryHandler httpRequestRetryHandler = (IOException exception, int executionCount, HttpContext context)->
		{
			if (executionCount > count) {
				// Do not retry if over max retry count
				return false;
			}
			if (exception instanceof ConnectTimeoutException
					|| exception instanceof NoHttpResponseException) {
				return true;
			}

			HttpClientContext clientContext = HttpClientContext.adapt(context);
			HttpRequest request = clientContext.getRequest();
			boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
			if (idempotent) {
				// 如果请求被认为是幂等的，那么就重试。即重复执行不影响程序其他效果的
				return true;
			}
			return false;
		};


		CloseableHttpClient httpClient = HttpClients.custom()
				// 设置请求配置
				.setDefaultRequestConfig(requestConfig(socketTimeout,connectTimeout))
				// 设置重试次数
				.setRetryHandler(httpRequestRetryHandler)
				.build();

		return httpClient;
	}
}
