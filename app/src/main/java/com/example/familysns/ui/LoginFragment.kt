package com.example.familysns.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.familysns.MainActivity
import com.example.familysns.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val TAG = "LoginFragment"

    // 🔁 새로운 방식: registerForActivityResult()
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.result
                firebaseAuthWithGoogle(account)
            } catch (e: Exception) {
                Log.w(TAG, "575881230993-m47e8qt8utbgr49liccvmjbpfk33gpkb.apps.googleusercontent.com", e)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("575881230993-m47e8qt8utbgr49liccvmjbpfk33gpkb.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // 버튼 클릭 시 로그인 실행
        binding.btnGoogleLogin.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")

                    val user = auth.currentUser
                    if (user != null) {
                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val userRef = db.collection("users").document(user.uid)

                        userRef.get().addOnSuccessListener { document ->
                            if (!document.exists()) {
                                val userDoc = hashMapOf(
                                    "uid" to user.uid,
                                    "name" to user.displayName,
                                    "email" to user.email,
                                    "photoUrl" to user.photoUrl?.toString(),
                                    "familyId" to emptyList<String>()
                                )

                                userRef.set(userDoc)
                                    .addOnSuccessListener {
                                        Log.d(TAG, "사용자 정보 저장 완료")
                                        navigateToMain(emptyList<String>())  // 새 유저는 familyId 없음
                                    }
                                    .addOnFailureListener {
                                        Log.w(TAG, "사용자 정보 저장 실패", it)
                                    }
                            } else {
                                val familyId = document.get("familyId") as? List<*>
                                navigateToMain(familyId)
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun navigateToMain(familyIdList: List<*>?) {
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            putExtra("hasFamily", !familyIdList.isNullOrEmpty())
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}