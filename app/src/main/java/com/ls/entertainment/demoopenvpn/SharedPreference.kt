package com.ls.entertainment.demoopenvpn

import android.content.Context
import android.content.SharedPreferences
import com.ls.entertainment.demoopenvpn.Utils.getImgURL

class SharedPreference(context: Context) {
	private val mPreference: SharedPreferences
	private val mPrefEditor: SharedPreferences.Editor
	private val context: Context
	
	init {
		mPreference = context.getSharedPreferences(APP_PREFS_NAME, Context.MODE_PRIVATE)
		mPrefEditor = mPreference.edit()
		this.context = context
	}
	
	/**
	 * Save server details
	 * @param server details of ovpn server
	 */
	fun saveServer(server: Server) {
		mPrefEditor.putString(SERVER_COUNTRY, server.country)
		mPrefEditor.putString(SERVER_FLAG, server.flagUrl)
		mPrefEditor.putString(SERVER_OVPN, server.ovpn)
		mPrefEditor.putString(SERVER_OVPN_USER, server.ovpnUserName)
		mPrefEditor.putString(SERVER_OVPN_PASSWORD, server.ovpnUserPassword)
		mPrefEditor.commit()
	}
	
	/**
	 * Get server data from shared preference
	 * @return server model object
	 */
	val server: Server
		get() = Server(
			mPreference.getString(SERVER_COUNTRY, "Japan"),
			mPreference.getString(SERVER_FLAG, getImgURL(R.drawable.japan)),
			mPreference.getString(SERVER_OVPN, "japan.ovpn"),
			mPreference.getString(SERVER_OVPN_USER, "vpn"),
			mPreference.getString(SERVER_OVPN_PASSWORD, "vpn")
		)
	
	companion object {
		private const val APP_PREFS_NAME = "CakeVPNPreference"
		private const val SERVER_COUNTRY = "server_country"
		private const val SERVER_FLAG = "server_flag"
		private const val SERVER_OVPN = "server_ovpn"
		private const val SERVER_OVPN_USER = "server_ovpn_user"
		private const val SERVER_OVPN_PASSWORD = "server_ovpn_password"
	}
}