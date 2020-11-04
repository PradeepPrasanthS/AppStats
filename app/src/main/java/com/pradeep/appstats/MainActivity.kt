package com.pradeep.appstats

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainActivity : AppCompatActivity() {

    private var installedApps: List<AppList>? = null
    private var installedAppAdapter: AppAdapter? = null
    var userInstalledApps: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userInstalledApps = findViewById<View>(R.id.installed_app_list) as ListView

        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )

        if (mode == AppOpsManager.MODE_ALLOWED) {
            loadInstalledApps()
        } else {
            startActivityForResult(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
                MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS
            )
        }

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledApps(): List<AppList>? {
        val apps: MutableList<AppList> = ArrayList()
        val packs = packageManager.getInstalledPackages(0)
        val mUsageStatsManager: UsageStatsManager =
            getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager

        val time = Date().time
        val lUsageStatsMap = mUsageStatsManager
            .queryAndAggregateUsageStats(
                UsageStatsManager.INTERVAL_DAILY.toLong(),
                time - 1000 * 1000
            )

        for (i in packs.indices) {
            val p = packs[i]
            if (!isSystemPackage(p)) {
                val appName = p.applicationInfo.loadLabel(packageManager).toString()
                val icon = p.applicationInfo.loadIcon(packageManager)
                val packages = p.applicationInfo.packageName
                val totalTimeUsageInMillis = lUsageStatsMap[packages]?.totalTimeInForeground ?: 0
                val hms = String.format(
                    "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(totalTimeUsageInMillis),
                    TimeUnit.MILLISECONDS.toMinutes(totalTimeUsageInMillis) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(
                            totalTimeUsageInMillis
                        )
                    ),
                    TimeUnit.MILLISECONDS.toSeconds(totalTimeUsageInMillis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            totalTimeUsageInMillis
                        )
                    )
                )

                apps.add(AppList(appName, icon, hms))
            }
        }
        return apps
    }

    private fun loadInstalledApps() {
        installedApps = getInstalledApps()
        installedAppAdapter = AppAdapter(this@MainActivity, installedApps)
        userInstalledApps!!.adapter = installedAppAdapter

        val count = userInstalledApps!!.count.toString() + ""
        val countApps = findViewById<View>(R.id.countApps) as TextView
        countApps.text = getString(R.string.total_count, count)
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }

    companion object {
        const val MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 100
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == 1) {
            loadInstalledApps()
        }
    }
}