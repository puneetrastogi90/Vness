package com.pr.imagemacro.tools

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlin.jvm.JvmOverloads
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.pr.imagemacro.ColorPickerAdapter
import com.pr.imagemacro.R
import com.pr.imagemacro.databinding.AddTextDialogBinding


class TextEditorDialogFragment : DialogFragment() {

    private lateinit var binding: AddTextDialogBinding
    private var _inputMethodManager: InputMethodManager? = null
    private var _colorCode = 0
    private var _textEditorListener: TextEditorListener? = null

    interface TextEditorListener {
        fun onDone(inputText: String?, colorCode: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = AddTextDialogBinding.inflate(LayoutInflater.from(context))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _inputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager


        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        binding.addTextColorPickerRecyclerView.layoutManager = layoutManager
        binding.addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(requireActivity())


        colorPickerAdapter.setOnColorPickerClickListener(object :
            ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                _colorCode = ContextCompat.getColor(requireContext(), colorCode)
                binding.addTextEditText.setTextColor(_colorCode)
            }
        })
        binding.addTextColorPickerRecyclerView.adapter = colorPickerAdapter
        binding.addTextEditText.setText(arguments?.getString(EXTRA_INPUT_TEXT))
        _colorCode = requireArguments().getInt(EXTRA_COLOR_CODE)
        binding.addTextEditText.setTextColor(_colorCode)
        _inputMethodManager!!.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.addTextEditText.requestFocus()

        //Make a callback on activity when user is done with text editing
        binding.addTextDoneTv.setOnClickListener { onClickListenerView ->
            _inputMethodManager!!.hideSoftInputFromWindow(onClickListenerView.windowToken, 0)
            dismiss()
            val inputText = binding.addTextEditText.text.toString()
            if (!TextUtils.isEmpty(inputText) && _textEditorListener != null) {
                _textEditorListener!!.onDone(inputText, _colorCode)
            }
        }
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        _textEditorListener = textEditorListener
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        //Show dialog with provide text and text color
        //Show dialog with default text input as empty and text color white
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}