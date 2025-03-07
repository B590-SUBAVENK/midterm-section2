package com.iub.midterm_section2

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
//import com.google.firebase.storage.FirebaseStorage
import com.iub.midterm_section2.databinding.FragmentProfileBinding
import com.iub.midterm_section2.model.User

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    // Firebase references
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        // Select Image button
        binding.btnSelectImage.setOnClickListener {
            selectImageFromGallery()
        }

        // Upload button
        binding.btnUploadImage.setOnClickListener {
            uploadProfilePicture()
        }

        // Load existing profile picture if available
        loadProfilePicture()

        return binding.root
    }

    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                imageUri = uri
                binding.ivProfile.setImageURI(uri)
            }
        }
    }

    private fun uploadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return

        if (imageUri == null) {
            Toast.makeText(requireContext(), "Please select an image!", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a path in Storage: "profile_pictures/<userID>.jpg"
        val fileRef = storageRef.child("profile_pictures/$userId.jpg")

        fileRef.putFile(imageUri!!)
            .addOnSuccessListener {
                // On success, get the download URL
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    saveProfilePictureUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to upload image", e)
                Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveProfilePictureUrl(downloadUrl: String) {
        val userId = auth.currentUser?.uid ?: return

        // Update "profileImageUrl" in the user's document
        firestoreDb.collection("users").document(userId)
            .update("profileImageUrl", downloadUrl)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to update profileImageUrl", e)
                Toast.makeText(requireContext(), "Failed to update profile: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        // Retrieve the user's doc to show existing profileImageUrl
        firestoreDb.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                val imageUrl = user?.profileImageUrl
                if (!imageUrl.isNullOrEmpty()) {
                    binding.ivProfile.load(imageUrl)
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Failed to load profile image", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
