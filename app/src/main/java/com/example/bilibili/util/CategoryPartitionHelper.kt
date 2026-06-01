package com.example.bilibili.util

import com.example.bilibili.data.model.CategoryInfo
import org.json.JSONArray
import org.json.JSONObject

object CategoryPartitionHelper {

    fun displayName(info: CategoryInfo?): String {
        if (info == null) return ""
        val parent = info.categoryName.trim()
        val sub = info.subCategoryName?.trim().orEmpty()
        return when {
            parent.isNotEmpty() && sub.isNotEmpty() -> "$parent · $sub"
            sub.isNotEmpty() -> sub
            else -> parent
        }
    }

    /** 从稿件 JSON 读取一级/二级分区 ID（兼容多种字段名）。 */
    fun readVideoCategoryIds(videoInfo: JSONObject): Pair<Int, Int> {
        var pCategoryId = optCategoryInt(
            videoInfo,
            "pcategoryId", // getVideoInfoByVideoId 实际返回字段（Jackson 序列化 VideoInfoPost）
            "pCategoryId",
            "PCategoryId",
            "p_category_id",
        )
        if (pCategoryId == 0) {
            pCategoryId = optCategoryIntByKeyPattern(videoInfo, "pcategory")
        }
        var categoryId = optCategoryInt(
            videoInfo,
            "categoryId",
            "CategoryId",
            "category_id",
        )
        if (categoryId == 0) {
            categoryId = optCategoryIntByKeyPattern(videoInfo, "categoryid") { key ->
                !key.contains("pcategory", ignoreCase = true)
            }
        }
        return pCategoryId to categoryId
    }

    private fun optCategoryInt(json: JSONObject, vararg keys: String): Int {
        for (key in keys) {
            if (!json.has(key) || json.isNull(key)) continue
            parseCategoryValue(json.get(key))?.let { return it }
        }
        return 0
    }

    /** 按键名模糊匹配（忽略大小写、下划线），兼容 pcategoryId 等序列化差异。 */
    private fun optCategoryIntByKeyPattern(
        json: JSONObject,
        keyContains: String,
        keyFilter: (String) -> Boolean = { true },
    ): Int {
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            if (!key.replace("_", "").contains(keyContains, ignoreCase = true)) continue
            if (!keyFilter(key)) continue
            if (json.isNull(key)) continue
            parseCategoryValue(json.get(key))?.let { return it }
        }
        return 0
    }

    private fun parseCategoryValue(value: Any?): Int? = when (value) {
        is Number -> value.toInt()
        is String -> value.toIntOrNull()
        else -> null
    }

    fun parseCategoryTreeData(categoryTreeResponse: String): JSONArray? {
        if (!ApiResponseHelper.isSuccess(categoryTreeResponse)) return null
        return try {
            val data = JSONObject(categoryTreeResponse).optJSONArray("data") ?: return null
            if (data.length() == 0) null else data
        } catch (_: Exception) {
            null
        }
    }

    /**
     * 根据稿件里的 pCategoryId（一级）和 categoryId（二级，可为 0）解析完整分区信息。
     * 若仅存在二级 ID，会在分类树中反查所属一级分区。
     */
    fun resolveFromIds(pCategoryId: Int, categoryId: Int, categoryTreeResponse: String): CategoryInfo? {
        val data = parseCategoryTreeData(categoryTreeResponse) ?: return null
        return resolveFromIds(pCategoryId, categoryId, data)
    }

    fun resolveFromIds(pCategoryId: Int, categoryId: Int, data: JSONArray): CategoryInfo? {
        if (pCategoryId > 0) {
            resolveFromTree(pCategoryId, categoryId, data)?.let { return it }
        }
        if (categoryId > 0) {
            resolveBySubCategoryId(categoryId, data)?.let { return it }
        }
        return null
    }

    private fun resolveFromTree(
        pCategoryId: Int,
        categoryId: Int,
        data: JSONArray,
    ): CategoryInfo? {
        for (i in 0 until data.length()) {
            val parent = data.getJSONObject(i)
            if (parent.optInt("categoryId") != pCategoryId) continue
            val parentName = parent.optString("categoryName")
            if (categoryId <= 0 || categoryId == pCategoryId) {
                return CategoryInfo(
                    categoryId = pCategoryId,
                    categoryName = parentName,
                )
            }
            val children = parent.optJSONArray("children") ?: JSONArray()
            for (j in 0 until children.length()) {
                val child = children.getJSONObject(j)
                if (child.optInt("categoryId") == categoryId) {
                    return CategoryInfo(
                        categoryId = pCategoryId,
                        categoryName = parentName,
                        subCategoryId = categoryId,
                        subCategoryName = child.optString("categoryName"),
                    )
                }
            }
            return CategoryInfo(categoryId = pCategoryId, categoryName = parentName)
        }
        return null
    }

    private fun resolveBySubCategoryId(subCategoryId: Int, data: JSONArray): CategoryInfo? {
        for (i in 0 until data.length()) {
            val parent = data.getJSONObject(i)
            val parentId = parent.optInt("categoryId")
            val parentName = parent.optString("categoryName")
            if (parentId == subCategoryId) {
                return CategoryInfo(categoryId = subCategoryId, categoryName = parentName)
            }
            val children = parent.optJSONArray("children") ?: continue
            for (j in 0 until children.length()) {
                val child = children.getJSONObject(j)
                if (child.optInt("categoryId") == subCategoryId) {
                    return CategoryInfo(
                        categoryId = parentId,
                        categoryName = parentName,
                        subCategoryId = subCategoryId,
                        subCategoryName = child.optString("categoryName"),
                    )
                }
            }
        }
        return null
    }
}
