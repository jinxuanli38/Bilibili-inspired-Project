# 粉丝列表：返回对方粉丝数 / 视频数

当前 `loadFansList` 返回示例里**没有** `otherFansCount` / `otherVideoCount`，所以 App 只能显示 `0粉丝 0视频`。

- **Android**：`FansPagingSource` 已解析这两个字段，**无需再改客户端**（改完后端并刷新列表即可）。
- **后端**：必须按下面修改，然后 **重新编译并重启 easylive-web**。

同目录下有 `UserFocus.java.snippet`、`UserFocusMapper.xml.snippet` 可直接复制。

## 1. `UserFocus.java`

在 `private Integer focusType;` 后增加字段与 getter/setter：

```java
private Integer otherFansCount;
private Integer otherVideoCount;

public Integer getOtherFansCount() {
    return otherFansCount;
}

public void setOtherFansCount(Integer otherFansCount) {
    this.otherFansCount = otherFansCount;
}

public Integer getOtherVideoCount() {
    return otherVideoCount;
}

public void setOtherVideoCount(Integer otherVideoCount) {
    this.otherVideoCount = otherVideoCount;
}
```

路径：`easylive-common/src/main/java/com/easylive/entity/po/UserFocus.java`

## 2. `UserFocusMapper.xml`

在 `selectList` 里、`focusType` 子查询之后、`</if>`（关闭 `query.queryType != null`）之前增加（仅粉丝列表 `queryType == 1`）：

```xml
            <if test="query.queryType == 1">
                ,(select count(1) from user_focus f2 where f2.focus_user_id = i.user_id) otherFansCount
                ,(select count(1) from video_info vi where vi.user_id = i.user_id) otherVideoCount
            </if>
```

路径：`easylive-common/src/main/resources/com/easylive/mappers/UserFocusMapper.xml`

## 说明

- `otherFansCount`：该粉丝用户被多少人关注（`user_focus` 中 `focus_user_id = 其 user_id` 的数量）
- `otherVideoCount`：该用户投稿视频总数（`video_info.user_id`）

修改后 `loadFansList` JSON 每条会多出例如：

```json
"otherFansCount": 1021,
"otherVideoCount": 25
```
