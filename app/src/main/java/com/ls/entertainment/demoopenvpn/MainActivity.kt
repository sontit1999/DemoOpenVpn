package com.ls.entertainment.demoopenvpn

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ls.entertainment.demoopenvpn.Utils.getImgURL
import com.ls.entertainment.demoopenvpn.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), NavItemClickListener {
	private val transaction = supportFragmentManager.beginTransaction()
	private var fragment: Fragment? = null
	private var serverListRv: RecyclerView? = null
	lateinit var serverLists: ArrayList<Server>
	private var serverListRVAdapter: ServerListRVAdapter? = null
	private var drawer: DrawerLayout? = null
	private var changeServer: ChangeServer? = null
	lateinit var binding : ActivityMainBinding
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
		
		// Initialize all variable
		initializeAll()
		val menuRight = findViewById<ImageButton>(R.id.navbar_right)
		val toolbar = findViewById<Toolbar>(R.id.toolbar)
		setSupportActionBar(toolbar)
		supportActionBar!!.setDisplayShowTitleEnabled(false)
		val toggle = ActionBarDrawerToggle(
			this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
		)
		drawer!!.addDrawerListener(toggle)
		menuRight.setOnClickListener { closeDrawer() }
		transaction.add(R.id.container, fragment!!)
		transaction.commit()
		
		// Server List recycler view initialize
		serverListRVAdapter = ServerListRVAdapter(serverLists!!, this)
		serverListRv!!.adapter = serverListRVAdapter
	}
	
	/**
	 * Initialize all object, listener etc
	 */
	private fun initializeAll() {
		drawer = findViewById(R.id.drawer_layout)
		fragment = MainFragment()
		serverListRv = findViewById(R.id.serverListRv)
		binding.rightMenu.serverListRv.setHasFixedSize(true)
		binding.rightMenu.serverListRv.layoutManager = LinearLayoutManager(this)
		serverLists = ArrayList()
		serverLists.addAll(serverList)
		changeServer = fragment as ChangeServer?
	}
	
	/**
	 * Close navigation drawer
	 */
	fun closeDrawer() {
		if (drawer!!.isDrawerOpen(GravityCompat.END)) {
			drawer!!.closeDrawer(GravityCompat.END)
		} else {
			drawer!!.openDrawer(GravityCompat.END)
		}
	}
	
	/**
	 * Generate server array list
	 */
	private val serverList: ArrayList<Server>
		private get() {
			val servers = ArrayList<Server>()
			servers.add(
				Server(
					"United States",
					getImgURL(R.drawable.usa_flag),
					"us.ovpn",
					"freeopenvpn",
					"467586123"
				)
			)
			servers.add(
				Server(
					"Japan", getImgURL(R.drawable.japan), "japan.ovpn", "", ""
				)
			)
			servers.add(
				Server(
					"Sweden", getImgURL(R.drawable.sweden), "sweden.ovpn", "", ""
				)
			)
			servers.add(
				Server(
					"Korea", getImgURL(R.drawable.korea), "korea.ovpn", "", ""
				)
			)
			return servers
		}
	
	/**
	 * On navigation item click, close drawer and change server
	 * @param index: server index
	 */
	override fun clickedItem(index: Int) {
		closeDrawer()
		changeServer!!.newServer(serverLists[index])
	}
	
	companion object {
		const val TAG = "CakeVPN"
	}
}