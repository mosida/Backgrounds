package com.galeapp.utils;

import java.net.URI;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

public class HttpUtil {

	/**
	 * apn wifi
	 */
	static final String APN_WIFI = "wifi";
	/**
	 * apn cmnet
	 */
	static final String APN_CMNET = "cmnet";
	/**
	 * apn cmwap
	 */
	static final String APN_CMWAP = "cmwap";
	/**
	 * apn uninet
	 */
	static final String APN_UNINET = "uninet";
	/**
	 * apn uniwap
	 */
	static final String APN_UNIWAP = "uniwap";
	/**
	 * apn 3gnet
	 */
	static final String APN_3GNET = "3gnet";
	/**
	 * apn 3gwap
	 */
	static final String APN_3GWAP = "3gwap";
	/**
	 * apn #777 ctnet
	 */
	static final String APN_CTNET = "#777";
	/**
	 * apn #777 ctwap
	 */
	static final String APN_CTWAP = "#777";
	/**
	 * apn internet
	 */
	static final String APN_INTERNET = "internet";
	/**
	 * apn unknow
	 */
	static final String APN_UNKNOW = "";

	/**
	 * 获取APN名字
	 * 
	 * @param context
	 * @return
	 */
	static String getApn(Context context) {
		try {

			if (PermissionUtil.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
				// 具有网络访问权限时才可以获取apn

				ConnectivityManager connectivityManager = (ConnectivityManager) context
						.getSystemService(Context.CONNECTIVITY_SERVICE);

				NetworkInfo activeNetworkInfo = connectivityManager
						.getActiveNetworkInfo();
				if (activeNetworkInfo != null) {
					if (activeNetworkInfo.isAvailable()) {
						if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
							// 移动网络
							// 判断接入点
							String _apn = activeNetworkInfo.getExtraInfo();
							if (_apn != null) {
								_apn = _apn.trim().toLowerCase();
								//
								if (_apn.length() > 10) {
									return _apn.substring(0, 10);
								}
								return _apn;
							} else {
								// 未知接入点，返回 APN_UNKNOW
								return APN_UNKNOW;
							}
						} else {
							return APN_WIFI;
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return APN_UNKNOW;
	}

	/**
	 * 检查当前网络是否可用 1.检查联网权限 2.检查网络状态
	 * 
	 * @param context
	 * @return
	 */
	static boolean isNetworkAvailable(Context context) {
		if (!PermissionUtil.isWith_INTERNET_Permission(context)) {
			// 不具有连接网络权限
			return false;
		}
		return isNetworkAvailable_WIth_INTERNET_Permission(context);
	}

	/**
	 * 在具有联网权限基础上检查当前网络是否可用
	 * 
	 * @param context
	 * @return
	 */
	static boolean isNetworkAvailable_WIth_INTERNET_Permission(Context context) {

		if (!PermissionUtil.isWith_ACCESS_NETWORK_STATE_Permission(context)) {
			// 不具有检查网络情况的权限，只能返回true
			return true;
		}
		try {
			ConnectivityManager connectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo activeNetworkInfo = connectivityManager
					.getActiveNetworkInfo();
			if (activeNetworkInfo != null) {
				if (activeNetworkInfo.isAvailable()) {
					// 经检查，网络可用
					return true;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;
	}

	private static String userAgent;

	/**
	 * 获取UserAgent
	 * 
	 * @return
	 */
	static String getUserAgent() {
		if (userAgent == null) {
			try {
				Locale l = Locale.getDefault();
				String localeLanguage = String.format("%s-%s", l.getLanguage(),
						l.getCountry());
				StringBuilder sb = new StringBuilder(256);
				sb.append("Mozilla/5.0 (Linux; U; Android ");
				sb.append(Build.VERSION.RELEASE);
				sb.append("; ");
				sb.append(localeLanguage);
				sb.append("; ");
				sb.append(Build.MODEL);
				sb.append(" Build/");
				sb.append(Build.ID);
				sb.append(") AppleWebkit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
				userAgent = sb.toString();
			} catch (Exception e) {
				return "";
			}
		}
		return userAgent;
	}

	/**
	 * 创建http请求参数
	 * 
	 * @return
	 */
	static HttpParams createHttpParams(Context context) {
		BasicHttpParams params = new BasicHttpParams();
		// 设置处理自动处理重定向
		HttpClientParams.setRedirecting(params, true);
		// 设置userAgent
		HttpProtocolParams.setUserAgent(params, getUserAgent());
		// 设置utf-8(待测试)
		HttpProtocolParams.setContentCharset(params, "utf-8");
		// 设置utf-8(待测试)
		HttpProtocolParams.setHttpElementCharset(params, "utf-8");
		// 为cmwap设置代理
		String apn = HttpUtil.getApn(context);
		if (apn.equals(HttpUtil.APN_CMWAP)) {
			HttpHost proxy = new HttpHost("10.0.0.172", 80, null);
			params.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}
		return params;
	}

	/**
	 * 创建DefaultHttpClient
	 * 
	 * @param context
	 * @return
	 */
	static DefaultHttpClient createHttpClient(Context context) {

		DefaultHttpClient httpClient = new DefaultHttpClient(
				createHttpParams(context));

		// 设置重定向处理，目的是获得重定向后的地址
		httpClient.setRedirectHandler(new RedirectHandler() {

			@Override
			public boolean isRedirectRequested(HttpResponse response,
					HttpContext context) {
				// TODO Auto-generated method stub
				int statusCode = response.getStatusLine().getStatusCode();
				if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
						|| (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
						|| (statusCode == HttpStatus.SC_SEE_OTHER)
						|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
					// 此处重定向处理
					return true;
				}
				return false;
			}

			@Override
			public URI getLocationURI(HttpResponse response, HttpContext context)
					throws ProtocolException {
				Header header = null;
				if (response.containsHeader("location")) {
					header = response.getFirstHeader("location");
				} else {
					if (response.containsHeader("Location")) {
						header = response.getFirstHeader("Location");
					} else {
						if (response.containsHeader("LOCATION")) {
							header = response.getFirstHeader("LOCATION");
						}
					}
				}
				if (header == null) {
					return null;
				}
				String url = header.getValue();
				if (url == null) {
					return null;
				}
				return URI.create(url);
			}
		});
		return httpClient;
	}

}
