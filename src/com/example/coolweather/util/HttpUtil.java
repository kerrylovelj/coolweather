package com.example.coolweather.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
	
	/**
	 * ��ɽ�������
	 * @param address ������IP
	 * @param listener ʹ��HttpCallbackListener��ڵ�ʵ��
	 */
	public static void sendHttpRequest(final String address,final HttpCallbackListener listener)
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				HttpURLConnection connection = null;
				try {
					URL url = new URL(address);
					connection = (HttpURLConnection)url.openConnection();//������
					connection.setRequestMethod("GET");//���û�ȡ
					connection.setConnectTimeout(8000);
					connection.setReadTimeout(8000);
					InputStream in = connection.getInputStream();//��ȡ������
					BufferedReader reader = new BufferedReader(new InputStreamReader(in));
					
					StringBuilder response = new StringBuilder();
					String line;
					while((line=reader.readLine())!=null){//���ж�ȡ
						response.append(line);
					}
					
					if(listener!=null){//��ȡ������
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
		}).start();//�߳�����
	}
}
