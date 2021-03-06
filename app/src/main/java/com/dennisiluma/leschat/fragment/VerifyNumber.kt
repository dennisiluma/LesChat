package com.dennisiluma.leschat.fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.dennisiluma.leschat.R
import com.dennisiluma.leschat.model.UserModel
import com.dennisiluma.leschat.databinding.FragmentVerifyNumberBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class VerifyNumber : Fragment() {

    private var code: String? = null
    private lateinit var pin: String
    private var firebaseAuth: FirebaseAuth? = null
    private var databaseReference: DatabaseReference? = null

    private var _binding: FragmentVerifyNumberBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*Arguments coming from GetUserNumber*/
        arguments?.let {
            code = it.getString("code")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentVerifyNumberBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        binding.btnVerify.setOnClickListener {
            if (checkPin()) {
                //if(code == pin)){ // check weather to remove or not
                    val credential = PhoneAuthProvider.getCredential(code!!, pin)
                    signInUser(credential)
                //}
                //else Toast.makeText(context, "Your pin Doesn't match, Type in the correct values" , Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkPin(): Boolean {
        pin = binding.otpTextView.text.toString().trim()
        if (pin.isEmpty()) {
            binding.otpTextView.error = "Filed is required"
            return false
        } else if (pin.length < 6) {
            binding.otpTextView.error = "Enter valid pin"
            return false
        } else return true
    }

    private fun signInUser(credential: PhoneAuthCredential) {
        firebaseAuth!!.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                val userModel =
                    UserModel(
                        "", "", "",
                        firebaseAuth!!.currentUser!!.phoneNumber!!,
                        firebaseAuth!!.uid!!
                    )
                Toast.makeText(context, "Succesfuly registered", Toast.LENGTH_SHORT).show()

                databaseReference!!.child(firebaseAuth?.uid!!).setValue(userModel)
                saveInSharePreferencesForIncompleteRegistration()
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.main_container, GetUserData())
                    .commit()
            }
        }
    }

    fun saveInSharePreferencesForIncompleteRegistration() {
        val sharePreferences =
            requireActivity().getSharedPreferences(
                "registeredButNotFullyComplete",
                Context.MODE_PRIVATE
            )
        val editor = sharePreferences.edit()
        editor.putString("registeredButNotFullyComplete", "registeredButNotFullyComplete")
        editor.apply()
    }


    companion object {

        @JvmStatic
        fun newInstance(code: String) =
            VerifyNumber().apply {
                arguments = Bundle().apply {
                    putString("code", code)
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}