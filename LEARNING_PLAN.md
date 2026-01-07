# Project Plan: Arithmetic App Overhaul

**Goal:** Transform the arithmetic app from a simple random quiz generator into a structured, engaging, and effective learning tool that teaches both accuracy and speed.

---

## Phase 1: Foundational Rework (Core Logic & UI)

*This phase replaces the old random system with a structured, level-based approach and implements the new single-screen user interface.*

### ### Step 1.1: Define the Learning Structure [DONE]

* **Action:** Create a data structure to define all learning levels.
* **Details:**
    * For **Addition** & **Subtraction**: Define concept-based levels like `ADDITION_UP_TO_10`, `ADDITION_UP_TO_20`, `ADDITION_WITH_CARRYING`.
    * For **Multiplication** & **Division**: Define number-based levels for each table from 1 to 12 (e.g., `MULTIPLICATION_TABLE_8`).

### ### Step 1.2: Add Mixed Review & Dependency Logic [DONE]

* **Action:** Enhance the learning structure with review levels and a prerequisite system.
* **Details:**
    * **Mixed Review:** After a user masters a set number of levels (e.g., 3 multiplication tables), automatically create a "Mixed Review" level that combines problems from all of them to ensure long-term retention.
    * **Level Dependencies:** Define prerequisites for levels (e.g., `ADDITION_UP_TO_20` requires `ADDITION_UP_TO_10`). The UI should show locked levels as grayed out or with a lock icon (🔒) until the prerequisite is met.

### ### Step 1.3: Build the Smart Problem Generator [DONE]

* **Action:** Update the exercise generation logic to support the new level types.
* **Details:**
    * The function should accept a level ID (e.g., `ADDITION_UP_TO_10`, `MIXED_REVIEW_1`) and generate a valid random problem for that level.

### ### Step 1.4: Implement the Expandable Card UI [DONE]

* **Action:** Build the new main menu using a single-screen layout.
* **Details:**
    * Create a list of expandable cards, one for each operation (`+`, `-`, `×`, `÷`).
    * **Collapsed State:** The card shows the operation name and a summary of progress (e.g., "Multiplication: 3 of 12 tables mastered").
    * **Expanded State:** Tapping a card expands it to show buttons for all its available levels, including locked and mixed review levels.

### ### Step 1.5: Implement 0-3 Star Progress System [DONE]

* **Action:** Create a clear, motivating 0-3 star rating system for each level.
* **Details:**
    * The star rating is a direct reflection of the level's mastery progress bar. The progress is determined by the sum of all fact strengths compared to the maximum possible strength.
    * **1 Star:** Awarded for >60% mastery.
    * **2 Stars:** Awarded for >90% mastery.
    * **3 Stars:** Awarded for 100% mastery.
    * The `LevelButton` in the UI will be updated to display the 0-3 stars.

### ### Step 1.6: Connect UI to the Game Logic [DONE]

* **Action:** Link the new UI to the problem generator.
* **Details:**
    * Tapping a level button (e.g., the "8" in the Multiplication card) should start a quiz session using the smart problem generator for that specific level (`MULTIPLICATION_TABLE_8`).

---

## Phase 2: Enhancing Engagement (Speed & Rewards)

*This phase builds on the foundation by making speed a visible and rewarding part of the core learning experience.*

### ### Step 2.1: Implement "Corner Badge" Speed Tiers [DONE]

* **Action:** Add a visual indicator to the progress grid to show speed mastery.
* **Details:**
    * **Speed Tiers:** A 0-3 tier speed badge will be calculated for each fact based on the user's average response time compared to a dynamic, complexity-based threshold.
    * **"Corner Badge" UI:** Once a fact is mastered (green), a small, colored "corner badge" or "dog-ear" will appear in the cell.
        * **Bronze:** Tier 1 Speed
        * **Silver:** Tier 2 Speed
        * **Gold:** Tier 3 Speed
    * This creates a clear, two-stage goal for each fact: first accuracy (green), then speed (gold).

