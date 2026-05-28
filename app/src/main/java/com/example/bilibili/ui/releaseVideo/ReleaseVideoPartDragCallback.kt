package com.example.bilibili.ui.releaseVideo

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class ReleaseVideoPartDragCallback(
    private val adapter: ReleaseVideoPartAdapter,
    private val onMoveSuccess: (from: Int, to: Int) -> Unit,
    private val onDragEnded: () -> Unit,
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
    0,
) {
    private var cachedItemAnimator: RecyclerView.ItemAnimator? = null

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder,
    ): Boolean {
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
            return false
        }
        adapter.moveItem(from, to)
        onMoveSuccess(from, to)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    override fun isLongPressDragEnabled(): Boolean = true

    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long,
    ): Int {
        val direction = if (viewSizeOutOfBounds > 0) 1 else -1
        val speed = maxOf(32, abs(viewSizeOutOfBounds) / 2)
        return direction * speed
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            val recyclerView = viewHolder?.itemView?.parent as? RecyclerView
            if (recyclerView != null && cachedItemAnimator == null) {
                cachedItemAnimator = recyclerView.itemAnimator
                recyclerView.itemAnimator = null
            }
            adapter.clearTitleFocus()
            val pos = viewHolder?.bindingAdapterPosition ?: RecyclerView.NO_POSITION
            val partId = adapter.getPartAt(pos)?.id ?: return
            adapter.enterDragMode(partId)
        }
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val droppedPos = viewHolder.bindingAdapterPosition
        adapter.exitDragMode()
        recyclerView.parent?.requestDisallowInterceptTouchEvent(false)
        recyclerView.requestDisallowInterceptTouchEvent(false)
        if (cachedItemAnimator != null) {
            recyclerView.itemAnimator = cachedItemAnimator
            cachedItemAnimator = null
        }
        super.clearView(recyclerView, viewHolder)
        onDragEnded()
        if (droppedPos != RecyclerView.NO_POSITION) {
            recyclerView.post {
                (recyclerView.layoutManager as? LinearLayoutManager)
                    ?.scrollToPositionWithOffset(droppedPos, 0)
            }
        }
    }
}
