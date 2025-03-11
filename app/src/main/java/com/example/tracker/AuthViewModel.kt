package com.example.tracker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.tracker.model.UserModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore


class AuthViewModel : ViewModel() {

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val firebase = Firebase.firestore

    private val _authState = MutableLiveData<AuthState>()
    val authState : LiveData<AuthState> = _authState

    
    init {
        checkAuthState()
    }

    fun checkAuthState(){
        if(auth.currentUser != null){
            _authState.value = AuthState.Authenticated
        }else{
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun login(email: String, password: String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }

    }

    fun signup(email: String, password: String, confirmPassword: String){

        if(email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email and password cannot be empty")
            return
        }

        if(password != confirmPassword){
            _authState.value = AuthState.Error("Passwords do not match")
            return
        }

        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){

                    var uid = task.result.user?.uid
                    val userModel = UserModel(email, email, uid!!)

                    firebase.collection("users")
                        .document(uid)
                        .set(userModel)
                        .addOnCompleteListener { dbtask ->
                            if(dbtask.isSuccessful){
                                _authState.value = AuthState.Authenticated
                            }else{
                                _authState.value = AuthState.Error(dbtask.exception?.message ?: "Unknown error")
                            }
                        }
                    //_authState.value = AuthState.Authenticated
                }else{
                    _authState.value = AuthState.Error(task.exception?.message ?: "Unknown error")
                }
            }
    }

    fun logout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    object Unauthenticated : AuthState()
    object Authenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
}