---

## Phase 3: Polishing and Refinement

*This phase adds smaller features that improve the overall learning experience and make the app more delightful to use.*

### ### Step 3.1 (New): Implement Graceful Error Handling for Review Questions
*   **Action:** Refine the `ExerciseProvider` logic.
*   **Details:** If a user makes a mistake on a review question from a *past* level, the system will log the mistake for future review but will **not** force the user to leave their currently selected level.

### ### Step 3.2 (New): Implement Proactive Confidence Boosters
*   **Action:** Further refine the `ExerciseProvider` logic.
*   **Details:** Implement the intra-level 80/20 split. When practicing a level, occasionally show an easy, already-mastered question from that same level to build confidence.

### ### Step 3.3 (New): Implement Reactive Repetition (Smart Repetition)
*   **Action:** Add stateful logic to the exercise session.
*   **Details:** When a user gets a problem wrong (e.g., fails $7 \times 8$), add related problems (e.g., $8 \times 7$, $56 \div 7$) to a temporary queue to be shown within the current session.

### ### Step 3.4 (Previously 3.2): Add Problem Variety
*   **Action:** Introduce different ways of asking questions.
*   **Details:**
    * Instead of just `$a + b = ?` format, add:
        * Fill-in-the-blank problems: `$a + ? = c`
        * Simple word problems: "You have 5 apples and get 3 more. How many apples in total?"

### ### Step 3.5 (Previously 3.3): Add Audio-Visual Feedback
*   **Action:** Make the app more responsive and fun.
*   **Details:**
    * Add simple, satisfying sound effects for correct and incorrect answers.
    * Create a special animation or "celebration" screen for when a user masters a level for the first time or achieves a new high score.

### ### Step 3.6 (New): Add "New Star" Celebration
*   **Action:** Add a celebration animation to the Results Screen when a user earns a new star.
*   **Details:**
    * The `ExerciseViewModel` will be updated to track the star rating before and after a session.
    * The `ResultsScreen` will display a special animation if a new star is earned.

---
# Completed Work

### Phase 1: The Core Data Layer

**Step 1: Define the `ExerciseAttempt` Data Table [DONE]**
*   **Purpose:** To create a detailed, timestamped log of every single exercise attempt for the teacher/parent view.
*   **Action:**
    *   Create a new data class `ExerciseAttempt`.
    *   This will be a Room "entity" (i.e., it will define a database table).
    *   **Columns:**
        *   `id` (auto-generating primary key)
        *   `userId` (long, foreign key to User)
        *   `timestamp` (the date and time of the attempt)
        *   `problemText` (string)
        *   `logicalOperation` (Operation enum)
        *   `correctAnswer` (int)
        *   `submittedAnswer` (int)
        *   `wasCorrect` (boolean)
        *   `durationMs` (long)
*   **Testing:** Write a Room migration test to add the new `ExerciseAttempt` table to the database.

**Step 2: Define the `FactMastery` Data Table [DONE]**
*   **Purpose:** To efficiently store the current learning state (strength and speed) of every unique fact. This table will power the `ExerciseProvider`.
*   **Action:**
    *   Create a new data class `FactMastery`.
    *   This will also be a Room entity.
    *   **Columns:**
        *   `factId` (a unique text primary key, e.g., "ADD_2_3")
        *   `userId` (long, foreign key to User)
        *   `strength` (integer from 0 to 5)
        *   `lastTestedTimestamp`
        *   `avgDurationMs` (a rolling average of correct response times)
*   **Testing:** Write a Room migration test to add the new `FactMastery` table.

**Step 3: Implement the Database Access Objects (DAOs) [DONE]**
*   **Purpose:** Create the interfaces that Room will use to generate the code for database queries.
*   **Action:**
    *   Create `UserDao`, `ExerciseAttemptDao`, and `FactMasteryDao`.
    *   Register them in `AppDatabase`.
