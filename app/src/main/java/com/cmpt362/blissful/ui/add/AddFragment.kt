package com.cmpt362.blissful.ui.add

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
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
import com.cmpt362.blissful.R
import com.cmpt362.blissful.databinding.FragmentAddBinding
import com.cmpt362.blissful.db.LocalRoomDatabase
import com.cmpt362.blissful.db.post.Post
import com.cmpt362.blissful.db.post.PostDatabaseDao
import com.cmpt362.blissful.db.post.PostRepository
import com.cmpt362.blissful.db.post.PostViewModel
import com.cmpt362.blissful.db.post.PostViewModelFactory
import com.cmpt362.blissful.db.util.getBitmap
import com.cmpt362.blissful.db.util.getUserId
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
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

    private lateinit var database: LocalRoomDatabase
    private lateinit var databaseDao: PostDatabaseDao
    private lateinit var repository: PostRepository
    private lateinit var viewModelFactory: PostViewModelFactory
    private lateinit var postViewModel: PostViewModel

    // Firebase
    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private val storageRef = Firebase.storage.reference


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Firebase auth
        auth = Firebase.auth


        addViewModel =
            ViewModelProvider(this)[AddViewModel::class.java]

        _binding = FragmentAddBinding.inflate(inflater, container, false)

        // Room Database
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]
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
                it,
                "com.cmpt362.blissful", tempImgFile
            )
        }!!

        // Image save Button
        imageSaveButton = binding.photoSubmitButton
        imageSaveButton.setOnClickListener {
            showAlertDialog()
        }

        // Receiving result from Camera and Gallery Intents
        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val bitmap = getBitmap(requireContext(), tempImgUri)
                addViewModel.newImage.value = bitmap
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                tempImgUri = result.data?.data!!
                val bitmap = getBitmap(requireContext(), tempImgUri)
                addViewModel.newImage.value = bitmap

                try {
                    val file = File(
                        context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        tempImgFileName
                    )
                    CoroutineScope(Dispatchers.IO).launch { openFile(file, bitmap) }

                } catch (e: Exception) {
                    Log.e(ContentValues.TAG, "File not saved: ", e)
                }


            }
        }

        // Saving the image in a View model
        addViewModel.newImage.observe(
            viewLifecycleOwner,
        ) {
            if (it != null) {
                // User selected Image
                imageView = binding.imageView
                imageView.setImageBitmap(it)
            } else {
                // Resetting back to placeholder image
                imageView = binding.imageView
                imageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.photo_icon
                    )
                )
                imageView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        requireContext(),
                        R.drawable.photo_icon
                    )
                )
            }
        }

        postTextView = binding.postInput
        submitButton = binding.submitButton

        // Submitting user entry
        submitButton.setOnClickListener {
            submitPost()
            addViewModel.newImage.value = null
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // Dialog for image selection
    private fun showAlertDialog() {
        val pictureDialogItems = arrayOf("Select photo from Gallery", "Capture photo from Camera")
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Enter your gratitude")
                .setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> gallery()
                        1 -> camera()
                    }
                }
                .show()
        }
    }

    private suspend fun openFile(file: File, bitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            val fileOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOut)
            fileOut.flush()
            fileOut.close()
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
        if (userId == -1) {
            Toast.makeText(
                requireContext(),
                "Please sign in to submit a post",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val postText = postTextView.text.toString().trim()
            val isPublic = addViewModel.isPublic.value ?: false
            if (postText.isNotEmpty()) {

                val defaultPost =
                    Post(
                        userId = userId,
                        content = postText,
                        location = "",
                    )

                val post = if (addViewModel.newImage.value != null) {
                    try {
                        val image = addViewModel.newImage.value
                        Post(
                            userId = userId,
                            content = postText,
                            location = null,
                            image = image,
                        )
                    } catch (e: Exception) {
                        Log.e(ContentValues.TAG, "File not saved: ", e)
                        defaultPost
                    }
                } else {
                    defaultPost
                }


                postViewModel.insert(post)
                postTextView.text.clear()
                Toast.makeText(requireContext(), "Post submitted", Toast.LENGTH_SHORT).show()

                // Adding data to firebase storage
                val currentUser = auth.currentUser
                if (isPublic) {
                    // Save data in firebase storage
                    // extract the file name with extension
                    // Image name format: Random_generated_string.jpg

                    val sd =
                        "${UUID.randomUUID()}.jpg"

                    // Upload Task with upload to directory 'file'
                    // and name of the file remains same
                    val uploadTask = storageRef.child("file/$sd").putFile(tempImgUri)
                    // On success, download the file URL and display it
                    uploadTask.addOnSuccessListener {
                        Log.e("Firebase", "Image Upload passed")
                    }.addOnFailureListener {
                        Log.e("Firebase", "Image Upload fail")
                    }

                    // Save data in firestore
                    val nestedData: MutableMap<String, Any> = HashMap()
                    if (currentUser != null) {
                        nestedData["entryName"] = currentUser.displayName.toString()
                    } else {
                        // TODO : change to user name
                        nestedData["entryName"] = userId.toString()
                    }
                    nestedData["data"] = sd //postText +
                    db.collection("entries").document("test")
                        .set(nestedData)
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a post text", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
