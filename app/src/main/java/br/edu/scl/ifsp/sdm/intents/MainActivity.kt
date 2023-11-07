package br.edu.scl.ifsp.sdm.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.Intent.ACTION_CHOOSER
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_PICK
import android.content.Intent.ACTION_VIEW
import android.content.Intent.EXTRA_INTENT
import android.content.Intent.EXTRA_TITLE
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import br.edu.scl.ifsp.sdm.intents.Extras.PARAMETER_EXTRA
import br.edu.scl.ifsp.sdm.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val activityMainBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private lateinit var parameterArl: ActivityResultLauncher<Intent>
    private lateinit var callPhonePermissionArl: ActivityResultLauncher<String>
    private lateinit var pickImageArl: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activityMainBinding.root)
        setSupportActionBar(activityMainBinding.toolbarIn.toolbar)
        supportActionBar?.subtitle = localClassName

        parameterArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                it.data?.getStringExtra(PARAMETER_EXTRA).also {
                    activityMainBinding.parameterTv.text = it
                }
            }
        }

        callPhonePermissionArl = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                callPhone(true)
            } else {
                Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_SHORT).show()
            }
        }

        pickImageArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            with (it) {
                if (resultCode == RESULT_OK) {
                    data?.data?.also {
                        activityMainBinding.parameterTv.text = it.toString()
                        startActivity(Intent(ACTION_VIEW).apply { data = it })
                    }
                }
            }
        }

        activityMainBinding.apply {
            parameterBt.setOnClickListener {
                Intent(this@MainActivity, ParameterActivity::class.java).apply {
                    putExtra(PARAMETER_EXTRA, parameterTv.text)
                    parameterArl.launch(this)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.openActivityMi -> {
                Intent("OPEN_PARAMETER_ACTIVITY_ACTION").apply {
                    putExtra(PARAMETER_EXTRA, activityMainBinding.parameterTv.text)
                    parameterArl.launch(this)
                }
                true
            }
            R.id.viewMi -> {
                startActivity(browserIntent())
                true
            }

            R.id.callMi -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(CALL_PHONE) == PERMISSION_GRANTED) {
                        callPhone(true)
                    } else {
                        callPhonePermissionArl.launch(CALL_PHONE)
                    }
                } else {
                    callPhone(true)
                }
                true
            }

            R.id.dialMi -> {
                callPhone(false)
                true
            }

            R.id.pickMi -> {
                val imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                pickImageArl.launch(Intent(ACTION_PICK).apply { setDataAndType(Uri.parse(imageDir), "image/*") })
                true
            }

            R.id.chooserMi -> {
                startActivity(
                    Intent(ACTION_CHOOSER).apply {
                        putExtra(EXTRA_TITLE, getString(R.string.choose_your_favorite_browser))
                        putExtra(EXTRA_INTENT, browserIntent())
                    }
                )
                true
            }

            else -> {
                false
            }
        }
    }

    private fun callPhone(shouldCall: Boolean) {
        startActivity(Intent(if (shouldCall) ACTION_CALL else ACTION_DIAL).apply {
            "tel: ${activityMainBinding.parameterTv.text}".also {
                data = Uri.parse(it)
            }
        })
    }

    private fun browserIntent(): Intent {
        val url = Uri.parse(activityMainBinding.parameterTv.text.toString())
        return Intent(ACTION_VIEW, url)
    }
}