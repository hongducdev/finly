---
name: Scaffold New Feature
description: Generates the necessary boilerplate code for a new feature in the Finly app, following MVVM Clean Architecture and Jetpack Compose.
---

# Scaffold New Feature

This skill guides the creation of a new feature in the Finly Android app. It ensures consistency with the existing Clean Architecture + MVVM + Jetpack Compose setup.

## 1. Feature Analysis

First, understand the feature requirements. Identify:

- **Feature Name**: e.g., `Budget`, `Statistics`, `UserProfile`.
- **Data Needed**: e.g., `BudgetEntity`, `UserPreferences`.
- **UI Screens**: e.g., `BudgetScreen`, `EditBudgetScreen`.

## 2. Directory Structure

Create/Verify the following package structure for the new feature (replace `[feature]` with the feature name in lowercase):

```
app/src/main/java/com/finly/
├── data/
│   ├── local/
│   │   ├── entity/       <-- Entity classes (Room)
│   │   └── [Feature]Dao.kt
│   └── repository/
│       └── [Feature]Repository.kt
├── ui/
│   ├── screens/
│   │   └── [Feature]Screen.kt
│   └── viewmodel/
│       └── [Feature]ViewModel.kt
```

## 3. Implementation Steps

### Step 3.1: Data Layer (Entity & DAO)

If the feature requires database storage:

- Create `@Entity` data class.
- Create `@Dao` interface with suspended methods and Flow return types.

**Example `[Feature]Dao.kt`:**

```kotlin
@Dao
interface [Feature]Dao {
    @Query("SELECT * FROM [table_name]")
    fun getAll(): Flow<List<[Entity]>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: [Entity])
}
```

### Step 3.2: Repository

Create a Singleton Repository to abstract data access.
**Example `[Feature]Repository.kt`:**

```kotlin
@Singleton
class [Feature]Repository @Inject constructor(
    private val dao: [Feature]Dao
) {
    fun getData() = dao.getAll()
    suspend fun saveData(item: [Entity]) = dao.insert(item)
}
```

### Step 3.3: ViewModel

Create a Hilt ViewModel to handle business logic and UI state.
**Example `[Feature]ViewModel.kt`:**

```kotlin
@HiltViewModel
class [Feature]ViewModel @Inject constructor(
    private val repository: [Feature]Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow([Feature]UiState())
    val uiState = _uiState.asStateFlow()

    // ... logic methods
}

data class [Feature]UiState(
    val isLoading: Boolean = false,
    val data: List<[Entity]> = emptyList()
)
```

### Step 3.4: UI Screen (Compose)

Create the Composable screen.
**Example `[Feature]Screen.kt`:**

```kotlin
@Composable
fun [Feature]Screen(
    navController: NavController,
    viewModel: [Feature]ViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(...) { padding ->
        // UI Content
    }
}
```

### Step 3.5: Navigation Registration

Update `MainActivity.kt` (or `FinlyNavHost` if separated) to include the new route.

```kotlin
composable("feature_route") {
    [Feature]Screen(navController = navController)
}
```

## 4. Final Review

- Check imports (no unused imports).
- Check for hardcoded strings (extract to `strings.xml`).
- Run build to verify Hilt dependency injection.
