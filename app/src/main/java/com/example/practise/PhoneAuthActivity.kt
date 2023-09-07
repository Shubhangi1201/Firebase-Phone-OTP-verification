package com.example.practise

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sendOtpButton: Button
    private lateinit var phoneNumberEditText: EditText
    private lateinit var verifyOtpButton: Button
    private lateinit var otpEditText: EditText
    private lateinit var storedVerificationId: String



    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        sendOtpButton = findViewById(R.id.sentOtpBtn)
        phoneNumberEditText = findViewById(R.id.phoneET)
        verifyOtpButton = findViewById(R.id.verifyBtn)
        otpEditText = findViewById(R.id.OTPet)

        auth = FirebaseAuth.getInstance()

        sendOtpButton.setOnClickListener{
            sendVerificationCode(phoneNumberEditText.text.toString())
            Toast.makeText(applicationContext, "send verification code method called", Toast.LENGTH_SHORT).show()


        }

        verifyOtpButton.setOnClickListener{
            verifyOTPCode(otpEditText.text.toString())
            Toast.makeText(applicationContext, "verify otp button function called", Toast.LENGTH_SHORT).show()

        }

    }


    fun sendVerificationCode(phoneNumber: String){
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks  = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:$credential")
            val code: String? = credential.smsCode
            if(!code.isNullOrBlank()){
                verifyOTPCode(code)
                Toast.makeText(applicationContext, "verify otp code function called", Toast.LENGTH_SHORT).show()

            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }

            Toast.makeText(applicationContext, "verification failed " + e.toString(), Toast.LENGTH_SHORT).show()


            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:$verificationId")
            Toast.makeText(applicationContext, "code sent successfully ", Toast.LENGTH_SHORT).show()


            // Save verification ID and resending token so we can use them later
            storedVerificationId = verificationId

        }
    }

    fun verifyOTPCode(code: String){
        val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(storedVerificationId, code)
        signinbycredentials(credential)
    }

    fun signinbycredentials(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener{task->
                if(task.isSuccessful){
                    Toast.makeText(applicationContext, "authentication successful", Toast.LENGTH_SHORT).show()

                }else{
                    Toast.makeText(applicationContext, "failed failed failed", Toast.LENGTH_SHORT).show()

                }

            }
    }
}
