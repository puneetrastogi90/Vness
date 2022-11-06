package com.pr.imagemacro

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.burhanrashid52.photoediting.base.BaseActivity
import com.pr.imagemacro.databinding.ActivityMainBinding
import com.pr.imagemacro.impl.MyImageEditorImpl
import com.pr.imagemacro.interfaces.ImageSaveListener
import com.pr.imagemacro.interfaces.MyImageEditor
import com.pr.imagemacro.tools.TextEditorDialogFragment
import com.pr.imagemacro.viewmodels.MainViewmodel
import ja.burhanrashid52.photoeditor.PhotoEditorView

class MainActivity : BaseActivity(), View.OnClickListener {

    private val mainViewmodel by viewModels<MainViewmodel>()

    private val myEditor: MyImageEditor<PhotoEditorView> = MyImageEditorImpl()

    @VisibleForTesting
    var mSaveImageUri: Uri? = null
    private var fileUtils: FileUtils? = null

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PICK_REQUEST = 53
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        initViews()
    }

    private fun initViews() {
        myEditor.setEditorView(binding.photoEditorView)
        myEditor.changeBackgroundColor(mainViewmodel.getRandomColor())
        binding.changeColorButton.setOnClickListener(this)
        binding.addTextButton.setOnClickListener(this)
        binding.shareButton.setOnClickListener(this)
        binding.downloadButton.setOnClickListener(this)
        binding.addImageButton.setOnClickListener(this)
        fileUtils = FileUtils(this)
    }

    @SuppressLint("MissingPermission")
    override fun onClick(view: View?) {
        when (view) {
            binding.changeColorButton -> myEditor.changeBackgroundColor(mainViewmodel.getRandomColor())
            binding.addTextButton -> addText()
            binding.downloadButton -> saveImage()
            binding.shareButton -> shareImage()
            binding.addImageButton -> addNewImage()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.undo -> myEditor.undoAction()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun addNewImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_REQUEST -> myEditor.addImage(data?.data, contentResolver)
            }
        }
    }


    private fun addText() {
        val textEditorDialogFragment = TextEditorDialogFragment.show(this)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditorListener {
            override fun onDone(inputText: String?, colorCode: Int) {
                myEditor.addText(inputText, colorCode)
            }
        })
    }

    private fun shareImage() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(R.string.save_image_to_share))
            return
        }
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(this, saveImageUri))
        startActivity(Intent.createChooser(intent, getString(R.string.share_image)))
    }


    private fun saveImage() {
        val fileName =
            System.currentTimeMillis().toString().plus(".") + binding.formatSpinner.selectedItem.toString()
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || isSdkHigherThan28()) {
            showLoading("Saving...")
            fileUtils?.createFile(fileName, object : FileUtils.OnFileCreated {
                override fun onFileCreated(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    if (created && filePath != null) {
                        myEditor.saveFile(
                            filePath,
                            binding.formatSpinner.selectedItem.toString(),
                            object : ImageSaveListener {
                                override fun onFileSaveSuccess(imagePath: String) {
                                    fileUtils?.notifyThatFileIsNowPubliclyAvailable(
                                        contentResolver
                                    )
                                    hideLoading()
                                    showSnackbar("Image Saved Successfully")
                                    mSaveImageUri = uri
                                }

                                override fun onFileSaveFailed(exception: Exception) {
                                    hideLoading()
                                    showSnackbar("Failed to save Image")
                                }

                            })
                    } else {
                        hideLoading()
                        error?.let { showSnackbar(error) }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }
}