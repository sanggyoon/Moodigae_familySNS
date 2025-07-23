package com.example.familysns.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.familysns.databinding.FragmentPostCreateBinding

class PostCreateFragment : Fragment() {
    private lateinit var binding: FragmentPostCreateBinding
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                binding.imagePreview.setImageURI(selectedImageUri)
                binding.imagePreview.visibility = View.VISIBLE
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostCreateBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        binding.btnNext.setOnClickListener {
            selectedImageUri?.let { uri ->
                val action = PostCreateFragmentDirections
                    .actionPostCreateFragmentToPostWriteFragment(uri.toString())
                findNavController().navigate(action)
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}