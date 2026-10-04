package com.zyx.consultant.knowledge;

/**
 * 知识文档在导入和发布过程中的生命周期状态。
 */
public enum DocumentStatus {
    DRAFT,
    PROCESSING,
    PUBLISHED,
    FAILED,
    ARCHIVED
}
