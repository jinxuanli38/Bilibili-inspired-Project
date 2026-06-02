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
        // 获取起末位置
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition
        // 确保位置真实存在
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION) {
            return false
        }
        // 移动位置
        adapter.moveItem(from, to)
        onMoveSuccess(from, to)
        return true
    }

    /**
     * 禁用了侧滑删除
     */
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit

    /**
     * 长按触发拖拽效果
     */
    override fun isLongPressDragEnabled(): Boolean = true

    /**
     * 自动计算滑动速度
     */
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

    /**
     * 选中状态监听
     */
    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        // 查看拖拽状态
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

    /**
     * 拖拽收尾清理
     */
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
