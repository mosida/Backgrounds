package com.galeapp.utils;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * 权限检查工具
 */
public class PermissionUtil {
	/**
	 * 是否具有相应权限
	 * 
	 * @param context
	 * @param permissionName
	 * @return
	 */
	static boolean isWithPermission(Context context, String permissionName) {
		try {
			if (context.checkCallingOrSelfPermission(permissionName) == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return true;
	}

	/**
	 * 检查是否具有联网INTERNET权限
	 * 
	 * @param context
	 * @return
	 */
	static boolean isWith_INTERNET_Permission(Context context) {
		return isWithPermission(context, android.Manifest.permission.INTERNET);
	}

	/**
	 * 检查是否具有ACCESS_NETWORK_STATE权限
	 * 
	 * @param context
	 * @return
	 */
	static boolean isWith_ACCESS_NETWORK_STATE_Permission(Context context) {
		return isWithPermission(context,
				android.Manifest.permission.ACCESS_NETWORK_STATE);
	}

	/**
	 * 检查是否具有ACCESS_WIFI_STATE权限
	 * 
	 * @param context
	 * @return
	 */
	static boolean isWith_ACCESS_WIFI_STATE_Permission(Context context) {
		return isWithPermission(context,
				android.Manifest.permission.ACCESS_WIFI_STATE);
	}

}
