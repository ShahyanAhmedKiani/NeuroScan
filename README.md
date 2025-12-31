# NeuroScan - AI-Powered Brain Tumor Detection & Brain Training App

## Overview

NeuroScan is a comprehensive Android application designed to serve as a supportive tool in the early detection of brain tumors and to promote cognitive health through engaging brain games. The app leverages a TensorFlow Lite model to analyze brain MRI scans and provides users with a platform to manage their profile, track their scan history, and challenge their memory skills.

This project was developed by **Shahyan Ahmed Kiani**.

---

## Features

### Core Medical Features
- **AI-Powered Tumor Detection**: Users can either capture a new brain MRI image using their camera or select an existing one from their gallery. The app uses a built-in TensorFlow Lite model to analyze the image and detect potential signs of Glioma, Meningioma, or Pituitary tumors.
- **Scan History**: Every scan and its corresponding result are saved to the user's private account in the Firebase Realtime Database. This allows users to track their scan history over time.
- **Detailed PDF Reports**: For each scan in the history, users can download a professional, detailed PDF report. The report includes the scan image, detection results, a timestamp, and general information about the detected tumor type.

### User & Profile Management
- **User Authentication**: Secure user registration and login functionality using Firebase Authentication, complete with email verification.
- **Persistent Login**: User session data is cached locally using `SharedPreferences`, so users remain logged in even after closing the app.
- **Profile Management**: Users can view and edit their profile information, including their name, date of birth, and country.
- **Customizable Profile Picture**: Users can upload and update their profile picture, which is stored as a Base64 string in the database for a lightweight solution.

### Engagement & Gamification
- **Brain Game (Memory Matrix)**: A classic 4x4 memory matching game designed to challenge and improve cognitive function.
- **Daily Streak Tracker**: The app tracks the user's daily streak for playing the brain game, encouraging consistent engagement.
- **Best Score Record**: The game saves the user's best score (lowest number of moves) to complete the game, providing a personal record to beat.
- **Streak Notifications**: To help maintain the daily streak, the app schedules a notification to remind the user to play if they haven't for half a day.

### Modern UI/UX
- **Consistent Theming**: The app features a modern, dark-themed UI with a consistent color scheme and Material Design components across all screens.
- **User-Friendly Inputs**: Custom date pickers and searchable country dropdowns are used in forms to enhance user experience and ensure data accuracy.
- **Full-Screen Image Viewer**: Images in the scan history can be tapped to be viewed in a full-screen, immersive mode.

---

## Technologies Used

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI**: Android XML with Material Design 3 Components
- **Backend & Database**: Firebase (Realtime Database for user data and scan history, Firebase Authentication for user management)
- **Machine Learning**: TensorFlow Lite for on-device model inference.
- **Asynchronous Programming**: Kotlin Coroutines for background tasks.
- **Local Storage**: `SharedPreferences` for caching user session data, game streaks, and best scores.
- **Image Handling**: [Glide](https://github.com/bumptech/glide) for efficient image loading and Base64 encoding for image storage.

---

## Setup and Installation

To set up and run the NeuroScan project on your local machine, follow these steps:

1.  **Clone the Repository**:
    ```sh
    git clone <your-repository-url>
    ```

2.  **Firebase Configuration**:
    - Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
    - Add an Android app to your Firebase project with the package name `com.example.neuroscan`.
    - Download the `google-services.json` file provided during the setup process and place it in the `D:/NeuroScan/app/` directory.
    - In the Firebase Console, enable **Authentication** (with the Email/Password provider) and the **Realtime Database**.

3.  **Build and Run**:
    - Open the project in Android Studio.
    - Let Gradle sync and download all the required dependencies.
    - Run the app on an Android emulator or a physical device.

