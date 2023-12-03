package com.cmpt362.blissful.ui.add

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentAddBinding
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getUserId
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID


class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private lateinit var tempImgUri: Uri
    private lateinit var addViewModel: AddViewModel
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var galleryResult: ActivityResultLauncher<Intent>
    private lateinit var imageSaveButton: Button
    private lateinit var intent: Intent
    private lateinit var tempImgFile: File
    private lateinit var imageView: ImageView
    private lateinit var publicToggleSwitch: SwitchCompat

    private val tempImgFileName = "temp_image.jpg"

    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!

    private lateinit var submitButton: Button
    private lateinit var postTextView: EditText

    private lateinit var auth: FirebaseAuth
    private val storageRef = Firebase.storage.reference

    private lateinit var postViewModel: PostViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        addViewModel = ViewModelProvider(this)[AddViewModel::class.java]

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        initializeDatabase()
        publicToggleSwitch = binding.publicToggleSwitch
        publicToggleSwitch.setOnCheckedChangeListener { _, isChecked ->
            addViewModel.isPublic.value = isChecked
        }

        // temp file to store user selected image
        tempImgFile =
            File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempImgFileName)
        // URI for the images
        tempImgUri = context?.let {
            FileProvider.getUriForFile(
                it, "com.cmpt362.blissful", tempImgFile
            )
        }!!

        // Image save Button
        imageSaveButton = binding.photoSubmitButton
        imageSaveButton.setOnClickListener {
            showAlertDialog()
        }

        cameraResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    addViewModel.newImage.value = tempImgUri.toString()
                }
            }

        galleryResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if (result.resultCode == Activity.RESULT_OK) {
                    tempImgUri = result.data?.data!!
                    addViewModel.newImage.value = tempImgUri.toString()

                    try {
                        val file = File(
                            context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            tempImgFileName
                        )
                        CoroutineScope(Dispatchers.IO).launch { openFile(file, tempImgUri) }

                    } catch (e: Exception) {
                        Log.e(TAG, "File not saved: ", e)
                    }
                }
            }


        // Saving the image in a View model
        addViewModel.newImage.observe(
            viewLifecycleOwner,
        ) { uri ->
            imageView = binding.imageView
            if (uri != null) {
                // User selected Image
                Glide.with(this).load(uri)
                    .into(imageView) // Use Glide or another library to load the image
            } else {
                // Resetting back to placeholder image
                imageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(), R.drawable.photo_icon
                    )
                )
            }
        }


        postTextView = binding.postInput
        submitButton = binding.submitButton
        submitButton.setOnClickListener {
            submitPost()
            addViewModel.newImage.value = null
        }

        return binding.root
    }

    private fun initializeDatabase() {
        val postRepository = PostRepository(FirebaseFirestore.getInstance())
        val viewModelFactory = PostViewModelFactory(postRepository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAlertDialog() {
        val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
        activity?.let {
            AlertDialog.Builder(it).setTitle("Enter your gratitude")
                .setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> gallery()
                        1 -> camera()
                    }
                }.show()
        }
    }

    private suspend fun openFile(destinationFile: File, sourceUri: Uri) =
        withContext(Dispatchers.IO) {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(sourceUri)
                val outputStream = FileOutputStream(destinationFile)
                inputStream?.use { input ->
                    outputStream.use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error copying file: ", e)
            }
        }

    private fun camera() {
        // Generating the intent and clicking the image
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    private fun gallery() {
        // Opening The gallery
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryResult.launch(intent)

    }

    private fun submitPost() {
        val userId = getUserId(requireContext())
        if (userId == "") {
            Toast.makeText(
                requireContext(), "Please sign in to submit a post", Toast.LENGTH_SHORT
            ).show()
        } else {
            val postText = postTextView.text.toString().trim()

            if (postText.isNotEmpty()) {
                val isPublic = addViewModel.isPublic.value ?: false

                val defaultPost = Post(
                    userId = userId, content = postText, isPublic = isPublic
                )

                val post = if (addViewModel.newImage.value != null) {
                    try {
                        // Save Image in firebase storage
                        // Image name format: Random_generated_string.jpg
                        val imageUrl =
                            "${UUID.randomUUID()}.jpg"

                        val uploadTask = storageRef.child("file/$imageUrl").putFile(tempImgUri)
                        // On success
                        uploadTask.addOnSuccessListener {
                            Log.e("Firebase", "Image Upload passed")
                        } // On success
                            .addOnFailureListener {
                                Log.e("Firebase", "Image Upload fail")
                            }
                        Post(
                            userId = userId,
                            content = postText,
                            isPublic = isPublic,
                            imageUrl = imageUrl,
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Error creating post with image URL: ", e)
                        defaultPost
                    }
                } else {
                    defaultPost
                }

                // Save post to Firebase Firestore
                postViewModel.insert(post);

                postTextView.text.clear()
                Toast.makeText(requireContext(), "Post submitted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a post text", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
