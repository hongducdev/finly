---
name: Create Room Migration
description: Guides the safe creation of Room database migrations in Android, including version bumps, migration scripts, and schema validation.
---

# Create Room Database Migration

This skill helps you migrate your Room database schema safely without losing user data.

## 1. Analyze Changes

Identify what changed in your `@Entity` classes:

- **New Table?** -> Need `CREATE TABLE`
- **New Column?** -> Need `ALTER TABLE ... ADD COLUMN`
- **Column Renamed?** -> Need `ALTER TABLE ... RENAME TO` (SQLite doesn't support direct rename easily, may need temp table)
- **Column Type Changed?** -> Complex migration (Temp table strategy)

## 2. Implementation Steps

### Step 2.1: Update Database Version

Open your `AppDatabase` (or equivalent `@Database` class):

1.  Increment `version` number by 1.
2.  Add the new `Migration` object to the `migrations` array/list being passed to the builder.

### Step 2.2: Define Migration

Create a `Migration` variable (usually in `AppDatabase` companion object or a separate file).

**Template:**

```kotlin
val MIGRATION_[Old]_[New] = object : Migration([Old], [New]) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add 'age' column to 'users' table
        database.execSQL("ALTER TABLE users ADD COLUMN age INTEGER NOT NULL DEFAULT 0")

        // Example: Create new table 'orders'
        // database.execSQL("CREATE TABLE IF NOT EXISTS `orders` (...)")
    }
}
```

### Step 2.3: Register Migration

In your database builder (usually in `DatabaseModule` or `AppDatabase.kt`):

```kotlin
Room.databaseBuilder(...)
    .addMigrations(MIGRATION_[Old]_[New]) // <--- Add this
    .build()
```

## 3. Common SQL Commands (SQLite)

- **Add Column:**
  `ALTER TABLE [table] ADD COLUMN [column] [type] [constraints]`
  _Example:_ `ALTER TABLE transactions ADD COLUMN category_id INTEGER`

- **Rename Table:**
  `ALTER TABLE [old_name] RENAME TO [new_name]`

## 4. Verification

- **Run App:** standard launch.
- **Check Logcat:** Watch for `Room` tag or crashes related to `IllegalStateException` (Schema mismatch).
- **Export Schema:** If configured, check the generated JSON schema in `schemas/`.
