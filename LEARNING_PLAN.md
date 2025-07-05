# Adaptive Learning Implementation Plan

This document outlines the step-by-step plan to implement the adaptive learning features described in `LEARNING_MODEL.md`.

---

### Phase 1: The Core Data Layer

**Step 1: Define the `ExerciseHistory` Data Table**
*   **Purpose:** To create a detailed, timestamped log of every single exercise attempt for the teacher/parent view.
*   **Action:**
    *   Create a new data class `ExerciseHistory`.
    *   This will be a Room "entity" (i.e., it will define a database table).
    *   **Columns:**
        *   `id` (auto-generating primary key)
        *   `timestamp` (the date and time of the attempt)
        *   `operation` (e.g., "ADDITION")
        *   `operand1`, `operand2`
        *   `was_correct` (boolean)
        *   `duration_ms` (long - the time in milliseconds it took to answer)
*   **Testing:** Write a Room migration test to add the new `ExerciseHistory` table to the database.

**Step 2: Define the `FactMastery` Data Table**
*   **Purpose:** To efficiently store the current learning state (strength) of every unique fact. This table will power the `ExerciseProvider`.
*   **Action:**
    *   Create a new data class `FactMastery`.
    *   This will also be a Room entity.
    *   **Columns:**
        *   `fact_id` (a unique text primary key, e.g., "ADD_2_3" for 2+3)
        *   `strength` (integer from 1 to 5)
        *   `last_tested_timestamp`
*   **Testing:** Write a Room migration test to add the new `FactMastery` table.

**Step 3: Implement the Database Access Objects (DAOs)**
*   **Purpose:** Create the interfaces that Room will use to generate the code for database queries.
*   **Action:**
    *   Create `ExerciseHistoryDao` with a method to `insert(history: ExerciseHistory)`.
    *   Create `FactMasteryDao` with methods to `getFact(factId: String)` and `upsertFact(mastery: FactMastery)` (upsert means insert or update).
*   **Testing:** Write unit tests for the DAOs to ensure they correctly insert and retrieve data from a test database.

---

### Phase 2: The Learning & UI Logic

**Step 4: Define the `Curriculum`**
*   **Purpose:** Codify the mastery levels.
*   **Action:** Create a `Curriculum` object/enum that defines the levels as we've outlined.
*   **Testing:** N/A (static data).

**Step 5: Create the `ExerciseProvider`**
*   **Purpose:** To intelligently select the next question.
*   **Action:**
    *   Create the `ExerciseProvider` class.
    *   Its `getNextExercise()` method will now query the `FactMastery` table to determine the user's progress and select a question based on the 80/20 new/review logic.
*   **Testing:** Write comprehensive **unit tests** for `ExerciseProvider`, feeding it a mocked `FactMasteryDao` with various states of progress and asserting it makes correct choices.

**Step 6: Integrate the New System into the `ExerciseBookViewModel`**
*   **Purpose:** To connect the new data and logic layers to the main exercise screen.
*   **Action:**
    *   Modify `ExerciseBookViewModel` to use the `ExerciseProvider`.
    *   When an answer is submitted, it will:
        1.  Calculate the `duration_ms`.
        2.  Create and insert an `ExerciseHistory` record.
        3.  Update the `FactMastery` record for that fact with its new strength.
*   **Testing:**
    *   Update existing **UI tests** (`ExerciseFlowTest`) to work with the new provider.
    *   Add **unit tests** to the ViewModel to verify that it correctly logs history and updates mastery upon receiving an answer.

---

### Phase 3: The Progress Report & UI

**Step 7: Display Progress on Exercise Setup Screen**
*   **Purpose:** To give the user a quick visual of their progress.
*   **Action:**
    *   Update the setup screen's ViewModel to query the `FactMastery` DAO and calculate the overall mastery percentage.
    *   Implement the progress bar and "Mastered!" badge UI.
*   **Testing:** Write a **UI test** to verify the progress bar and badge display correctly based on mocked data from the ViewModel.

**Step 8: Build the Progress Report Screen (Heatmap)**
*   **Purpose:** To create the detailed heatmap view.
*   **Action:**
    *   Create a `ProgressReportViewModel` that queries the `FactMastery` DAO.
    *   Create the `ProgressReportScreen` Composable to display the heatmap grid.
*   **Testing:** Write a **UI test** for the screen, verifying that the grid cells are displayed with the correct color/status based on mocked mastery data.

**Step 9: Build the Teacher/Parent History View**
*   **Purpose:** To display the detailed log of all attempts.
*   **Action:**
    *   This could be a separate screen or a drill-down from the heatmap.
    *   Create a ViewModel that queries the `ExerciseHistory` DAO.
    *   Display the results in a simple, scrollable list showing the date, exercise, result, and duration for each attempt.
*   **Testing:** Write a **UI test** that verifies the list displays the correct data provided by a mocked ViewModel.

**Step 10: Add Navigation**
*   **Purpose:** To connect all the new screens.
*   **Action:** Add buttons and update the navigation graph to link the setup screen to the progress report and history views.
*   **Testing:** Update **UI tests** to confirm that clicking the navigation buttons takes the user to the correct screens.
