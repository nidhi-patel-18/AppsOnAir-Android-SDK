package com.appsonair

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.base.BaseActivity
import com.appsonair.tools.ToolsAdapter.OnItemSelected
import com.appsonair.tools.ToolType
import com.appsonair.tools.ToolType.*
import com.appsonair.tools.ToolsAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import kotlinx.coroutines.launch
import java.io.IOException

class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    BottomSheetShape.Properties, BottomSheetEmoji.EmojiListener, OnItemSelected {

    lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var mShapeBSFragment: BottomSheetShape
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mEmojiBSFragment: BottomSheetEmoji
    private lateinit var mTxtCurrentTool: TextView
    private lateinit var mRvTools: RecyclerView
    private val mEditingToolsAdapter = ToolsAdapter(this)
    private lateinit var mRootView: ConstraintLayout

    @VisibleForTesting
    var mSaveImageUri: Uri? = null

    private lateinit var mSaveFileHelper: FileSaveHelper

    override fun onDestroy() {
        super.onDestroy()
        isOpen = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isOpen()) {
            // The activity is already open, so finish this instance
            finish()
            return
        }

        isOpen = true
        setContentView(R.layout.activity_edit_image)
        initViews()
        handleIntentImage(mPhotoEditorView.source)

        mEmojiBSFragment = BottomSheetEmoji()
        mShapeBSFragment = BottomSheetShape()
        mEmojiBSFragment.setEmojiListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools.layoutManager = llmTools
        mRvTools.adapter = mEditingToolsAdapter

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
            .build() // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this)

        mSaveFileHelper = FileSaveHelper(this)
    }

    private fun handleIntentImage(source: ImageView) {
        if (intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_EDIT, ACTION_NEXTGEN_EDIT -> {
                try {
                    val uri = intent.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            else -> {
                val intentType = intent.type
                if (intentType != null && intentType.startsWith("image/")) {
                    val imageUri = intent.data
                    if (imageUri != null) {
                        source.setImageURI(imageUri)
                    }
                }
            }
        }
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRootView = findViewById(R.id.rootView)

        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)

        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)
    }

    override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
        rootView?.let {
            val textEditorDialogFragment = TextEditorDialog.show(this, text.toString(), colorCode)
            textEditorDialogFragment.setOnTextEditorListener(object :
                TextEditorDialog.TextEditorListener {
                override fun onDone(inputText: String, colorCode: Int) {
                    val styleBuilder = TextStyleBuilder()
                    styleBuilder.withTextColor(colorCode)
                    mPhotoEditor.editText(it, inputText, styleBuilder)
                    mTxtCurrentTool.setText(R.string.label_text)
                }
            })
        }
    }


    override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    override fun onTouchSourceImage(event: MotionEvent?) {
        Log.d(TAG, "onTouchView() called with: event = [$event]")
    }


    @SuppressLint("MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean, filePath: String?, error: String?, uri: Uri?
                ) {
                    lifecycleScope.launch {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder().setClearViewsEnabled(true)
                                .setTransparencyEnabled(true).build()

                            val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                            if (result is SaveFileResult.Success) {
                                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                hideLoading()
                                showSnackbar("Image Saved Successfully")
                                mSaveImageUri = uri
                                mPhotoEditorView.source.setImageURI(mSaveImageUri)
                            } else {
                                hideLoading()
                                showSnackbar("Failed to save Image")
                            }
                        } else {
                            hideLoading()
                            error?.let { showSnackbar(error) }
                        }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                PICK_REQUEST -> try {
                    mPhotoEditor.clearAllViews()
                    val uri = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver, uri
                    )
                    mPhotoEditorView.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor.addEmoji(emojiUnicode)
        mTxtCurrentTool.setText(R.string.label_emoji)
    }

    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }

    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
                mTxtCurrentTool.setText(R.string.label_shape)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }

            TEXT -> {
                val textEditorDialogFragment = TextEditorDialog.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialog.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                        mTxtCurrentTool.setText(R.string.label_text)
                    }
                })
            }

            ERASER -> {
                mPhotoEditor.brushEraser()
                mTxtCurrentTool.setText(R.string.label_eraser_mode)
            }

            EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)

            UNDO -> mPhotoEditor.undo()

            REDO -> mPhotoEditor.redo()

            GALLERY -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }

    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    override fun onBackPressed() {
        if (!mPhotoEditor.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {
        private const val TAG = "EditImageActivity"
        private const val PICK_REQUEST = 53
        const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
        private var isOpen = false
        fun isOpen(): Boolean {
            return isOpen
        }
    }
}