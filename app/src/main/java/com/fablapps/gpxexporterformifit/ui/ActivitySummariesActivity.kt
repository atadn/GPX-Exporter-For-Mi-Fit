package com.fablapps.gpxexporterformifit.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.anjlab.android.iab.v3.TransactionDetails
import com.fablapps.gpxexporterformifit.R
import com.fablapps.gpxexporterformifit.adapters.ActivityListAdapter
import com.fablapps.gpxexporterformifit.export.ExportDialog
import com.fablapps.gpxexporterformifit.helpers.*
import com.fablapps.gpxexporterformifit.helpers.GetDataAsyncTask.AsyncResponse
import com.fablapps.gpxexporterformifit.helpers.BillingHelper
import com.fablapps.gpxexporterformifit.models.DataActivity
import com.fablapps.gpxexporterformifit.util.Constants
import com.fablapps.gpxexporterformifit.util.ItemClickSupport
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_summaries_activity.*
import kotlinx.android.synthetic.main.content_summaries.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import xyz.sangcomz.stickytimelineview.RecyclerSectionItemDecoration
import xyz.sangcomz.stickytimelineview.TimeLineRecyclerView
import xyz.sangcomz.stickytimelineview.model.SectionInfo
import java.util.*
import kotlin.collections.ArrayList

class ActivitySummariesActivity : AppCompatActivity(), AsyncResponse, NavigationView.OnNavigationItemSelectedListener, BillingHelper.billingListener{

    private lateinit var mInterstitialAd: InterstitialAd

    private var adapter: RecyclerView.Adapter<*>? = null

    private val dataActivityList = ArrayList<DataActivity>()
    private lateinit var dataList: ArrayList<HashMap<String, String>>

    private lateinit var db: DatabaseHelper

    private lateinit var recyclerView: TimeLineRecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout

    private lateinit var noRootAccessTextView: TextView
    private lateinit var noDataTextview: TextView

    private lateinit var progressBar: ProgressBar

    private var filterMenu: MenuItem? = null

    private var isRefreshing: Boolean = false

