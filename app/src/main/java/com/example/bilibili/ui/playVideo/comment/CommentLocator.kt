package com.example.bilibili.ui.playVideo.comment

import com.example.bilibili.data.model.CommentItem
import com.example.bilibili.ui.playVideo.CommentAnchor
import com.example.bilibili.ui.playVideo.matches

object CommentLocator {

    data class LocateResult(
        val highlightCommentId: Int,
        val expandParentCommentId: Int? = null,
    )

    fun findInList(list: List<CommentItem>, anchor: CommentAnchor): LocateResult? {
        if (anchor.content.isNotBlank() || anchor.sendUserId.isNotBlank()) {
            for (item in list) {
                if (anchor.matches(item.userId, item.content)) {
                    return LocateResult(highlightCommentId = item.commentId)
                }
                findInChildren(item, anchor, item.commentId)?.let { return it }
            }
        }
        if (anchor.parentCommentId > 0) {
            findByCommentId(list, anchor.parentCommentId)?.let { return it }
        }
        return null
    }

    private fun findInChildren(
        parent: CommentItem,
        anchor: CommentAnchor,
        expandRootId: Int,
    ): LocateResult? {
        for (child in parent.children.orEmpty()) {
            if (anchor.matches(child.userId, child.content)) {
                return LocateResult(
                    highlightCommentId = child.commentId,
                    expandParentCommentId = expandRootId,
                )
            }
            findInChildren(child, anchor, expandRootId)?.let { return it }
        }
        return null
    }

    fun findByCommentId(list: List<CommentItem>, commentId: Int): LocateResult? {
        for (root in list) {
            if (root.commentId == commentId) {
                return LocateResult(highlightCommentId = commentId)
            }
            locateInSubtree(root, commentId, root.commentId)?.let { return it }
        }
        return null
    }

    private fun locateInSubtree(
        parent: CommentItem,
        targetId: Int,
        expandRootId: Int,
    ): LocateResult? {
        for (child in parent.children.orEmpty()) {
            if (child.commentId == targetId) {
                return LocateResult(
                    highlightCommentId = targetId,
                    expandParentCommentId = expandRootId,
                )
            }
            locateInSubtree(child, targetId, expandRootId)?.let { return it }
        }
        return null
    }

    fun findInFlatList(list: List<CommentItem>, anchor: CommentAnchor): LocateResult? {
        for (item in list) {
            if (anchor.matches(item.userId, item.content)) {
                return LocateResult(highlightCommentId = item.commentId)
            }
        }
        return null
    }
}
