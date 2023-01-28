package com.ls.entertainment.demoopenvpn

import android.content.Context
import android.net.ConnectivityManager

/**
 * This class is responsible for internet status checking
 */
object CheckInternetConnection {
	/**
	 * Check internet status
	 * @param context
	 * @return: internet connection status
	 */
	fun netCheck(context: Context): Boolean {
		val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
		val nInfo = cm.activeNetworkInfo
		return nInfo != null && nInfo.isConnectedOrConnecting
	}
}