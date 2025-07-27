# WhisperNote

一個優雅的 Android + Wear OS 筆記同步應用，實現手機與智能手錶之間的無縫創意記錄與同步。

## 🌟 功能特色

### 📱 手機端 (Android App)
- **完整筆記管理**：新增、編輯、刪除想法筆記
- **直觀操作界面**：點擊編輯、長按刪除
- **智能響應**：自動響應手錶的同步請求
- **即時通知**：接收來自手錶的新想法並即時顯示

### ⌚ 手錶端 (Wear OS App)  
- **主動同步**：📱 Sync 按鈕主動拉取手機端完整清單
- **快速新增**：📝 Send 按鈕展開輸入界面，快速記錄靈感
- **暗黑主題**：專為 Wear OS 優化的深色界面
- **圓形適配**：針對圓形錶面特別優化，避免邊緣截斷
- **全屏滾動**：整個畫面統一滾動，提供最佳使用體驗

## 🏗️ 系統架構

### Pull-Based 同步機制
```
手錶主動 ← 手機被動響應
   ↓
1. 手錶: 點擊 Sync → 發送同步請求
2. 手機: 收到請求 → 回傳完整清單  
3. 手錶: 接收清單 → 更新本地顯示
```

### 雙向通訊協議
```
通訊路徑:
├─ /request_list  → 手錶請求清單
├─ /response_list → 手機回傳清單  
└─ /add_idea     → 手錶新增想法
```

## 🛠️ 技術實作

### 核心技術棧
- **語言**: Kotlin
- **UI 框架**: Android Views (手機) + 原生 View (手錶)
- **通訊**: Google Play Services Wearable Data Layer API
- **數據序列化**: Gson (JSON)
- **建置工具**: Gradle with Kotlin DSL

### 專案結構
```
WhisperNote/
├── app/                          # Android 手機模組
│   ├── src/main/
│   │   ├── java/...MainActivity.kt    # 手機端主程式
│   │   └── res/layout/activity_main.xml  # 手機端 UI
│   └── build.gradle.kts          # 手機端建置設定
├── wear/                         # Wear OS 模組  
│   ├── src/main/
│   │   ├── java/...MainActivity.kt    # 手錶端主程式
│   │   └── res/layout/activity_main.xml  # 手錶端 UI
│   └── build.gradle.kts          # 手錶端建置設定
└── README.md                     # 專案說明文檔
```

## 🚀 快速開始

### 環境需求
- **Android Studio**: Arctic Fox 或更新版本
- **Android SDK**: API 30+ (手機), API 33+ (手錶)
- **Kotlin**: 1.8+
- **Gradle**: 8.0+

### 建置步驟

1. **Clone 專案**
   ```bash
   git clone <repository-url>
   cd WhisperNote
   ```

2. **開啟 Android Studio**
   ```
   File → Open → 選擇 WhisperNote 資料夾
   ```

3. **同步專案**
   ```
   點擊 "Sync Project with Gradle Files"
   ```

4. **建置並安裝**
   ```bash
   # 建置兩個模組
   ./gradlew assembleDebug
   
   # 或分別建置
   ./gradlew :app:assembleDebug      # 手機端
   ./gradlew :wear:assembleDebug     # 手錶端
   ```

### 部署設定

1. **手機端**：直接安裝到 Android 手機
2. **手錶端**：安裝到已配對的 Wear OS 設備
3. **配對確認**：確保手機與手錶已透過 Wear OS app 完成配對

## 📱 使用指南

### 手機端操作
1. **新增想法**：在輸入框中輸入 → 點擊「Add」
2. **編輯想法**：點擊清單中的任一項目
3. **刪除想法**：長按清單中的項目 → 確認刪除
4. **查看狀態**：透過狀態欄了解目前操作結果

### 手錶端操作  
1. **同步清單**：點擊「📱 Sync」按鈕拉取最新清單
2. **新增想法**：
   - 點擊「📝 Send」展開輸入框
   - 輸入想法文字
   - 點擊「➤」送出
   - 自動同步更新清單
3. **查看清單**：上下滑動瀏覽所有想法

## 🔧 設定與自訂

### 簽名設定
專案支援 release 建置簽名設定：

```kotlin
// 在 local.properties 中設定：
KEYSTORE_FILE=path/to/your.keystore
KEYSTORE_PASSWORD=your_password  
KEY_ALIAS=your_alias
KEY_PASSWORD=your_key_password
```

### UI 自訂
- **手機端顏色**：修改 `app/res/values/colors.xml`
- **手錶端配色**：調整 `wear/res/layout/activity_main.xml` 中的色彩值
- **文字大小**：調整各 layout 檔案中的 `textSize` 屬性

## 🐛 疑難排解

### 常見問題

**Q: 手錶無法連線到手機？**
- 確認兩個裝置都已安裝對應的 app
- 檢查 Wear OS app 中的配對狀態
- 重新啟動兩個 app

**Q: 同步失敗？**  
- 確認兩個 app 都使用相同的簽名
- 檢查手錶與手機的網路連線
- 重新配對 Wear OS 裝置

**Q: 手錶 UI 顯示異常？**
- 確認使用的是 Wear OS 2.0+ 裝置
- 重新安裝手錶端 app
- 檢查手錶的顯示設定

### Debug 建議
- 使用 `adb logcat` 檢查錯誤訊息
- 確認 Wearable Data Layer API 的權限設定
- 檢查 AndroidManifest.xml 中的設定

## 🔮 未來規劃

### 計劃功能
- [ ] 數據持久化存儲 (Room Database)
- [ ] 雲端同步支援 (Firebase)
- [ ] 語音輸入整合
- [ ] 自訂分類標籤
- [ ] 深色/淺色主題切換
- [ ] 匯出功能 (文字檔、PDF)

### 技術改進
- [ ] 重構為 MVVM 架構
- [ ] Jetpack Compose 遷移
- [ ] Coroutines 優化非同步操作
- [ ] 單元測試覆蓋
- [ ] CI/CD 流程建立

## 📄 授權條款

本專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 文件

## 🤝 貢獻指南

歡迎提交 Issue 和 Pull Request！

1. Fork 本專案
2. 創建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交變更 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 開啟 Pull Request

## 📧 聯絡資訊

如有問題或建議，歡迎透過以下方式聯絡：

- **Issues**: [GitHub Issues](https://github.com/your-repo/WhisperNote/issues)
- **Email**: your-email@example.com

---

⭐ 如果這個專案對你有幫助，請給我們一個星星！