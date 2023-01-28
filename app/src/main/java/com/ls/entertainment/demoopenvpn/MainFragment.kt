package com.ls.entertainment.demoopenvpn

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.RemoteException
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.ls.entertainment.demoopenvpn.CheckInternetConnection.netCheck
import com.ls.entertainment.demoopenvpn.databinding.FragmentMainBinding
import de.blinkt.openvpn.OpenVpnApi
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import de.blinkt.openvpn.core.VpnStatus
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainFragment : Fragment(), View.OnClickListener, ChangeServer {
	private var server: Server? = null
	private val vpnThread = OpenVPNThread()
	private val vpnService = OpenVPNService()
	var vpnStart = false
	private var preference: SharedPreference? = null
	lateinit var binding: FragmentMainBinding
	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
	): View {
		binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
		val view = binding.root
		initializeAll()
		return view
	}
	
	/**
	 * Initialize all variable and object
	 */
	private fun initializeAll() {
		preference = SharedPreference(requireContext())
		server = preference!!.server
		
		// Update current selected server icon
		updateCurrentServerIcon(server!!.flagUrl)
	}
	
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.vpnBtn.setOnClickListener(this)
		
		// Checking is vpn already running or not
		isServiceRunning
		VpnStatus.initLogCache(requireActivity().cacheDir)
	}
	
	/**
	 * @param v: click listener view
	 */
	override fun onClick(v: View) {
		when (v.id) {
			R.id.vpnBtn ->                 // Vpn is running, user would like to disconnect current connection.
				if (vpnStart) {
					confirmDisconnect()
				} else {
					prepareVpn()
				}
		}
	}
	
	/**
	 * Show show disconnect confirm dialog
	 */
	fun confirmDisconnect() {
		val builder = AlertDialog.Builder(
			requireActivity()
		)
		builder.setMessage(requireActivity().getString(R.string.connection_close_confirm))
		builder.setPositiveButton(requireActivity().getString(R.string.yes)) { dialog, id -> stopVpn() }
		builder.setNegativeButton(requireActivity().getString(R.string.no)) { dialog, id ->
			// User cancelled the dialog
		}
		
		// Create the AlertDialog
		val dialog = builder.create()
		dialog.show()
	}
	
	/**
	 * Prepare for vpn connect with required permission
	 */
	private fun prepareVpn() {
		if (!vpnStart) {
			if (netCheck(requireContext())) {
				
				// Checking permission for network monitor
				val intent = VpnService.prepare(context)
				if (intent != null) {
					startActivityForResult(intent, 1)
				} else startVpn() //have already permission
				
				// Update confection status
				status("connecting")
			} else {
				
				// No internet connection available
				showToast("you have no internet connection !!")
			}
		} else if (stopVpn()) {
			
			// VPN is stopped, show a Toast message.
			showToast("Disconnect Successfully")
		}
	}
	
	/**
	 * Stop vpn
	 * @return boolean: VPN status
	 */
	fun stopVpn(): Boolean {
		try {
			OpenVPNThread.stop()
			status("connect")
			vpnStart = false
			return true
		} catch (e: Exception) {
			e.printStackTrace()
		}
		return false
	}
	
	/**
	 * Taking permission for network access
	 */
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == Activity.RESULT_OK) {
			
			//Permission granted, start the VPN
			startVpn()
		} else {
			showToast("Permission Deny !! ")
		}
	}
	
	/**
	 * Get service status
	 */
	val isServiceRunning: Unit
		get() {
			setStatus(OpenVPNService.getStatus())
		}
	
	/**
	 * Start the VPN
	 */
	private fun startVpn() {
		try {
			// .ovpn file
			val conf = requireActivity().assets.open(server!!.ovpn!!)
			val isr = InputStreamReader(conf)
			val br = BufferedReader(isr)
			var config = ""
			var line: String?
			while (true) {
				line = br.readLine()
				if (line == null) break
				config += """
					$line
					
					""".trimIndent()
			}
			br.readLine()
			OpenVpnApi.startVpn(
				context,
				config,
				server!!.country,
				server!!.ovpnUserName,
				server!!.ovpnUserPassword
			)
			
			// Update log
			binding.logTv.text = "Connecting..."
			vpnStart = true
		} catch (e: IOException) {
			e.printStackTrace()
		} catch (e: RemoteException) {
			e.printStackTrace()
		}
	}
	
	/**
	 * Status change with corresponding vpn connection status
	 * @param connectionState
	 */
	fun setStatus(connectionState: String?) {
		if (connectionState != null) when (connectionState) {
			"DISCONNECTED" -> {
				status("connect")
				vpnStart = false
				OpenVPNService.setDefaultStatus()
				binding.logTv.text = ""
			}
			"CONNECTED"    -> {
				vpnStart = true // it will use after restart this activity
				status("connected")
				binding.logTv.text = ""
			}
			"WAIT"         -> binding.logTv.text = "waiting for server connection!!"
			"AUTH"         -> binding.logTv.text = "server authenticating!!"
			"RECONNECTING" -> {
				status("connecting")
				binding.logTv.text = "Reconnecting..."
			}
			"NONETWORK"    -> binding.logTv.text = "No network connection"
		}
	}
	
	/**
	 * Change button background color and text
	 * @param status: VPN current status
	 */
	fun status(status: String) {
		if (status == "connect") {
			binding.vpnBtn.text = requireContext().getString(R.string.connect)
		} else if (status == "connecting") {
			binding.vpnBtn.text = requireContext().getString(R.string.connecting)
		} else if (status == "connected") {
			binding.vpnBtn.text = requireContext().getString(R.string.disconnect)
		} else if (status == "tryDifferentServer") {
			binding.vpnBtn.setBackgroundResource(R.drawable.button_connected)
			binding.vpnBtn.text = "Try Different\nServer"
		} else if (status == "loading") {
			binding.vpnBtn.setBackgroundResource(R.drawable.button)
			binding.vpnBtn.text = "Loading Server.."
		} else if (status == "invalidDevice") {
			binding.vpnBtn.setBackgroundResource(R.drawable.button_connected)
			binding.vpnBtn.text = "Invalid Device"
		} else if (status == "authenticationCheck") {
			binding.vpnBtn.setBackgroundResource(R.drawable.button_connecting)
			binding.vpnBtn.text = "Authentication \n Checking..."
		}
	}
	
	/**
	 * Receive broadcast message
	 */
	var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context, intent: Intent) {
			try {
				setStatus(intent.getStringExtra("state"))
			} catch (e: Exception) {
				e.printStackTrace()
			}
			try {
				var duration = intent.getStringExtra("duration")
				var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
				var byteIn = intent.getStringExtra("byteIn")
				var byteOut = intent.getStringExtra("byteOut")
				if (duration == null) duration = "00:00:00"
				if (lastPacketReceive == null) lastPacketReceive = "0"
				if (byteIn == null) byteIn = " "
				if (byteOut == null) byteOut = " "
				updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
	}
	
	/**
	 * Update status UI
	 * @param duration: running time
	 * @param lastPacketReceive: last packet receive time
	 * @param byteIn: incoming data
	 * @param byteOut: outgoing data
	 */
	fun updateConnectionStatus(
		duration: String,
		lastPacketReceive: String,
		byteIn: String,
		byteOut: String
	) {
		binding.durationTv.text = "Duration: $duration"
		binding.lastPacketReceiveTv.text = "Packet Received: $lastPacketReceive second ago"
		binding.byteInTv.text = "Bytes In: $byteIn"
		binding.byteOutTv.text = "Bytes Out: $byteOut"
	}
	
	/**
	 * Show toast message
	 * @param message: toast message
	 */
	fun showToast(message: String?) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
	}
	
	/**
	 * VPN server country icon change
	 * @param serverIcon: icon URL
	 */
	fun updateCurrentServerIcon(serverIcon: String?) {
		Glide.with(requireContext()).load(serverIcon).into(binding.selectedServerIcon)
	}
	
	/**
	 * Change server when user select new server
	 * @param server ovpn server details
	 */
	override fun newServer(server: Server) {
		this.server = server
		updateCurrentServerIcon(server.flagUrl)
		
		// Stop previous connection
		if (vpnStart) {
			stopVpn()
		}
		prepareVpn()
	}
	
	override fun onResume() {
		LocalBroadcastManager.getInstance(requireActivity())
			.registerReceiver(broadcastReceiver, IntentFilter("connectionState"))
		if (server == null) {
			server = preference!!.server
		}
		super.onResume()
	}
	
	override fun onPause() {
		LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(broadcastReceiver)
		super.onPause()
	}
	
	/**
	 * Save current selected server on local shared preference
	 */
	override fun onStop() {
		if (server != null) {
			preference!!.saveServer(server!!)
		}
		super.onStop()
	}
}