package com.pradeep.appstats

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    private var installedApps: List<AppList>? = null
    private var installedAppAdapter: AppAdapter? = null
    var userInstalledApps: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userInstalledApps = findViewById<View>(R.id.installed_app_list) as ListView

        installedApps = getInstalledApps()
        installedAppAdapter = AppAdapter(this@MainActivity, installedApps)
        userInstalledApps!!.adapter = installedAppAdapter
        userInstalledApps!!.onItemClickListener =
            OnItemClickListener { _, _, i, _ ->
                val colors = arrayOf(" Open App", " App Info")
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle("Choose Action")
                    .setItems(
                        colors
                    ) { _, which ->
                        if (which == 0) {
                            val intent = packageManager.getLaunchIntentForPackage(
                                installedApps!![i].packages
                            )
                            if (intent != null) {
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    installedApps!![i].packages + " Error, Please Try Again...",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        if (which == 1) {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            intent.data = Uri.parse("package:" + installedApps!![i].packages)
                            Toast.makeText(
                                this@MainActivity,
                                installedApps!![i].packages,
                                Toast.LENGTH_SHORT
                            ).show()
                            startActivity(intent)
                        }
                    }
                builder.show()
            }

        val abc = userInstalledApps!!.count.toString() + ""
        val countApps = findViewById<View>(R.id.countApps) as TextView
        countApps.text = "Total Installed Apps: $abc"
        Toast.makeText(this, "$abc Apps", Toast.LENGTH_SHORT).show()

    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getInstalledApps(): List<AppList>? {
        val pm = packageManager
        val apps: MutableList<AppList> = ArrayList()
        val packs = packageManager.getInstalledPackages(0)
        //List<PackageInfo> packs = getPackageManager().getInstalledPackages(PackageManager.GET_PERMISSIONS);
        for (i in packs.indices) {
            val p = packs[i]
            if (!isSystemPackage(p)) {
                val appName = p.applicationInfo.loadLabel(packageManager).toString()
                val icon = p.applicationInfo.loadIcon(packageManager)
                val packages = p.applicationInfo.packageName
                apps.add(AppList(appName, icon, packages))
            }
        }
        return apps
    }

    private fun isSystemPackage(pkgInfo: PackageInfo): Boolean {
        return pkgInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
    }
}