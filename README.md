# MMCM Curriculum Tracker (Kotlin Mobile App)

This mobile app helps students and admins at Mapúa Malayan Colleges Mindanao (MMCM) track curriculum progress and determine course eligibility based on prerequisites, co-requisites, and regular term offerings. Built using **Kotlin + Jetpack Compose**, it integrates with **Firebase Firestore** and implements a **DAG-based course unlocking system**.

---

## Features

### Student Users
- Browse curriculum checklist
- See completed courses
- Automatically view eligible next courses based on completed coures
- See color-coded term availability:
  - Green: offered this term
  - Orange: not offered this term
- Account and profile management

### Admin Users
- Access student list
- Mark student progress (Curriculum Overview)
- Calculate next available courses per student
- View developer information and availability legend

---

## Technologies Used

- **Jetpack Compose** for UI
- **Kotlin** as the primary language
- **Firebase Firestore** for real-time database
- **MVVM** architecture using ViewModels
- **Topological Sorting** (Kahn’s algorithm) for dependency-based course checking

---

## Project Structure

```

com.mamogkat.mmcmcurriculumtracker/
├── models/                        # Data models
├── navigation/                   # Navigation graph and routes
│   └── Navigation.kt
├── repository/                   # Firestore abstraction
│   └── FirebaseRepository.kt
├── ui/screens/
│   ├── admin/
│   │   ├── AdminCurriculumOverviewScreen.kt
│   │   ├── AdminHomeScreen.kt
│   │   ├── AdminNextCoursesScreen.kt
│   │   ├── AdminStudentListScreen.kt
│   │   ├── AdminViewStudentProgressScreen.kt
│   │   ├── ManageCurriculumsScreen.kt
│   │   ├── AdminSettingsScreen.kt
│   │   └── AdminPanelScaffold.kt
│   ├── auth/                     # Login and registration
│   │   ├── LoginScreen.kt
│   │   ├── RegisterScreen.kt
│   │   ├── ForgotPassword.kt
│   │   └── ...
│   └── student/                  # Student-facing screens
│       ├── HomeScreen.kt
│       ├── CurriculumOverviewScreen.kt
│       ├── NextCoursesScreen.kt
│       ├── UserAccountScreen.kt
│       └── ...
├── studentscreens/
│   └── StudentMain.kt
├── theme/                        # Theming and styles
├── viewmodel/
│   ├── AuthViewModel.kt
│   ├── CurriculumViewModel.kt
│   ├── AdminViewModel.kt
│   ├── StudentViewModel.kt
│   └── TimerViewModel.kt
├── MainActivity.kt               # App entry point

````

---

## How Course Eligibility Works

- Courses are stored as documents with fields like prerequisites, corequisites, and term offerings.
- The app constructs a **Directed Acyclic Graph (DAG)** based on these relationships.
- Using **Kahn’s Algorithm**, it identifies all courses a student is now eligible to take.

---

## Sample Course Format (Firestore)

```kotlin
mapOf(
    "code" to "CPE103-4",
    "name" to "Microprocessors",
    "units" to 3,
    "prerequisites" to listOf("CPE101-1"),
    "coRequisites" to listOf(),
    "regularTerms" to listOf(1, 2),
    "term" to 1,
    "yearLevel" to 3
)
````

---

## Getting Started

1. Clone this repository.
2. Open it in Android Studio.
3. Connect your Firebase project and place your `google-services.json` file.
4. Sync Gradle and run the app.

---

## Future Plans

* Editable curriculum management by admins
* Exportable student reports

---

## Developed by

**Mohammad Jameel Jibreel N. Mamogkat**  
Bachelor of Science in Computer Engineering – MMCM

**Duff S. Bastasa**  
Bachelor of Science in Computer Engineering – MMCM


---

```

