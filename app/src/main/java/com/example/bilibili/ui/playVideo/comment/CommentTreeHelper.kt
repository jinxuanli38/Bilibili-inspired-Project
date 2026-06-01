package com.example.bilibili.ui.playVideo.comment

import com.example.bilibili.data.model.CommentItem

object CommentTreeHelper {

    fun findById(comments: List<CommentItem>, commentId: Int): CommentItem? {
        for (comment in comments) {
            if (comment.commentId == commentId) return comment
            val nested = comment.children?.let { findById(it, commentId) }
            if (nested != null) return nested
        }
        return null
    }

    fun findByAnchor(
        comments: List<CommentItem>,
        sendUserId: String?,
        content: String?,
    ): CommentItem? {
        if (sendUserId.isNullOrBlank() && content.isNullOrBlank()) return null
        fun walk(nodes: List<CommentItem>): CommentItem? {
            for (node in nodes) {
                if (matchesAnchor(node, sendUserId, content)) return node
                node.children?.let { walk(it) }?.let { return it }
            }
            return null
        }
        return walk(comments)
    }

    fun containsDescendant(root: CommentItem, commentId: Int): Boolean {
        if (root.commentId == commentId) return true
        return root.children.orEmpty().any { containsDescendant(it, commentId) }
    }

    /**
     * 获取某条评论下的直接回复。优先 children，再 pCommentId，再处理「都挂在根评论下」的楼中楼。
     */
    fun directReplies(allRoots: List<CommentItem>, parentCommentId: Int): List<CommentItem> {
        val parent = findById(allRoots, parentCommentId)
        val fromChildren = parent?.children.orEmpty()
        if (fromChildren.isNotEmpty()) return fromChildren

        val byParentId = collectByParentId(allRoots, parentCommentId)
        if (byParentId.isNotEmpty()) return byParentId

        if (parent != null) {
            val flat = collectFlatRepliesTargeting(allRoots, parent)
            if (flat.isNotEmpty()) return flat
        }
        return emptyList()
    }

    /**
     * 消息页进入评论详情：在 [directReplies] 为空时，用锚点评论或扁平楼中楼规则兜底。
     */
    fun repliesForMessageThread(
        allRoots: List<CommentItem>,
        parentCommentId: Int,
        anchorSendUserId: String?,
        anchorContent: String?,
    ): List<CommentItem> {
        val direct = directReplies(allRoots, parentCommentId)
        if (direct.isNotEmpty()) return direct

        val anchor = findByAnchor(allRoots, anchorSendUserId, anchorContent)
        if (anchor != null) {
            if (anchor.pCommentId == parentCommentId) {
                return listOf(anchor)
            }
            val viaAnchorParent = directReplies(allRoots, anchor.pCommentId)
            if (viaAnchorParent.isNotEmpty()) return viaAnchorParent
            return listOf(anchor)
        }

        val parent = findById(allRoots, parentCommentId)
        if (parent != null) {
            val flat = collectFlatRepliesTargeting(allRoots, parent)
            if (flat.isNotEmpty()) return flat
        }
        return emptyList()
    }

    private fun matchesAnchor(item: CommentItem, sendUserId: String?, content: String?): Boolean {
        if (!sendUserId.isNullOrBlank() && sendUserId != item.userId) return false
        if (!content.isNullOrBlank() && content.trim() != item.content.trim()) return false
        return true
    }

    private fun collectByParentId(comments: List<CommentItem>, parentCommentId: Int): List<CommentItem> {
        val result = mutableListOf<CommentItem>()
        fun walk(nodes: List<CommentItem>) {
            for (node in nodes) {
                if (node.pCommentId == parentCommentId) {
                    result.add(node)
                }
                node.children?.let { walk(it) }
            }
        }
        walk(comments)
        return result
    }

    /**
     * 部分后端把二、三级回复都挂在根评论的 children 里，且 pCommentId 仍指向根评论；
     * 此时用 replyNickName / replyUserId 判断「回复了哪一条」。
     */
    private fun collectFlatRepliesTargeting(
        allRoots: List<CommentItem>,
        parent: CommentItem,
    ): List<CommentItem> {
        val rootId = parent.pCommentId.takeIf { it > 0 } ?: parent.commentId
        val root = findById(allRoots, rootId) ?: return emptyList()
        val result = mutableListOf<CommentItem>()
        fun walk(nodes: List<CommentItem>) {
            for (node in nodes) {
                if (isReplyTargeting(node, parent)) {
                    result.add(node)
                }
                node.children?.let { walk(it) }
            }
        }
        walk(root.children.orEmpty())
        return result
    }

    private fun isReplyTargeting(node: CommentItem, parent: CommentItem): Boolean {
        if (node.commentId == parent.commentId) return false
        if (node.pCommentId == parent.commentId) return true
        val rootId = parent.pCommentId.takeIf { it > 0 } ?: return false
        if (node.pCommentId != rootId) return false
        if (!parent.replyNickName.isNullOrBlank() && node.replyNickName == parent.nickName) {
            return true
        }
        if (parent.userId.isNotBlank() && node.replyUserId == parent.userId) {
            return true
        }
        return false
    }
}
