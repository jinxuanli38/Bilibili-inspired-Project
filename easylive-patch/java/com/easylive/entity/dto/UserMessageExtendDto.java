package com.easylive.entity.dto;

public class UserMessageExtendDto {
    private String messageContent;
    private String messageContentReply;
    /** 被点赞/收藏的目标评论ID，视频类消息为 0 */
    private Integer commentId;
    /** comment：右侧展示评论文案；video：右侧展示视频封面 */
    private String previewType;
    // 审核状态
    private Integer auditStatus;

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public String getMessageContentReply() {
        return messageContentReply;
    }

    public void setMessageContentReply(String messageContentReply) {
        this.messageContentReply = messageContentReply;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getPreviewType() {
        return previewType;
    }

    public void setPreviewType(String previewType) {
        this.previewType = previewType;
    }

    public Integer getAuditStatus() {
        return auditStatus;
    }

    public void setAuditStatus(Integer auditStatus) {
        this.auditStatus = auditStatus;
    }
}
