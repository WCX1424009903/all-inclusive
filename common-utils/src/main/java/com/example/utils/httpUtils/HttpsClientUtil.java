package com.example.utils.httpUtils;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

/**
 *
 * @program: rohs_web
 * @description: https util 参考：https://blog.csdn.net/weixin_42142125/article/details/83620792
 * @author: liy
 * @create: 2021-04-25 14:18
 **/
public class HttpsClientUtil {

    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";

    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 6000;

    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 6000;


    public static CloseableHttpClient createSSLClientDefault() {
        try {
            //使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            //NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
            //有效的SSL会话并匹配到目标主机。
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }


    /**
     * 模拟发送https post 请求
     * @param url
     * @param headers
     * @param params
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPost(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        //构建POST请求   请求地址请更换为自己的。
        HttpPost httpPost = new HttpPost(url);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        /*httpPost.setHeader("Cookie", "");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");*/
        HttpClientUtil.packageHeader(headers, httpPost);

        // 封装请求参数
        HttpClientUtil.packageParam(params, httpPost);

        InputStream inputStream = null;
        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = null;
        //使用之前写的方法创建httpClient实例
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createSSLClientDefault();
            return HttpClientUtil.getHttpClientResult(httpResponse,httpClient,httpPost);
        } finally {
            // 释放资源
            close(httpClient,httpResponse,null);
        }
    }

    /**
     * 模拟发送https get 请求
     */
    public static HttpClientResult doGet(String url) throws Exception {
        //构建POST请求   请求地址请更换为自己的。
        HttpGet get = new HttpGet(url);
        InputStream inputStream = null;
        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = null;
        //使用之前写的方法创建httpClient实例
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createSSLClientDefault();
            // 构造消息头
            get.setHeader("Content-type", "text/html;charset=UTF-8");

            return HttpClientUtil.getHttpClientResult(httpResponse,httpClient,get);
        } finally {
            // 释放资源
            close(httpClient,httpResponse,null);
        }
    }

    /**
     * 释放资源
     * @param httpClient
     * @param httpResponse
     * @param inputStream
     * @throws IOException
     */
    public static void close(CloseableHttpClient httpClient,CloseableHttpResponse httpResponse,InputStream inputStream) throws IOException {
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 重定向 302
     * 模拟发送https post 请求
     * @param url
     * @param headers
     * @param params
     * @return 重定向地址 Header里的Location属性
     * @throws Exception
     */
    public static String doPostWithRedirect(String url, Map<String, String> headers, Map<String, String> params) throws Exception {
        String locationUrl = "";
        //构建POST请求   请求地址请更换为自己的。
        HttpPost httpPost = new HttpPost(url);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        // 设置请求头
        HttpClientUtil.packageHeader(headers, httpPost);

        // 封装请求参数
        HttpClientUtil.packageParam(params, httpPost);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = null;
        //使用之前写的方法创建httpClient实例
        CloseableHttpClient httpClient = null;
        try {
            httpClient = createSSLClientDefault();
            httpResponse = httpClient.execute(httpPost);
            //----------判断是否重定向开始
            //获取响应头信息
//            Header[] responseHeaders = httpResponse.getAllHeaders();
            int code = httpResponse.getStatusLine().getStatusCode();
            if (code == 302) {
                Header header = httpResponse.getFirstHeader("Location"); //目标地址是在HTTP-HEAD中的Location
                locationUrl = header.getValue(); //跳转后的地址
            }
        } finally {
            // 释放资源
            close(httpClient,httpResponse,null);
        }
        return locationUrl;
    }
}
