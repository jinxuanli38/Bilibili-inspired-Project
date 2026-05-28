package com.example.bilibili.ui.releaseVideo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.bilibili.R
import com.example.bilibili.data.model.ReleaseVideoPart
import com.example.bilibili.databinding.ItemReleaseVideoPartBinding
import com.example.bilibili.util.UiUtils.dp
import kotlin.math.max

class ReleaseVideoPartAdapter(
    private val onPartSelected: (ReleaseVideoPart) -> Unit,
    private val onMoreClick: (ReleaseVideoPart, View) -> Unit,
) : RecyclerView.Adapter<ReleaseVideoPartAdapter.PartViewHolder>() {

    private val items = mutableListOf<ReleaseVideoPart>()
    private var selectedPartId: String? = null
    private var recyclerView: RecyclerView? = null
    private var draggingPartId: String? = null
    private var isDragging = false
    /** 拖拽开始时冻结的 P 序号，松手前不随位置变化 */
    private var frozenPartLabels: Map<String, Int>? = null

    override fun getItemCount(): Int = items.size

    fun isDragging(): Boolean = isDragging

    fun enterDragMode(draggedPartId: String) {
        if (isDragging && this.draggingPartId == draggedPartId) return
        isDragging = true
        draggingPartId = draggedPartId
        frozenPartLabels = items.mapIndexed { index, part -> part.id to (index + 1) }.toMap()
        notifyItemRangeChanged(0, itemCount, PAYLOAD_DRAG_STATE)
    }

    fun exitDragMode() {
        if (!isDragging) return
        isDragging = false
        draggingPartId = null
        frozenPartLabels = null
        notifyItemRangeChanged(0, itemCount, PAYLOAD_DRAG_STATE)
    }

    fun frozenLabelFor(partId: String): Int? = frozenPartLabels?.get(partId)

    fun shouldShowPartProgress(part: ReleaseVideoPart): Boolean {
        if (part.uploadStatus == "上传完成") return false
        if (part.uploadStatus.contains("失败")) return false
        if (part.uploadProgress >= 100) return false
        return part.uploadId.isNotEmpty() && part.uploadStatus.startsWith("正在")
    }

    /** @return null 表示应隐藏进度条 */
    fun resolvePartDisplayProgress(part: ReleaseVideoPart): Int? {
        if (!shouldShowPartProgress(part)) return null
        return part.uploadProgress.coerceIn(0, 99)
    }

    fun setSelectedPartId(partId: String?) {
        val oldId = selectedPartId
        if (oldId == partId) return
        selectedPartId = partId
        items.forEachIndexed { index, part ->
            if (part.id == oldId || part.id == partId) {
                notifyItemChanged(index, PAYLOAD_SELECTION)
            }
        }
    }

    fun clearTitleFocus() {
        recyclerView?.findFocus()?.clearFocus()
        recyclerView?.context?.let { hideKeyboard(it, recyclerView) }
    }

    fun getPartAt(position: Int): ReleaseVideoPart? = items.getOrNull(position)

    fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition !in items.indices || toPosition !in items.indices || fromPosition == toPosition) {
            return
        }
        val item = items.removeAt(fromPosition)
        items.add(toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun setParts(parts: List<ReleaseVideoPart>) {
        if (isDragging) {
            syncWhileDragging(parts)
            return
        }
        val diff = DiffUtil.calculateDiff(PartDiffCallback(items, parts))
        items.clear()
        items.addAll(parts)
        diff.dispatchUpdatesTo(this)
    }

    private fun syncWhileDragging(parts: List<ReleaseVideoPart>) {
        parts.forEach { newPart ->
            val index = items.indexOfFirst { it.id == newPart.id }
            if (index < 0) return@forEach
            val old = items[index]
            if (old.uploadProgress == newPart.uploadProgress &&
                old.uploadStatus == newPart.uploadStatus &&
                old.displayTitle == newPart.displayTitle
            ) {
                return@forEach
            }
            val merged = newPart.copy(
                uploadProgress = max(old.uploadProgress, newPart.uploadProgress),
            )
            items[index] = merged
            notifyItemChanged(index, PAYLOAD_PROGRESS)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        if (this.recyclerView === recyclerView) {
            this.recyclerView = null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartViewHolder {
        val binding = ItemReleaseVideoPartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return PartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PartViewHolder, position: Int) {
        holder.bind(items[position], selected = items[position].id == selectedPartId)
    }

    override fun onBindViewHolder(
        holder: PartViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        val part = items.getOrNull(position) ?: return
        when {
            payloads.contains(PAYLOAD_DRAG_STATE) -> {
                holder.applyDragState(
                    isDragging = isDragging,
                    isDraggedItem = part.id == draggingPartId,
                    selected = part.id == selectedPartId,
                    part = part,
                    position = position,
                )
            }
            payloads.contains(PAYLOAD_SELECTION) -> {
                holder.applySelection(part.id == selectedPartId)
            }
            payloads.contains(PAYLOAD_PROGRESS) -> {
                holder.bindPartUploadProgress(part)
            }
            else -> super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class PartViewHolder(
        private val binding: ItemReleaseVideoPartBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        private var isDragPreview = false
        private var savedWidth = 0
        private var savedHeight = 0
        private var savedMarginEnd = 0

        init {
            binding.root.isLongClickable = true
            binding.tvPartLabel.isLongClickable = true
            binding.etPartTitle.isFocusable = false
            binding.etPartTitle.isFocusableInTouchMode = false
            binding.etPartTitle.isClickable = false
            binding.etPartTitle.isLongClickable = false
            binding.root.setOnClickListener {
                currentPart()?.let { onPartSelected(it) }
            }
            binding.btnMore.setOnClickListener { view ->
                currentPart()?.let { part -> onMoreClick(part, view) }
            }
        }

        fun bind(part: ReleaseVideoPart, selected: Boolean) {
            val pos = bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION } ?: 0
            updatePartLabel(part, pos)
            bindPartStatusText(part)
            bindPartUploadProgress(part)
            applyDragState(
                isDragging = isDragging,
                isDraggedItem = part.id == draggingPartId,
                selected = selected,
                part = part,
                position = pos,
            )
        }

        fun applySelection(selected: Boolean) {
            if (isDragPreview) return
            binding.root.setBackgroundResource(
                if (selected) R.drawable.bg_release_part_card_selected
                else R.drawable.bg_release_part_card,
            )
            binding.etPartTitle.clearFocus()
        }

        fun applyDragState(
            isDragging: Boolean,
            isDraggedItem: Boolean,
            selected: Boolean,
            part: ReleaseVideoPart,
            position: Int,
        ) {
            updatePartLabel(part, position)
            if (!isDragging) {
                if (isDragPreview) {
                    exitDragPreview()
                } else {
                    binding.etPartTitle.visibility = View.VISIBLE
                    binding.btnMore.visibility = View.VISIBLE
                }
                applySelection(selected)
                return
            }
            if (!isDragPreview) {
                enterDragPreview(isDraggedItem)
            } else {
                applyDragPreviewStyle(isDraggedItem)
            }
        }

        fun bindPartUploadProgress(part: ReleaseVideoPart) {
            val bar = binding.progressPartUpload
            val displayProgress = resolvePartDisplayProgress(part)
            if (displayProgress == null || isDragPreview) {
                bar.visibility = View.GONE
                bar.progress = 0
                return
            }
            bar.visibility = View.VISIBLE
            bar.progress = displayProgress
        }

        private fun enterDragPreview(isActive: Boolean) {
            if (isDragPreview) {
                applyDragPreviewStyle(isActive)
                return
            }
            isDragPreview = true
            binding.etPartTitle.clearFocus()
            hideKeyboard(binding.root.context, binding.etPartTitle)
            binding.etPartTitle.visibility = View.GONE
            binding.btnMore.visibility = View.GONE
            binding.progressPartUpload.visibility = View.GONE
            val lp = binding.root.layoutParams as RecyclerView.LayoutParams
            savedWidth = lp.width
            savedHeight = lp.height
            savedMarginEnd = lp.marginEnd
            lp.width = DRAG_CARD_WIDTH_DP.dp
            lp.height = DRAG_CARD_HEIGHT_DP.dp
            lp.marginEnd = 6.dp
            binding.root.layoutParams = lp
            applyDragPreviewStyle(isActive)
        }

        private fun applyDragPreviewStyle(isActive: Boolean) {
            binding.root.setBackgroundResource(
                if (isActive) R.drawable.bg_release_part_card_drag_active
                else R.drawable.bg_release_part_card_drag_idle,
            )
            val ctx = binding.root.context
            binding.tvPartLabel.setTextColor(
                if (isActive) {
                    ContextCompat.getColor(ctx, R.color.bili_pink)
                } else {
                    LABEL_COLOR_IDLE
                },
            )
        }

        private fun exitDragPreview() {
            if (!isDragPreview) return
            isDragPreview = false
            binding.etPartTitle.visibility = View.VISIBLE
            binding.btnMore.visibility = View.VISIBLE
            currentPart()?.let { bindPartUploadProgress(it) }
            val lp = binding.root.layoutParams as RecyclerView.LayoutParams
            if (savedWidth > 0) {
                lp.width = savedWidth
                lp.height = savedHeight
                lp.marginEnd = savedMarginEnd
            } else {
                lp.width = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT
                lp.marginEnd = 8.dp
            }
            binding.root.layoutParams = lp
            binding.tvPartLabel.setTextColor(LABEL_COLOR_IDLE)
            currentPart()?.let { part ->
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    updatePartLabel(part, pos)
                }
                applySelection(part.id == selectedPartId)
            }
        }

        private fun updatePartLabel(part: ReleaseVideoPart, position: Int) {
            val labelNum = frozenLabelFor(part.id) ?: (position + 1)
            binding.tvPartLabel.text = "P$labelNum"
        }

        private fun bindPartStatusText(part: ReleaseVideoPart) {
            val isError = part.uploadStatus.contains("失败") || part.uploadStatus.contains("预上传失败")
            if (isError) {
                binding.etPartTitle.text = part.uploadStatus
                binding.etPartTitle.setTextColor(0xFFE53935.toInt())
                return
            }
            binding.etPartTitle.setTextColor(0xFF212121.toInt())
            val title = part.displayTitle
            if (binding.etPartTitle.text?.toString() != title) {
                binding.etPartTitle.text = title
            }
        }

        private fun updateTitle(part: ReleaseVideoPart, force: Boolean) {
            bindPartStatusText(part)
        }

        private fun currentPart(): ReleaseVideoPart? {
            val pos = bindingAdapterPosition
            return if (pos != RecyclerView.NO_POSITION) items.getOrNull(pos) else null
        }
    }

    private class PartDiffCallback(
        private val oldList: List<ReleaseVideoPart>,
        private val newList: List<ReleaseVideoPart>,
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].id == newList[newItemPosition].id

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] == newList[newItemPosition]
    }

    private companion object {
        const val PAYLOAD_SELECTION = "selection"
        const val PAYLOAD_DRAG_STATE = "drag_state"
        const val PAYLOAD_PROGRESS = "progress"
        private const val DRAG_CARD_WIDTH_DP = 54
        private const val DRAG_CARD_HEIGHT_DP = 100
        private const val LABEL_COLOR_IDLE = 0xFFB3B3B3.toInt()

        fun hideKeyboard(context: Context, view: View?) {
            val target = view ?: return
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(target.windowToken, 0)
        }
    }
}
