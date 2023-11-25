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
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.cmpt362.blissful.databinding.FragmentAddBinding
import com.cmpt362.blissful.db.util.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    // DELETE UNUSED
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addViewModel =
            ViewModelProvider(this)[AddViewModel::class.java]

        _binding = FragmentAddBinding.inflate(inflater, container, false)
        val root: View = binding.root

        tempImgFile = File(context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES), tempImgFileName)

        // URI for the images
        tempImgUri = context?.let {
            FileProvider.getUriForFile(
                it,
                "com.cmpt362.blissful", tempImgFile)
        }!!

        // Buttons
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
            imageView = binding.imageView
            imageView.setImageBitmap(it)
        }
        return root
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
}