*   **Testing:** Write unit tests for the DAOs to ensure they correctly insert and retrieve data from a test database. [DONE]

---

### Phase 2: The Learning & UI Logic

**Step 4: Define the `Curriculum` [POSTPONED]**
*   **Purpose:** Codify the mastery levels.
*   **Action:** Create a `Curriculum` object/enum that defines the levels as we've outlined.
*   **Note:** The first level for each of the four operations has been implemented. This will be revisited after the learning simulator is built to allow for better testing and validation of the full curriculum.
*   **Testing:** N/A (static data).

**Step 5: Create the `ExerciseProvider` [DONE]**
*   **Purpose:** To intelligently select the next question.
*   **Action:**
    *   Create the `ExerciseProvider` class.
    *   Its `getNextExercise()` method will query the `FactMastery` table to determine the user's progress and select a question based on the 80/20 new/review logic.
*   **Testing:** Write comprehensive **unit tests** for `ExerciseProvider`, feeding it a mocked `FactMasteryDao` with various states of progress and asserting it makes correct choices.

**Step 5a: Implement "Working Set" Algorithm" [DONE]**
*   **Purpose:** To make the learning process more engaging and less repetitive.
*   **Action:**
    *   Update the `ExerciseProvider` to use the "Working Set" method.
    *   This involves identifying a small group of weak or new facts for the user to focus on in each session.
*   **Testing:** Add new unit tests to `ExerciseProviderTest` to validate the "Working Set" logic, including scenarios for initial set creation, limiting the set size, and replacing mastered facts.

**Step 6: Integrate the New System into the `ExerciseBookViewModel` [DONE]**
*   **Purpose:** To connect the new data and logic layers to the main exercise screen.
*   **Action:**
    *   Modify `ExerciseBookViewModel` to use the `ExerciseRepository` for fetching exercises, removing the old `ExerciseBook` dependency.
    *   When an answer is submitted, it will:
        1.  Calculate the `durationMs`.
        2.  Create and insert an `ExerciseAttempt` record.
        3.  Update the `FactMastery` record for that fact with its new strength.
*   **Testing:**
    *   Update existing **UI tests** (`ExerciseFlowT`est) to work with the new provider.
    *   Add **unit tests** to the ViewModel to verify that it correctly logs history and updates mastery upon receiving an answer.

---

### Phase 3: The Progress Report & UI

**Step 7: Build the Consolidated Progress Report Screen [DONE]**
*   **Purpose:** To create a single, detailed view where users can see their progress across all operations.
*   **Action:**
    *   Created a `ProgressReportViewModel` that provides two specialized data flows: one for per-fact progress and another for per-level progress.
    *   The `ProgressReportScreen` now displays progress differently based on the operation type:
        *   **Addition/Subtraction:** A list of all curriculum levels with a progress bar for each.
        *   **Multiplication/Division:** A classic grid/matrix view showing the mastery of core facts.
*   **Testing:** Unit tests for the ViewModel were updated to verify both data flows. UI tests were updated to verify the new consolidated screen.

**Step 8: Build the Teacher/Parent History View [DONE]**
*   **Purpose:** To display the detailed log of all attempts.
*   **Action:**
    *   This could be a separate screen or a drill-down from the heatmap.
    *   Create a ViewModel that queries the `ExerciseAttempt` DAO.
    *   Display the results in a simple, scrollable list showing the date, exercise, result, and duration for each attempt.
*   **Testing:** Write a **UI test** that verifies the list displays the correct data provided by a mocked ViewModel.

**Step 9: Add Navigation [DONE]**
*   **Purpose:** To connect all the new screens.
*   **Action:** Add buttons and update the navigation graph to link the setup screen to the progress report and history views.
*   **Testing:** Update **UI tests** to confirm that clicking the navigation buttons takes the user to the correct screens.
