package com.example.tracker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.tracker.model.UserModel
import com.example.tracker.receivers.HabitAlarmReceiver
import com.example.tracker.utils.NotificationUtils
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import java.util.Calendar


class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth

    private val firebase = Firebase.firestore


    fun login(email: String, password: String, onResult: (Boolean,String?)-> Unit ){

        if(email.isEmpty() || password.isEmpty()){
            onResult(false, "Email and password cannot be empty")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    onResult(true, null)
                }else{
                    onResult(false, task.exception?.message ?: "Unknown error")
                }
            }

    }

    fun signup(email: String, password: String, confirmPassword: String, onResult: (Boolean,String?)-> Unit ){

        if(email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()){
            onResult(false, "Email and password cannot be empty")
            return
        }

        if(password != confirmPassword){
            onResult(false, "Passwords do not match")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){

                    val userID = task.result.user?.uid
                    val userModel = UserModel(email, userID!!)

                    firebase.collection("users")
                        .document(userID)
                        .set(userModel)
                        .addOnCompleteListener { dbTask ->
                            if(dbTask.isSuccessful){
                                onResult(true,null)
                            }else{
                                onResult(false, dbTask.exception?.message ?: "Unknown error")
                            }
                        }

                }else{
                    onResult(false, task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun logout(){
        auth.signOut()
    }

}
