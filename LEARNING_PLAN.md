# Adaptive Learning Implementation Plan

This document outlines the step-by-step plan to implement the adaptive learning features described in `LEARNING_MODEL.md`.

---

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
*   **Purpose:** To efficiently store the current learning state (strength) of every unique fact. This table will power the `ExerciseProvider`.
*   **Action:**
    *   Create a new data class `FactMastery`.
    *   This will also be a Room entity.
    *   **Columns:**
        *   `factId` (a unique text primary key, e.g., "ADD_2_3")
        *   `userId` (long, foreign key to User)
        *   `strength` (integer from 1 to 5)
        *   `lastTestedTimestamp`
*   **Testing:** Write a Room migration test to add the new `FactMastery` table.

**Step 3: Implement the Database Access Objects (DAOs) [DONE]**
*   **Purpose:** Create the interfaces that Room will use to generate the code for database queries.
*   **Action:**
    *   Create `UserDao`, `ExerciseAttemptDao`, and `FactMasteryDao`.
    *   Register them in `AppDatabase`.
*   **Testing:** Write unit tests for the DAOs to ensure they correctly insert and retrieve data from a test database. [DONE]

---

### Phase 2: The Learning & UI Logic

**Step 4: Define the `Curriculum` [IN PROGRESS]**
*   **Purpose:** Codify the mastery levels.
*   **Action:** Create a `Curriculum` object/enum that defines the levels as we've outlined.
*   **Note:** The first level for each of the four operations (Addition, Subtraction, Multiplication, Division) has been implemented. The curriculum still needs to be expanded to include all levels defined in `LEARNING_MODEL.md`.
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
    *   Create a ViewModel that queries the `ExerciseAttempt` DAO.
    *   Display the results in a simple, scrollable list showing the date, exercise, result, and duration for each attempt.
*   **Testing:** Write a **UI test** that verifies the list displays the correct data provided by a mocked ViewModel.

**Step 10: Add Navigation**
*   **Purpose:** To connect all the new screens.
*   **Action:** Add buttons and update the navigation graph to link the setup screen to the progress report and history views.
*   **Testing:** Update **UI tests** to confirm that clicking the navigation buttons takes the user to the correct screens.

---

### Phase 4: Algorithm Tuning & Evaluation

**Step 11: Build a Learning Simulator**
*   **Purpose:** To rapidly test and evaluate changes to the learning algorithm without needing real user data.
*   **Action:**
    *   Create a simulation environment that models a virtual student's learning process.
    *   The simulator will interact with the `ExerciseProvider` and have configurable parameters (e.g., probability of making a mistake on a new vs. known fact, learning speed).
    *   Develop scripts or a simple dashboard to visualize the simulated learning progress, identify how the algorithm adapts, and analyze the effectiveness of the "Working Set" and "Spaced Repetition" logic under different conditions.
*   **Testing:** N/A (internal development tool).
