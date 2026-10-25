# 🔥 LeetCode Streak Widget for Android

An aesthetic, modern, and battery-friendly Android widget suite dedicated to tracking your **LeetCode Streak** with **100% accuracy**.

[![Build LeetCode Streak Widget APK](https://github.com/TonyStarkCodes/leetcode-streak-widget/actions/workflows/build-apk.yml/badge.svg)](https://github.com/TonyStarkCodes/leetcode-streak-widget/actions/workflows/build-apk.yml)
[![Release](https://img.shields.io/github/v/release/TonyStarkCodes/leetcode-streak-widget?color=orange&label=Latest%20APK)](https://github.com/TonyStarkCodes/leetcode-streak-widget/releases)

---

## 🎯 Why Most LeetCode Streak Apps Get It Wrong

Many third-party LeetCode apps show inaccurate streak counts or prematurely reset your streak. Here is why:

1. **UTC vs. Local Timezone Collisions:**
   LeetCode backend stores submission timestamps as midnight **00:00:00 UTC** Unix timestamps. When apps convert timestamps directly using local device timezones, submissions in the Western hemisphere shift backwards by 1 day, causing artificial gaps, while Eastern hemisphere submissions can cross boundaries unexpectedly.
   > **Our Solution:** We normalize submission days according to UTC epoch day boundaries (matching LeetCode's official algorithm), while cross-validating with device local day to ensure submissions are detected seamlessly worldwide.

2. **The "Today in Progress" Trap:**
   When a user has solved yesterday's problem, their streak is still alive! Many apps check if `submissions[today] > 0`, and if not, immediately reset the streak to 0, alarming the user in the morning.
   > **Our Solution:** A streak is never broken while today is still in progress. If yesterday was active, the streak is preserved with a clear `Pending Today ⚠️` status until midnight UTC.

3. **Backend Cache Lags:**
   LeetCode's GraphQL profile API sometimes has a propagation delay between `submissionCalendar` updates and `userCalendar.streak`.
   > **Our Solution:** We cross-calculate the streak directly from raw submission timestamps and reconcile it with LeetCode's official streak token, ensuring the displayed number is always accurate.

---

## ✨ Features & Widgets

### 📱 3 Modern Widget Sizes
- **2x1 Compact Flame:** Minimalist fire icon, bold streak count, username handle, and active status pill.
- **2x2 Square Widget:** Large streak counter, flame badge, solved problems count, and a **1-tap Refresh button** right on the widget.
- **4x2 Rich Dashboard:** Full overview with current & max streak, total active days, Easy / Medium / Hard problem breakdown with signature LeetCode colors, global ranking, and instant refresh.

### 🌓 Adaptive Light & Dark Mode
- Built using native Android RemoteViews resource qualifiers (`values` and `values-night`).
- Automatically transitions between sleek dark slate (`#18181B`) and crisp light theme (`#FFFFFF`) to match your device's system appearance.

### 🔋 Battery Drain Prevention
- **0% Background Drain:** By default, widgets update strictly when you tap the **Refresh (⟳)** button in the app or directly on the widget.
- **Optional Battery-Safe Periodic Sync:** If enabled, Android WorkManager only triggers every 12 hours under `RequiresBatteryNotLow` and `RequiresNetworkConnected` constraints. No persistent background services, no wakelocks.

### ⚡ Direct Pinning
- Add any of the 3 widgets directly to your Home Screen with a single tap inside the app.

---

## 🚀 Download & Installation

### Option 1: Download from GitHub Releases (Recommended)
1. Go to **[Releases](https://github.com/TonyStarkCodes/leetcode-streak-widget/releases)**.
2. Download the latest `leetcode-streak-widget.apk`.
3. Open the APK on your Android device and tap **Install** (allow "Install from Unknown Sources" if prompted).

### Option 2: Download from GitHub Actions
1. Go to the **[Actions Tab](https://github.com/TonyStarkCodes/leetcode-streak-widget/actions)** in this repository.
2. Click on the latest workflow run.
3. Scroll down to **Artifacts** and download `leetcode-streak-widget`.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Widgets:** Android AppWidgetProvider + RemoteViews (Multi-size, Android 8.0 - 15+)
- **Networking:** OkHttp 4 (LeetCode GraphQL API)
- **Background Tasks:** Android Jetpack WorkManager
- **CI/CD:** GitHub Actions (Automated APK build, artifact upload, and GitHub Release creation)

---

## 👤 Author
- **GitHub:** [@TonyStarkCodes](https://github.com/TonyStarkCodes)
