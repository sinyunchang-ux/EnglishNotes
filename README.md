# 英文筆記 (EnglishNotes) Android App

## 功能
- 📝 記錄英文句子 + 中文翻譯
- 🎙 每條筆記可錄音（最長 180 秒）
- ▶️ 播放錄音
- 📤 分享錄音到 Line（或任何 App）
- 📊 表格列表視圖（類 Google 試算表）
- 📅 月曆視圖（有錄音=綠色，有筆記=藍色，無=白色）
- 📥 CSV 匯入（格式：英文,中文）
- ✏️ 編輯 / 刪除筆記
- 自動記錄「新增日期」與「錄音日期」

---

## 開啟步驟

1. 解壓縮 `EnglishNotes.zip`
2. 開啟 Android Studio → **File > Open** → 選擇 `EnglishNotes` 資料夾
3. 等待 Gradle Sync 完成（第一次需要下載依賴，約 3-5 分鐘）
4. 連接 Android 手機（或啟動模擬器）
5. 點擊 ▶️ Run 即可安裝

### 系統需求
- Android Studio Hedgehog (2023.1.1) 或更新版本
- Android SDK 26 以上（Android 8.0+）
- JDK 17

---

## CSV 匯入格式

```
英文,中文
I love learning English.,我喜歡學英文。
The weather is nice today.,今天天氣很好。
```

- 第一欄：英文句子（必填）
- 第二欄：中文翻譯（選填）
- 支援有/無 header 行（會自動跳過 "english"/"英文" 標題行）

---

## 專案結構

```
app/src/main/java/com/englishnotes/
├── MainActivity.kt          ← 主畫面 + Tab 導航
├── data/
│   ├── NoteEntity.kt        ← Room 資料表
│   ├── NoteDao.kt           ← 資料庫查詢
│   ├── NoteDatabase.kt      ← Room 資料庫
│   └── NoteRepository.kt    ← 資料層
├── viewmodel/
│   └── NoteViewModel.kt     ← 業務邏輯 + 錄音控制
└── ui/
    ├── Theme.kt             ← 顏色主題
    ├── TableScreen.kt       ← 表格列表畫面
    ├── CalendarScreen.kt    ← 月曆畫面
    ├── AddNoteDialog.kt     ← 新增筆記對話框
    ├── EditNoteDialog.kt    ← 編輯筆記對話框
    └── RecordingDialog.kt   ← 錄音對話框
```
