package com.example.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	
	/**
	 * 完成接受数据
	 * @param address 服务器IP
	 * @param listener 使用HttpCallbackListener借口的实例
	 */
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection)url.openConnection();//打开链接
					connection.setRequestMethod("GET");//设置获取
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();//获取输入流
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					
					StringBuilder response = new StringBuilder();
					String line;
					while((line=reader.readLine())!=null){//逐行读取
						response.append(line);
					}
					
					if(listener!=null){//读取完后输出
						listener.onFinish(response.toString());
					}
					
				} catch (Exception e) {
					if(listener!=null){
						listener.onError(e);
					}
				}finally{
					if(connection!=null){
						connection.disconnect();
					}
				}
			}
		}).start();//线程启动
	}
}
