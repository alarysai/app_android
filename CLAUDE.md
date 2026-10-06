# Claude Code Project Instructions

You are working on the alarysai Android app (`com.alarysai.alarysai`), with strict engineering standards.

The app reads content straight from Cloud Firestore (project `alarysai-b6e85`). The data contract lives in `web_admin/docs/android-integration.md` and `web_admin/docs/data-model.md`: follow its queries exactly (status filter + documented indexes). See `README.md`.

All code generation, refactoring, fixes, and feature implementation must follow the rules below.

---

## Core Stack

- Kotlin
- Jetpack Compose
- MVVM
- Coroutines
- Flow / StateFlow
- Hilt for dependency injection
- Navigation Compose

---

## Architecture Rules

Use **MVVM with pragmatic Clean Architecture**.

The codebase is organized by:
- feature modules
- shared core modules
- clear layer separation: `presentation`, `domain`, `data`

### Layer responsibilities

#### Presentation
Contains:
- Compose screens
- reusable UI for the feature
- ViewModels
- UiState
- UiAction
- UiEvent / UiEffect
- UI-only mapping and rendering logic

Must not contain:
- business rules
- repository implementation
- direct infrastructure concerns

#### Domain
Contains:
- use cases
- domain models
- repository contracts/interfaces
- business rules

Must not depend on:
- Android framework UI details
- data implementation details

#### Data
Contains:
- repository implementations
- remote/local data sources
- DTOs
- persistence models
- mappers
- API/database integrations

Must not contain:
- UI logic
- screen state logic

---

## Modularization Rules

Use modularization by feature and shared responsibility.

### Main module
- `app`

### Shared modules
- `core:common`
- `core:designsystem`
- `core:ui`
- `core:navigation`
- `core:firebase` (Firestore access shared by every content feature)
- `core:testing`

Add `core:network` or `core:database` only when a feature really needs them.

### Feature modules
- `feature:home`
- `feature:questionnaires`
- `feature:tips`
- `feature:advertisers`
- `feature:history`
- `feature:auth`
- `feature:plans`
- `feature:splash`

### Dependency rules
- Presentation depends on Domain when needed.
- Data depends on Domain contracts when needed.
- Domain must not depend on Data or Presentation implementation.
- Features must not depend on internal details of other features.
- Shared reusable logic belongs in core modules when justified.

Avoid cyclic dependencies.

---

## Standard Feature Structure

Each feature should follow this structure when applicable:

feature:<name>
- presentation
  - screen
  - components
  - state
  - action
  - event
  - viewmodel
- domain
  - model
  - repository
  - usecase
- data
  - repository
  - remote
  - local
  - mapper
  - model

Keep structure clear and responsibilities explicit.

---

## MVVM Standards

Each screen or relevant flow should define:

- Screen
- ViewModel
- UiState
- UiAction
- UiEvent or UiEffect when needed

### State rules
- Use `StateFlow` for UI state.
- Expose immutable state from ViewModel.
- Handle loading, success, empty, and error states explicitly.
- Use one predictable source of truth for screen state.

### Interaction rules
- `UiAction` represents user intent.
- `UiEvent` or `UiEffect` represents one-time effects like navigation or snackbar.

---

## Compose Rules

- Keep composables small and focused.
- Prefer stateless composables where possible.
- Hoist state when appropriate.
- Separate screen-level UI from reusable components.
- Add previews when useful.
- Do not place business logic inside composables.
- Do not create overly large composables.
- Avoid duplicated UI logic.

---

## Clean Code Rules

Mandatory:
- clear naming
- small focused functions
- single responsibility
- readable code over clever code
- avoid god classes
- avoid large files when possible
- prefer composition over inheritance
- remove dead code
- avoid duplication
- keep control flow explicit

Avoid vague names like:
- Manager
- Handler
- Utils
- Helper

unless truly justified.

---

## Clean Architecture Rules

Apply Clean Architecture pragmatically.

Required:
- business rules go to domain/use cases when meaningful
- repositories are defined as contracts when appropriate
- data layer implements repository contracts
- model mapping is explicit when needed

Avoid:
- unnecessary use cases for trivial pass-through code
- unnecessary abstraction
- overengineering
- modularization without real benefit

---

## Testing Rules

Testing is mandatory.

Every new feature must include tests.
Every meaningful change must add or update tests.

At minimum, include when applicable:
- ViewModel unit tests
- UseCase unit tests
- Repository tests for non-trivial logic
- Compose UI tests for critical flows

Test relevant scenarios:
- success
- loading
- error
- empty state
- retry
- edge cases
- validation

Testing principles:
- prefer deterministic tests
- avoid flaky tests
- test behavior, not implementation details
- prioritize meaningful coverage over vanity coverage

---

## Documentation Rules

Documentation is mandatory for everything created, changed, refactored, or removed.

No task is complete without documentation updates.

Whenever any code changes, update documentation when applicable:
- feature behavior
- architecture
- module responsibility
- API/data contract
- navigation
- setup/configuration
- business rules
- testing notes
- important technical decisions

Documentation must be:
- clear
- objective
- current
- consistent with the code
- easy for another developer to understand

Do not postpone documentation.

---

## Delivery Rules

Every delivered task must include:
- implementation
- tests
- documentation updates

For each task, clearly state:
- what was implemented or changed
- why
- affected modules
- tests added or updated
- documentation added or updated
- limitations if any

---

## Definition of Done

A task is only done when:
- code is implemented
- architecture rules were followed
- tests were added or updated
- documentation was added or updated
- responsibilities are clear
- code is maintainable

If tests are missing, it is not done.
If documentation is missing, it is not done.
If architecture was bypassed, it is not done.

---

## Instructions for Every Change

For every request:
1. follow the existing architecture
2. keep module boundaries clear
3. use MVVM consistently
4. keep business logic out of UI
5. add or update tests
6. add or update documentation
7. avoid unnecessary complexity
8. keep the implementation explicit and maintainable

Do not invent a different project structure unless explicitly requested.
