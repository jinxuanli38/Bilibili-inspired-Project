# 编辑稿件自动回填分区

## 现象

进入编辑页后分区显示「选择分区」，保存时提示需手动选择，但稿件本身已有分区。

## Android（本仓库已修复）

- `CategoryPartitionHelper.readVideoCategoryIds`：兼容 **`pcategoryId`**（`getVideoInfoByVideoId` 实际返回）、`pCategoryId`、`p_category_id` 等。
- `resolveFromIds`：支持仅存在二级 `categoryId` 时在分类树反查一级分区；校验接口 `success`/`code==200`。
- `loadVideoForEdit`：一级或二级任一有值即拉取分类树并 `selectedPartition` 回填。

## 后端建议（easylive-common）

`CategoryInfoServiceImpl.getAllCategoryInfo` 在 Redis 为空时只写了缓存但未重新读取，可能返回空列表，导致客户端解析不到分区名称：

```java
@Override
public List<CategoryInfo> getAllCategoryInfo() {
    List<CategoryInfo> categoryInfoList = redisComponent.getCategoryInfo();
    if (categoryInfoList == null || categoryInfoList.isEmpty()) {
        saveCategoryInfo2Redis();
        categoryInfoList = redisComponent.getCategoryInfo();
    }
    return categoryInfoList == null ? Collections.emptyList() : categoryInfoList;
}
```

已在 `Downloads/easylive-main/.../CategoryInfoServiceImpl.java` 应用。

## Android 补充（2025）

- 编辑时缓存 `editPCategoryId` / `editCategoryId`，解析失败仍可保存。
- `effectiveCategoryIds()` 提交时用缓存 ID。
- `applyPartitionForEdit` 清分类缓存并重试解析名称。
