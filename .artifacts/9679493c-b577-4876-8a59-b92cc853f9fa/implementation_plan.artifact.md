# Modern Media App Upgrade Plan

This plan outlines the steps to transform the current photo gallery into a modern, Apple-inspired media app with video support, AI search, and a translucent, curved UI.

## User Review Required

> [!IMPORTANT]
> **New Permissions**: We will need to add `READ_MEDIA_VIDEO` permission for Android 13+ and `READ_EXTERNAL_STORAGE` for older versions to support video discovery.
> [!NOTE]
> **Glassmorphism Styling**: The UI will use `Brush.verticalGradient` and semi-transparent backgrounds to achieve the "Apple-style" translucent look.

## Proposed Changes

### 1. Dependencies & Data Model

#### [MODIFY] [libs.versions.toml](file:///D:/Kotlin%20Applications/gradle/libs.versions.toml)
- Add `androidx.media3` dependencies for video playback (ExoPlayer).
- Add `androidx.compose.material:material-icons-extended` for a richer set of icons.

#### [MODIFY] [MediaItem.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/data/model/MediaItem.kt)
- Add `MediaType` enum (IMAGE, VIDEO).
- Add `duration` and `mimeType` fields to support video metadata.

---

### 2. Media Discovery (Repository & ViewModel)

#### [MODIFY] [MediaRepository.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/data/repository/MediaRepository.kt)
- Update `getImages()` to `getMedia()` which queries both `MediaStore.Images` and `MediaStore.Video`.

#### [MODIFY] [HomeViewModel.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/ui/home/HomeViewModel.kt)
- Add search logic to filter `photos` (now `mediaItems`) by name.
- Add filtering logic for "Photos" vs "Videos" views.
- Implement "AI Search" stub (triggered by a search query).

---

### 3. Modern UI Overhaul

#### [MODIFY] [HomeScreen.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/ui/home/HomeScreen.kt)
- **Top Bar**: Add a modern, translucent search bar with rounded corners.
- **Floating Buttons**: Implement a stacked floating button group at the bottom:
    - AI Search Button (Top)
    - Video Filter Button (Middle)
    - Photo Filter Button (Bottom)
- **Grid Styling**: Increase corner radius for grid items and the overall screen container.

#### [NEW] [MediaDetailScreen.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/ui/home/MediaDetailScreen.kt)
- Implement a `HorizontalPager` to allow swiping through media.
- Integrate `ExoPlayer` for video playback within the pager.
- Support full-screen "immersive" mode.

#### [MODIFY] [MainActivity.kt](file:///D:/Kotlin%20Applications/app/src/main/java/com/example/myfirstandroidapp/MainActivity.kt)
- Update permission requests to include `Manifest.permission.READ_MEDIA_VIDEO`.
- Setup Navigation3 or simple state-based navigation to handle the detail view.

## Verification Plan

### Automated Tests
- Build and run `:app:assembleDebug`.
- Verify `MediaRepository` fetches both types of media via logs.

### Manual Verification
- **Visual Check**: Inspect the "Glassmorphism" search bar and floating buttons on an emulator/device.
- **Interaction**: Tap a photo/video to open the detail view; verify swiping left/right navigates correctly.
- **Video Playback**: Confirm videos play automatically when scrolled to in the detail view.
- **Search**: Type in the search bar to verify real-time filtering.
