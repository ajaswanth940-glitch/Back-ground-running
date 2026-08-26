package com.example.background

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        workManager = WorkManager.getInstance(this)
        progressBar = findViewById(R.id.progress_bar)
        tvStatus = findViewById(R.id.tv_status)

        findViewById<Button>(R.id.btn_one_time).setOnClickListener {
            startOneTimeWork()
        }

        findViewById<Button>(R.id.btn_periodic).setOnClickListener {
            schedulePeriodicWork()
        }
    }

    private fun startOneTimeWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val oneTimeWorkRequest = OneTimeWorkRequestBuilder<DataUpdateWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "oneTimeDataUpdate",
            ExistingWorkPolicy.REPLACE,
            oneTimeWorkRequest
        )

        observeWork(oneTimeWorkRequest.id)
    }

    private fun schedulePeriodicWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<DataUpdateWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "periodicDataUpdate",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWorkRequest
        )
        
        tvStatus.text = "Status: Periodic work scheduled"
    }

    private fun observeWork(workId: java.util.UUID) {
        workManager.getWorkInfoByIdLiveData(workId).observe(this) { workInfo ->
            if (workInfo != null) {
                val progress = workInfo.progress.getInt("progress", 0)
                progressBar.progress = progress
                
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> tvStatus.text = "Status: Enqueued"
                    WorkInfo.State.RUNNING -> tvStatus.text = "Status: Running ($progress%)"
                    WorkInfo.State.SUCCEEDED -> {
                        tvStatus.text = "Status: Succeeded"
                        progressBar.progress = 100
                    }
                    WorkInfo.State.FAILED -> tvStatus.text = "Status: Failed"
                    WorkInfo.State.CANCELLED -> tvStatus.text = "Status: Cancelled"
                    else -> tvStatus.text = "Status: ${workInfo.state}"
                }
            }
        }
    }
}
