# WhisperNote - 開發者筆記

## 🏗️ 架構決策記錄

### 1. Pull-Based 同步機制選擇
**決策**: 採用手錶主動拉取，而非手機主動推送
**原因**: 
- 手錶端可控制同步時機，節省電池
- 避免手機需要主動發現手錶裝置的複雜性
- 更符合 Wear OS 的設計哲學（手錶主動獲取需要的資訊）

### 2. 使用 Message API 而非 Data API
**決策**: 使用 MessageClient 進行通訊
**原因**:
- 即時性要求：需要立即的請求-響應機制
- 資料一致性：避免 Data API 的快取問題
- 控制權：手錶端完全控制何時同步

### 3. Wear OS UI 設計選擇
**決策**: 使用 LinearLayout + ScrollView 而非 ListView
**原因**:
- 圓形錶面適配：整體滾動避免清單被邊緣截斷
- 使用體驗：全屏滾動比小區域滾動更自然
- 自訂性：可完全控制每個項目的外觀和間距

## 🔧 技術實作細節

### Wearable 通訊協議
```kotlin
// 通訊路徑常數
const val REQUEST_LIST_PATH = "/request_list"    // 手錶 → 手機
const val RESPONSE_LIST_PATH = "/response_list"  // 手機 → 手錶  
const val ADD_IDEA_PATH = "/add_idea"           // 手錶 → 手機

// 訊息格式
REQUEST_LIST: "sync_request".toByteArray()
RESPONSE_LIST: gson.toJson(ideasList).toByteArray()
ADD_IDEA: newIdeaText.toByteArray()
```

### 錯誤處理策略
1. **連線失敗**: 顯示明確的錯誤訊息，提示檢查配對狀態
2. **同步超時**: 使用 Toast 提示用戶重試
3. **資料格式錯誤**: Gson 解析失敗時回退到空清單

### UI 響應式設計
```kotlin
// 手錶端動態 UI 更新
private fun updateIdeasDisplay() {
    ideasContainer.removeAllViews()
    for (idea in ideasList) {
        val ideaView = createIdeaTextView(idea)
        ideasContainer.addView(ideaView)
    }
}
```

## 🎨 UI/UX 設計考量

### Wear OS 特殊處理
1. **圓形錶面適配**: 底部預留 24dp 避免內容被邊緣切掉
2. **Dark Mode 優化**: 使用高對比度配色提升可讀性
3. **觸控優化**: 最小觸控目標 48dp，實際使用 72px 提供更好體驗

### 配色選擇
```xml
背景: #000000 (純黑，省電)
主容器: #1A1A1A (深灰)
清單項目: #2A2A2A (中灰)
文字: #FFFFFF (高對比白)
按鈕:
  - Sync: #2196F3 (藍色，代表資料獲取)
  - Send: #4CAF50 (綠色，代表發送動作)  
  - Submit: #FF5722 (橘紅，代表確認動作)
```

## 🚫 已知限制與解決方案

### 1. 資料持久化
**限制**: 目前僅使用記憶體存儲
**影響**: App 重啟後手機端資料會重置為範例資料
**未來解決**: 整合 Room Database

### 2. 多裝置支援  
**限制**: 目前假設一對一配對 (一手機對一手錶)
**影響**: 無法支援多個手錶連接到同一手機
**未來解決**: 加入裝置管理機制

### 3. 離線同步
**限制**: 需要手機與手錶保持連線
**影響**: 斷線時無法同步
**未來解決**: 實作離線佇列機制

## 🔍 Debug 指南

### 常用 ADB 指令
```bash
# 檢查手錶連線狀態
adb devices

# 安裝 APK 到手錶 (透過手機)
adb -s <phone_device> install wear_app.apk

# 即時查看 Log
adb logcat | grep "WhisperNote"

# 清除 App 資料
adb shell pm clear com.jovicheer.whispernote
```

### Log 標籤策略
```kotlin
// 建議在各個主要功能加入 Log
Log.d("WhisperNote_Phone", "Sending ideas list: ${ideasList.size} items")
Log.d("WhisperNote_Wear", "Received sync response: $message")
Log.e("WhisperNote_Error", "Failed to send message: ${exception.message}")
```

## 📋 測試檢查清單

### 手機端測試
- [ ] 新增想法功能
- [ ] 編輯想法功能  
- [ ] 刪除想法功能
- [ ] 響應手錶同步請求
- [ ] 接收手錶新想法

### 手錶端測試
- [ ] Sync 按鈕同步功能
- [ ] Send 按鈕展開/收起
- [ ] 新想法輸入和送出
- [ ] 清單滾動體驗
- [ ] 圓形錶面邊緣顯示

### 整合測試
- [ ] 配對狀態檢查
- [ ] 斷線重連機制
- [ ] 同時操作衝突處理
- [ ] 效能壓力測試 (大量想法)

## 🔧 建置設定說明

### Gradle 設定重點
```kotlin
// 確保兩個模組使用相同的簽名
signingConfigs {
    create("release") {
        // 使用相同的 keystore 確保可信任連線
    }
}

// 重要依賴
implementation("com.google.android.gms:play-services-wearable:18.1.0")
implementation("com.google.code.gson:gson:2.10.1")
```

### AndroidManifest.xml 關鍵設定
```xml
<!-- Wear OS 專用 -->
<uses-feature android:name="android.hardware.type.watch" />
<uses-library android:name="com.google.android.wearable" android:required="true" />
<meta-data android:name="com.google.android.wearable.standalone" android:value="false" />
```

## 💡 效能優化建議

### 記憶體管理
- 使用 `removeAllViews()` 避免 View 記憶體洩漏
- Gson 實例重用，避免重複創建
- 適當的生命週期管理 (onResume/onPause)

### 電池優化
- 僅在需要時註冊 MessageClient listener  
- 避免不必要的背景操作
- 使用 `setUrgent()` 僅於真正緊急時

### 網路效率
- 資料壓縮：JSON 格式相對輕量
- 批次操作：避免頻繁的小量通訊
- 錯誤重試：適當的重試機制避免無效請求

---

**最後更新**: 2024年 - 專案第一版完成
**維護者**: Claude & Development Team