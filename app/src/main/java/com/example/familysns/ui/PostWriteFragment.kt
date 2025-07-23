package com.example.familysns.ui

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.example.familysns.databinding.FragmentPostWriteBinding

class PostWriteFragment : Fragment() {
    private lateinit var binding: FragmentPostWriteBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostWriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = arguments?.getString("imageUri")
        binding.imageView.setImageURI(Uri.parse(imageUri))
    }
}