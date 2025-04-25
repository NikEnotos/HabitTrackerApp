package com.example.tracker


import androidx.lifecycle.ViewModel
import com.example.tracker.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore


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
