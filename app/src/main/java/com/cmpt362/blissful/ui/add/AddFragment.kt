package com.cmpt362.blissful.ui.add


import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
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
import com.cmpt362.blissful.db.util.Util
import com.cmpt362.blissful.db.util.getUserId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        addViewModel =
            ViewModelProvider(this)[AddViewModel::class.java]

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        database = LocalRoomDatabase.getInstance(requireContext())
        databaseDao = database.postDatabaseDao
        repository = PostRepository(databaseDao)
        viewModelFactory = PostViewModelFactory(repository)
        postViewModel = ViewModelProvider(this, viewModelFactory)[PostViewModel::class.java]

        // temp file to store user selected image
        tempImgFile = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempImgFileName)
        // Placeholder image to bitmap
        val placeHolderImage = BitmapFactory.decodeResource(
            requireContext().resources,
            R.drawable.photo_icon
        )
        // URI for the images
        tempImgUri = context?.let {
            FileProvider.getUriForFile(
                it,
                "com.cmpt362.blissful", tempImgFile)
        }!!

        // Image save Button
        imageSaveButton = binding.photoSubmitButton
        imageSaveButton.setOnClickListener {
            showAlertDialog()
        }

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = Util.getBitmap(requireContext(), tempImgUri)
                addViewModel.newImage.value = bitmap
            }
        }

        galleryResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                tempImgUri = result.data?.data!!
                val bitmap = Util.getBitmap(requireContext(), tempImgUri)
                addViewModel.newImage.value = bitmap

                try {
                    val file = File(
                        context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                        tempImgFileName
                    )
                    CoroutineScope(Dispatchers.IO).launch { openFile(file, bitmap) }

                }
                catch (e: Exception){
                    Log.e(ContentValues.TAG, "File not saved: ", e)
                }


            }
        }

        // Saving the image in a View model
        addViewModel.newImage.observe(
            viewLifecycleOwner,
        ) {
            if(it != placeHolderImage) {
                // User selected Image
                imageView = binding.imageView
                imageView.setImageBitmap(it)
            }
            else{
                // Resetting back to placeholder image
                imageView = binding.imageView
                imageView.setImageDrawable(resources.getDrawable(R.drawable.photo_icon))
            }
        }

        postTextView = binding.postInput
        submitButton = binding.submitButton
        submitButton.setOnClickListener {
            submitPost()
            // Resetting back to placeholder image
            addViewModel.newImage.value = placeHolderImage
        }

        return binding.root

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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

    fun camera(){
        // Generating the intent and clicking the image
        intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImgUri)
        cameraResult.launch(intent)
    }

    fun gallery(){
        // Opening The gallery
        intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        galleryResult.launch(intent)

    }
    private suspend fun openFile(file: File, bitmap: Bitmap) =
        withContext(Dispatchers.IO) {
            val fileOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fileOut)
            fileOut.flush()
            fileOut.close()
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
            val postText = postTextView.text.toString()
            if (postText.isNotEmpty()) {
                val post = Post(
                    userId = userId,
                    content = postText,
                    location = "",
                )
                postViewModel.insert(post)
                postTextView.text.clear()
                Toast.makeText(requireContext(), "Post submitted", Toast.LENGTH_SHORT).show()
            }
        }
    }

}