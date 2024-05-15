package com.appsonair

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.icu.number.IntegerWidth
import android.icu.text.ListFormatter.Width
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.appsonair.base.BaseActivity
import com.appsonair.tools.ToolType
import com.appsonair.tools.ToolType.EMOJI
import com.appsonair.tools.ToolType.ERASER
import com.appsonair.tools.ToolType.GALLERY
import com.appsonair.tools.ToolType.REDO
import com.appsonair.tools.ToolType.SHAPE
import com.appsonair.tools.ToolType.TEXT
import com.appsonair.tools.ToolType.UNDO
import com.appsonair.tools.ToolsAdapter
import com.appsonair.tools.ToolsAdapter.OnItemSelected
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
import java.io.File
import java.io.IOException

class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    BottomSheetShape.Properties, BottomSheetEmoji.EmojiListener, OnItemSelected {

    lateinit var mPhotoEditor: PhotoEditor
    private lateinit var mPhotoEditorView: PhotoEditorView
    private lateinit var mShapeBSFragment: BottomSheetShape
    private lateinit var mShapeBuilder: ShapeBuilder
    private lateinit var mEmojiBSFragment: BottomSheetEmoji
    private lateinit var mRvTools: RecyclerView
    private lateinit var width: Integer
    private lateinit var height: Integer
    private val mEditingToolsAdapter = ToolsAdapter(this)

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

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(true)
            .setClipSourceImage(true)
            .build()

        val layoutParams = mPhotoEditorView.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        layoutParams.height =  ViewGroup.LayoutParams.WRAP_CONTENT
        mPhotoEditorView.layoutParams = layoutParams

        mPhotoEditor.setOnPhotoEditorListener(this)
        mSaveFileHelper = FileSaveHelper(this)
    }

    private fun handleIntentImage(source: ImageView) {
        if (intent == null) {
            return
        } else {
            if (intent != null && intent.hasExtra("IMAGE_PATH")) {
                val imagePath = intent.getParcelableExtra<Uri>("IMAGE_PATH")
                source.setImageURI(imagePath)
            } else {
                //
            }
        }
    }

    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photo_editor_view)
        mRvTools = findViewById(R.id.rv_tools)

        val imgClose: ImageView = findViewById(R.id.img_close)
        imgClose.setOnClickListener(this)

        val tvDone: TextView = findViewById(R.id.tv_done)
        tvDone.setOnClickListener(this)
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
            R.id.img_close -> onBackPressed()
            R.id.tv_done -> saveImage()
        }
    }


    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading(getString(R.string.please_wait))
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
                                mSaveImageUri = uri
                                mPhotoEditorView.source.setImageURI(mSaveImageUri)
                                sendFeedback(uri)
                            } else {
                                hideLoading()
                                showSnackbar(getString(R.string.failed_to_save_image))
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

    private fun sendFeedback(imageUri: Uri?) {
        val intent = Intent(this, FeedbackActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("IMAGE_PATH", imageUri)
        startActivity(intent)
        finish()
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
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
    }

    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
    }

    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor.addEmoji(emojiUnicode)
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
        builder.setPositiveButton(getString(R.string.save)) { _: DialogInterface?, _: Int ->
            saveImage()
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton(getString(R.string.discard)) { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
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
                    }
                })
            }

            ERASER -> {
                mPhotoEditor.setBrushEraserSize(100f)
                mPhotoEditor.brushEraser()
            }

            EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)

            UNDO -> mPhotoEditor.undo()

            REDO -> mPhotoEditor.redo()

            GALLERY -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(
                    Intent.createChooser(
                        intent,
                        getString(R.string.select_picture)
                    ), PICK_REQUEST
                )
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
        private var isOpen = false
        fun isOpen(): Boolean {
            return isOpen
        }
    }
}