package com.example.background

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay

class DataUpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d("DataUpdateWorker", "Starting background task...")
        
        // Indicate progress
        for (i in 1..10) {
            delay(500) // Simulate work
            setProgress(workDataOf("progress" to i * 10))
            Log.d("DataUpdateWorker", "Progress: ${i * 10}%")
        }

        // Simulate data update from an API
        Log.d("DataUpdateWorker", "Data updated successfully.")
        
        return Result.success()
    }
}
