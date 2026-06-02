package com.example.bilibili.ui.releaseVideo.bottomSheet

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.core.widget.doOnTextChanged
import com.example.bilibili.databinding.LayoutBottomSheetIntroductionBinding
import com.example.bilibili.ui.releaseVideo.ReleaseVideoViewModel
import com.example.bilibili.util.TextSelectHandleHelper
import com.example.bilibili.util.UserInfoText
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class IntroductionBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomSheetIntroductionBinding? = null
    private val binding get() = _binding!!

    // 直接访问 ViewModel
    private val viewModel: ReleaseVideoViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutBottomSheetIntroductionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TextSelectHandleHelper.applyPinkHandlesIn(view)
        setupIntroductionInput()
        setupButtons()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE,
        )
        binding.etIntroductionInput.postDelayed({ focusAndShowKeyboard() }, 150)
    }

    private fun setupIntroductionInput() {
        // 从 ViewModel 获取当前的简介
        val currentIntroduction = UserInfoText.normalize(viewModel.introduction.value)

        // 设置初始文本
        binding.etIntroductionInput.setText(currentIntroduction)
        binding.tvWordCount.text = "${currentIntroduction.length}/2000"

        // 监听文本变化
        binding.etIntroductionInput.doOnTextChanged { text, _, _, _ ->
            val newText = text?.toString() ?: ""
            binding.tvWordCount.text = "${newText.length}/2000"
        }
    }

    private fun setupButtons() {
        // 取消按钮
        binding.tvCancel.setOnClickListener {
            dismiss()
        }

        // 确认按钮
        binding.tvConfirm.setOnClickListener {
            viewModel.setIntroduction(binding.etIntroductionInput.text.toString())
            hideKeyboard()
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        hideKeyboard()
        activity?.currentFocus?.clearFocus()
        super.onDismiss(dialog)
    }

    private fun focusAndShowKeyboard() {
        if (_binding == null) return
        binding.etIntroductionInput.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.etIntroductionInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etIntroductionInput.windowToken, 0)
        activity?.window?.decorView?.let { decor ->
            imm.hideSoftInputFromWindow(decor.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}