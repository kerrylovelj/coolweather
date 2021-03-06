package com.example.coolweather.activity;

import java.util.ArrayList;
import java.util.List;
import com.example.coolweather.R;
import com.example.coolweather.db.CoolWeatherDB;
import com.example.coolweather.model.City;
import com.example.coolweather.model.County;
import com.example.coolweather.model.Province;
import com.example.coolweather.util.HttpCallbackListener;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends Activity {
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTY = 2;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB = null;
	private List<String> dataList = new ArrayList<>();
	
	/**
	 * 省市县表
	 */
	private List<Province> provinceList = new ArrayList<>();
	private List<City> cityList = new ArrayList<>();
	private List<County> countyList = new ArrayList<>();
	/**
	 * 选中的省市县
	 */
	private Province selectedProvince;
	private City selectedCity;
	private County selectedCounty;
	
	/**
	 * 当前选中级别
	 */
	private int currentLevel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Log.e("haha", "执行OnCreate()方法");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		Log.e("haha","控件初始化");
		listView = (ListView)findViewById(R.id.list_view);
		titleText = (TextView)findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		Log.e("haha","适配器设置完毕");
		coolWeatherDB = CoolWeatherDB.getInstance(this);
		Log.e("haha","coolWeatherDB赋值完毕");
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(currentLevel==LEVEL_PROVINCE){
					selectedProvince = provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity = cityList.get(position);
					queryCounties();
				}
			}
		});	
		queryProvinces();//加载省级数据
		Log.e("haha", "OnCreate()方法执行完毕");
	}
	/**
	 * 查询全国所有的省，优先从数据库查找，没有的话再去服务器上查找
	 */
	private void queryProvinces(){
		coolWeatherDB.loadProvince();
		provinceList = coolWeatherDB.loadProvince();
		Log.e("haha", String.valueOf(provinceList.size()));
		if(provinceList.size()>0){
			dataList.clear();
			for(Province province:provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel=LEVEL_PROVINCE;
		}else{
			queryFromServer(null,"province");
		}
	}
	/**
	 * 查询全国所有的市，优先从数据库查找，没有的话再去服务器上查找
	 */
	private void queryCities(){
		Log.e("haha", "进入queryCities()方法 cityList.size:"+cityList.size()+"  seleProvinId"+selectedProvince.getId());
		cityList = coolWeatherDB.loadCities(selectedProvince.getId());
		Log.e("haha", String.valueOf(cityList.size()));
		if(cityList.size()>0){
			dataList.clear();
			for(City c : cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selectedProvince.getProvinceCode(),"city");
		}
		Log.e("haha", "queryCities()方法执行完毕 ProvinceCode:"+selectedProvince.getProvinceCode().toString());
	}
	/**
	 * 查询全国所有的县，优先从数据库查找，没有的话再去服务器上查找
	 */
	private void queryCounties(){
		Log.e("haha", "进入queryCounties()方法");
		countyList = coolWeatherDB.loadCounty(selectedCity.getId());
		if(countyList.size()>0){
			dataList.clear();
			for(County c :countyList){
				dataList.add(c.getCountyNmae());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedCity.getCityName());
			currentLevel = LEVEL_COUNTY;
		}else{
			queryFromServer(selectedCity.getCityCode(),"county");
		}
		Log.e("haha", "退出queryCounties()方法");
	}
	/**
	 * 根据传入的代号和类型从服务器上查询省市县数据
	 */
	private void queryFromServer(final String code,final String type){
		Log.e("haha","进入queryFromServer()方法");
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city"+code+".xml";
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if("province".equals(type)){
					result = Utility.handleProvincesResponse(coolWeatherDB, response);
				}else if("city".equals(type)){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selectedProvince.getId());
				}else if("county".equals(type)){
					result = Utility.handleCountiesResponse(coolWeatherDB, response, selectedCity.getId());
				}
				if(!result)
					Log.e("haha", "false");
				else
					Log.e("haha", "true");
				if(result){
					//通过runOnUiThread()方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								Log.e("haha", "主线程执行queryCities");
								queryCities();
							}else if("county".equals(type)){
								queryCounties();
							}
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
				
			}
		});
		Log.e("haha","queryFromServer()方法 re:");
	}
	
	/**
	 * 显示进度对话框
	 */
	private void showProgressDialog() {
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	/**
	 * 关闭进度对话框
	 */
	private void closeProgressDialog(){
		if(progressDialog!=null)
			progressDialog.dismiss();
	}
	
	/**
	 * 捕获Back按键 
	 */
	@Override
	public void onBackPressed() {
		if(currentLevel==LEVEL_COUNTY){
			queryCities();
		}else if(currentLevel==LEVEL_CITY){
			queryProvinces();
		}else{
			finish();
		}
	}
}
