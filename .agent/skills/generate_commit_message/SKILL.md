---
name: Generate Commit Message
description: Generates a standard git commit message following the Conventional Commits specification.
---

# Generate Commit Message

This skill assists in creating clear, standardized commit messages based on staged changes.

## 1. Analyze Changes

Run `git status` and `git diff --cached` (if available) or review the changes made to files.
Identify:

- **Type**: What kind of change is this?
- **Scope**: What part of the app is affected? (ui, data, widget, build, etc.)
- **Description**: A short, imperative summary.

## 2. Conventional Commits Format

**Structure:**

```
<type>(<scope>): <subject>

[optional body]

[optional footer(s)]
```

### Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation only changes
- **style**: Changes that do not affect the meaning of the code (white-space, formatting, etc)
- **refactor**: A code change that neither fixes a bug nor adds a feature
- **perf**: A code change that improves performance
- **test**: Adding missing tests or correcting existing tests
- **chore**: Changes to the build process or auxiliary tools (e.g., gradle, manifest)

### Scope (Optional)

Examples: `auth`, `home`, `widget`, `database`, `deps`.

## 3. Examples

- **New Feature:** `feat(widget): add home screen widget layout`
- **Bug Fix:** `fix(viewmodel): resolve context injection crash`
- **Refactor:** `refactor(repo): move data fetching to background thread`
- **Build Update:** `chore(deps): update kotlin version to 1.9.0`

## 4. Execution

Once the message is formulated, use it in the `git commit` command:
`git commit -m "feat(scope): your message"`
