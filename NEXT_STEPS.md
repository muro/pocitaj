# Next Steps & Project Roadmap

This document captures the remaining tasks and vision for the Pocitaj app. It serves as a backlog for future development sessions.

## 🎨 Audio & Visual Polishing
- **Satisfying Soundscape**
    - **Details**: Add SFX for button clicks, correct/incorrect answer strokes, and level completion/new star earned.
    - **Difficulty**: 6/10 (Low code complexity, but depending on external data)
    - **Dependencies**: `SoundManager.kt`, audio assets in `res/raw`.
    - **Risk**: Low. Safe UI enhancement.
- **Vibrant Color Palette**
    - **Details**: Revitalize the UI with high-contrast, playful colors. Replace "beige" backgrounds with dynamic gradients or clean surface colors that adapt to light/dark modes.
    - **Difficulty**: 6/10 (Medium, needs to define an external palette and design)
    - **Dependencies**: `Theme.kt`, `Color.kt`, global background modifiers.
    - **Risk**: Low. Purely visual.
- **Playful Moments**
    - **Details**: Introduce animated GIFs or Lottie animations on success (sushi, cats, animals) to delight users after completing exercises.
    - **Difficulty**: 3/10 (Low, but depending on external data)
    - **Dependencies**: `ResultsScreen.kt`, `ConfettiAnimation.kt`, animation assets.
    - **Risk**: Low.
- **Immersive Exercise UX**
  - **Details**: Make the exercise screen much more animated and friendly. The drawing card should
    animate from the missing part of the equation to its large input state, making it clear what is
    being filled. Use more vibrant, inviting colors for the input area.
  - **Difficulty**: 7/10 (Medium-High)
  - **Dependencies**: `ExerciseScreen.kt`, `EquationDisplay.kt`, Shared Element-like transitions.
  - **Risk**: Medium. Requires complex coordinated animations.

## 🏆 Gamification & Engagement
- **Achievement System**
    - **Details**: Implement a daily achievement tracker (e.g., "50 Correct Answers," "3 New Stars"). Requires a new database entity to persist "unlocked" status.
    - **Difficulty**: 7/10 (High)
    - **Dependencies**: Room DB (`Achievement` entity), `ExerciseViewModel` (to trigger unlocks), new UI screen for awards.
    - **Risk**: Medium. Requires database schema changes and careful state management.
- **Daily Practice Goals**
    - **Details**: Rework "Smart Practice" to offer tiered goals (10, 30, 50 exercises). The `TodaysCatchTracker` should reward the user with different "Sushi" categories at each tier.
    - **Difficulty**: 5/10 (Medium)
    - **Dependencies**: `TodaysCatchTracker.kt`, `HistoryViewModel.kt`.
    - **Risk**: Low. Mostly logic and UI updates.
- **"Boss Battle" Review Levels**
    - **Details**: Design review levels as milestones. Give them distinct icons (e.g., Shields/Crowns) and borders in the curriculum list. Clearly state which levels they verify.
    - **Difficulty**: 4/10 (Medium)
    - **Dependencies**: `Curriculum.kt`, `LevelProgressItem.kt`, `StarRatingDisplay.kt`.
    - **Risk**: Low. Safe enhancement of existing level logic.

## 📚 Curriculum & Logic
- **Operand Expansion**
  - **Details**: Add missing pedagogical levels. E.g., Subtraction with borrowing.
    - **Difficulty**: 2/10 (Low)
    - **Dependencies**: `Curriculum.kt`, `Level.kt`.
    - **Risk**: Low. Pure logic additions.
- **Progress Visualization & Setup Merge**
    - **Details**: Evaluate if the multiplication grid should be integrated directly into the `ExerciseSetupScreen` to help users pick which level to practice next based on "cold" spots.
    - **Difficulty**: 4/10 (Medium)
    - **Dependencies**: `ProgressReportScreen.kt`, `ExerciseSetupScreen.kt`.
    - **Risk**: Low. Involves moving/refactoring UI components.
- **Tighten Simulation Velocity Guards**
    - **Details**: Our current simulations use very generous `maxExpected` limits (e.g., 20,000 exercises). Based on gathered data (Pure Beginner completes Addition in ~580 exercises, Adaptive in ~1800), we should tighten these guards to catch regressions that bloat the learning experience.
    - **Difficulty**: 2/10 (Low)
    - **Dependencies**: `StrategySimulationTest.kt`.
    - **Risk**: Low.

## 📱 User Experience & Platform

- **Enhanced Profile Editing** [DONE (partially)]
    - **Details**: Add more diverse and polished icons. Implement "Automatic Color Assignment" that picks an unused color from the palette for new users.
    - **Difficulty**: 3/10 (Low)
    - **Dependencies**: `UserProfileScreen.kt`, `UserAppearance.kt`.
    - **Risk**: Low. Safe UI/Logic tweak.
- **Adaptive Layouts (Tablet/Fold/Landscape)**
    - **Details**: Implement `WindowSizeClass` support. Create "List-Detail" views for large screens and ensure everything is reachable in landscape mode.
    - **Difficulty**: 8/10 (High)
    - **Dependencies**: Global layout changes in all major screens (`ProgressReport`, `Setup`, `Exercise`).
    - **Risk**: Medium. High chance of visual regressions on standard phones if not careful.
- **Unified Icons**
    - **Details**: Finalize a distinct, high-quality icon for every level in the curriculum.
    - **Difficulty**: 2/10 (Low)
    - **Dependencies**: `Level.kt`, `Curriculum.kt`, image assets.
    - **Risk**: Low.

## ✅ Completed (Recent)

- **Automatic Profile Assignment**: Unique, randomized color and icon assignment for new users is
  implemented and verified.
- **Test Infrastructure Consolidation**: Core DAO fakes unified and moved to `debug` source set to
  prevent production leakage.