    private lateinit var eventDateAndDis: String
    private lateinit var eventTime: String
    private lateinit var eventSavedTime: String
    private lateinit var eventTypeString: String
    private var position: Int? = null

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summaries_activity)

        initComponent()
        initAds()

        getDataAsync()

        ItemClickSupport.addTo(recyclerView).setOnItemClickListener { _, position, _ ->
            this.position = position

            eventTypeString = ToolHelper.getEventTypeString(applicationContext, dataActivityList[position].type)
            eventDateAndDis = eventTypeString + " " + ToolHelper.getEventDistance(dataActivityList[position].distance.toDouble())
            eventTime = ToolHelper.getActivityUsedTime(dataActivityList[position].trackId, dataActivityList[position].endTime)
            eventSavedTime = ToolHelper.getSavedTimeHours(applicationContext, dataActivityList[position].trackId.toLong())

            if (!PrefManager.getBoolean(Constants.NOADS_BILLING_ID)) {
                if (mInterstitialAd.isLoaded) {
                    mInterstitialAd.show()
                } else {
                    showExportDialog()
                }
            } else {
                showExportDialog()
            }
        }

        refreshLayout.setOnRefreshListener {
            db.closeDb()
            isRefreshing = true
            getDataAsync()
        }

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                showExportDialog()
            }
        }

    }

    @SuppressLint("WrongConstant")
    private fun initComponent() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.title = resources.getString(R.string.activity_summaries_title)
        supportActionBar?.subtitle = resources.getString(R.string.app_name)

        EventBus.getDefault().register(this)

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        db = DatabaseHelper(this, getExternalFilesDir(null)!!.path + Constants.DATABASE_PATH)
        BillingHelper(this@ActivitySummariesActivity, Constants.LICENSE_KEY_BILLING, this, null)
        BillingHelper.bpInitialize()
        PrefManager(this)

        progressBar = findViewById(R.id.progress_activity_summaries)
        recyclerView = findViewById(R.id.view_activity_summaries)
        refreshLayout = findViewById(R.id.swipeRefreshLayout)
        noRootAccessTextView = findViewById(R.id.text_no_root_access)
        noDataTextview = findViewById(R.id.text_no_data)

        recyclerView.layoutManager = LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL,
                false)
    }

    private fun initAds() {
        MobileAds.initialize(this,
                Constants.ADMOB_GENERAL_ID)

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = Constants.ADMOB_AD_ID

        if (!PrefManager.getBoolean(Constants.NOADS_BILLING_ID)) {
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }
    }

    private fun showExportDialog() {
        ExportDialog.openActivityDetailDialog(this@ActivitySummariesActivity, eventDateAndDis, eventTime, eventSavedTime, dataActivityList[this.position!!].trackId)
    }

    private fun getEventAll() {
        db.opendb()

        if (isRefreshing) {
            filterMenuClear()
        }

        dataList = db.allData()

        if (dataList.size != 0) {
            for (i in dataList.indices) {
                dataActivityFill(i)
            }
            showActivitySummaries()
        } else {
            showNoData()
        }
    }

    private fun filterMenuClear() {
        if (dataActivityList.isNotEmpty() && dataList.isNotEmpty()) {
            dataList.clear()
            dataActivityList.clear()
        }

        filterMenu?.subMenu?.getItem(0)?.isChecked = true
        filterMenu?.subMenu?.getItem(1)?.isChecked = true
        filterMenu?.subMenu?.getItem(2)?.isChecked = true
        filterMenu?.subMenu?.getItem(3)?.isChecked = true
        filterMenu?.subMenu?.getItem(4)?.isChecked = true

        isRefreshing = false
    }

    private fun getEventFilterRemove(type: Int) {
        with(dataActivityList.iterator()) {
            forEach { if (it.type == type) remove() }
        }

        notifyDataSetChanged()
    }

    private fun getEventFilterAdd(type: String) {

        for (i in dataList.indices) {
            if (dataList[i]["TYPE"] == type) {
                dataActivityFill(i)
            }
        }

        dataActivityList.sortByDescending { it.date }

        notifyDataSetChanged()
    }

    private fun dataActivityFill(i: Int) {
        dataActivityList.add(
                DataActivity(
                        Integer.parseInt(dataList[i]["_id"]!!),
                        Integer.parseInt(dataList[i]["TRACKID"]!!),
                        Integer.parseInt(dataList[i]["TYPE"]!!),
                        Integer.parseInt(dataList[i]["DISTANCE"]!!),
                        Integer.parseInt(dataList[i]["COSTTIME"]!!),
                        Integer.parseInt(dataList[i]["ENDTIME"]!!),
                        dataList[i]["DATE"]))
    }

    private fun showActivitySummaries() {
        filterMenu?.isVisible = true

        if (recyclerView.itemDecorationCount < 1) {
            recyclerView.addItemDecoration(getSectionCallback(dataActivityList))
        }

        adapter = ActivityListAdapter(dataActivityList)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adapter

        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        noRootAccessTextView.visibility = View.GONE
        noDataTextview.visibility = View.GONE

        notifyDataSetChanged()
    }

    private fun showNoRootAccessInfo() {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        noRootAccessTextView.visibility = View.VISIBLE
    }

    private fun showNoData() {
        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE
        noRootAccessTextView.visibility = View.GONE
        noDataTextview.visibility = View.VISIBLE
    }

    private fun notifyDataSetChanged() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun getDataAsync() {
        GetDataAsyncTask(this, getExternalFilesDir(null)).execute()
    }

    private fun getSectionCallback(dataLocationList: List<DataActivity>): RecyclerSectionItemDecoration.SectionCallback {
        return object : RecyclerSectionItemDecoration.SectionCallback {
            override fun isSection(position: Int): Boolean =
                    dataLocationList[position].date != dataLocationList[position - 1].date

            override fun getSectionHeader(position: Int): SectionInfo? =
                    SectionInfo(dataLocationList[position].date)
        }
    }

    private fun startPurchase() {
        if (BillingHelper.isReadyToPurchase()) {
            if (BillingHelper.isIabServiceAvailable(this)) {
                if (BillingHelper.isPurchased(Constants.NOADS_BILLING_ID)) {
                    Toast.makeText(this, resources.getString(R.string.billing_purchased), Toast.LENGTH_SHORT).show()
                    if (!PrefManager.getBoolean(Constants.NOADS_BILLING_ID)) {
                        PrefManager.putBoolean(Constants.NOADS_BILLING_ID, true)
                    }
                } else {
                   BillingHelper.bpPurchase(this, Constants.NOADS_BILLING_ID)
                }
            } else {
                Toast.makeText(this, resources.getString(R.string.billing_error_gms), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.billing_error_gms), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!BillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        Toast.makeText(applicationContext, resources.getString(R.string.billing_now_purchased), Toast.LENGTH_SHORT).show()
        PrefManager.putBoolean(Constants.NOADS_BILLING_ID, true)
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        if (errorCode > 1) {
            Toast.makeText(applicationContext, resources.getString(R.string.billing_purchase_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun processFinish(output: Int) {
        when (output) {
            GetDataAsyncTask.PROCESS_FINISH -> getEventAll()
            GetDataAsyncTask.NO_ROOT -> showNoRootAccessInfo()
            GetDataAsyncTask.NO_DB -> showNoData()
        }
        swipeRefreshLayout.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        filterMenu = menu.findItem(R.id.filter)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.run_item -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    getEventFilterAdd(Constants.TYPE_RUN.toString())
                } else {
                    getEventFilterRemove(Constants.TYPE_RUN)
                }
                return true
            }
            R.id.walk_item -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    getEventFilterAdd(Constants.TYPE_WALK.toString())
                } else {
                    getEventFilterRemove(Constants.TYPE_WALK)
                }
                return true
            }
            R.id.trail_item -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    getEventFilterAdd(Constants.TYPE_TRAIL.toString())
                } else {
                    getEventFilterRemove(Constants.TYPE_TRAIL)
                }
                return true
            }
            R.id.cycling_item -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    getEventFilterAdd(Constants.TYPE_CYCLING.toString())
                } else {
                    getEventFilterRemove(Constants.TYPE_CYCLING)
                }
                return true
            }
            R.id.other_item -> {
                item.isChecked = !item.isChecked
                if (item.isChecked) {
                    getEventFilterAdd(Constants.TYPE_OTHER.toString())
                } else {
                    getEventFilterRemove(Constants.TYPE_OTHER)
                }
                return true
            }
            R.id.help -> {
                DialogHelp.openHelpDialog(this)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.share -> {
                ToolHelper.openShareDialog(this@ActivitySummariesActivity)
            }
            R.id.feedback -> {
                ToolHelper.openMail(this@ActivitySummariesActivity, resources.getString(R.string.dialogmail_email), resources.getString(R.string.dialogmail_subject), resources.getString(R.string.dialogmail_title))
            }
            R.id.rateit -> {
                ToolHelper.openRate(this@ActivitySummariesActivity)
            }
            R.id.noads -> {
                ToolHelper.openBuyDialog(this)
            }
            R.id.about -> {
                ToolHelper.openAboutDialog(this)
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(action: String) {
        when (action) {
            "btn_buy" -> startPurchase()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
        BillingHelper.bpRelease()
        db.closeDb()
    }
}


