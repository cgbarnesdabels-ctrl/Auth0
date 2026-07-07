package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.TaskRepository
import com.example.ui.TaskCalendarApp
import com.example.ui.TaskViewModel
import com.example.ui.TaskViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Local SQLite Database and Repository
        val database = AppDatabase.getDatabase(this)
        val repository = TaskRepository(database.authAppDao())
        
        // Instantiate the Shared ViewModel
        val viewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                TaskCalendarApp(viewModel = viewModel)
            }
        }
    }
}
