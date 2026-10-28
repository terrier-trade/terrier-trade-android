# Terrier Trade

Buy, sell and swap with verified BU students.

Terrier Trade is an Android app where Boston University students can list things they no longer need, find things they want, and agree on a trade through an in-app chat. A trade can include items, money, or both. Payment happens outside the app (Venmo, Zelle or cash).

> This project is in the proposal stage for CS 501 Mobile Application Development (Fall 2026). Details may change.

## Team

- Priya Bajaria
- Jerin Joseph

## Problem

Buying and selling at BU is spread across Facebook groups, Marketplace and group chats. These include strangers and scams, and there is no easy way to swap items plus cash in one deal.

## Core Features

- Sign up with a verified @bu.edu email
- Create listings with a camera photo, title, price and category
- Scan a textbook barcode to fill in the title, author and cover
- Browse and search listings, including offline
- Request an item and chat with the seller once they accept
- Trade window in the chat where both people agree on the items and the amount
- Both people confirm when the trade is done, or either can cancel

## Tech Stack

- Kotlin and Jetpack Compose
- MVVM with ViewModel and StateFlow
- Room for local storage
- Firebase (Auth, Firestore, Storage)
- Open Library API for book details
- CameraX and ML Kit for barcode scanning

## Milestones

| Date | Milestone |
|---|---|
| Oct 6 | Preliminary proposal presentation |
| Oct 20 | Proposal and initial backlog |
| Nov 10 | Project Update 1 |
| Nov 24 | Project Update 2 |
| Dec 8 | Final submission and showcase |

## Setup

Requirements: the latest stable Android Studio, its bundled JDK, and an emulator or phone on Android 8.0 (API 26) or newer.

1. In Android Studio choose **File > New > Project from Version Control** and paste `https://github.com/terrier-trade/terrier-trade-android`.
2. Wait for Gradle sync to finish. If Android Studio asks for a JDK, pick the bundled one.
3. Choose the `app` run configuration and press Run.
4. Run the unit tests with `./gradlew test` (or right click `app/src/test` and choose Run Tests).

The app currently runs on in-memory sample data, so no Firebase setup or API keys are needed yet.

## Project structure

```
app/src/main/java/edu/bu/terriertrade/
  TerrierTradeApplication.kt   creates the AppContainer when the app starts
  di/                          AppContainer (manual dependency injection) and ViewModel helpers
  data/model/                  User, Listing, Chat, Message, Deal, Review
  data/repository/             repository interfaces that ViewModels depend on
  data/fake/                   in-memory repositories and SampleData (used until Firebase and Room are added)
  data/network/                NetworkMonitor for the offline banner
  navigation/                  Routes, bottom bar, NavHost
  navigation/graphs/           one file of screens per feature area
  ui/theme/                    colors, fonts, shapes, spacing
  ui/components/               shared buttons, fields, states and the app header
  ui/common/                   UiState and UiStateContent
  ui/screens/                  one package per screen
  util/                        price and time formatting
```

## Building a feature

1. Branch from `main`: `git checkout main && git pull && git checkout -b feature/<name>`.
2. Create the screen and its ViewModel in `ui/screens/<name>/`. The ViewModel takes repositories in its constructor and gets them through `appContainer` in its factory (see `di/ViewModelSupport.kt`).
3. Replace the `PlaceholderScreen` for your route in the matching file in `navigation/graphs/`.
4. When the real data source is ready, swap the fake for it in `di/AppContainer.kt`. Only that one line changes.
5. Add tests, open a pull request into `main`, and ask the other person to review it.

| Area | Owner | Files |
|---|---|---|
| Sign up, sign in, verify | Priya | `navigation/graphs/AuthScreens.kt`, `ui/screens/auth/` |
| Browse, post, listing detail, search | Priya | `navigation/graphs/ListingScreens.kt`, `ui/screens/feed/`, `ui/screens/post/` |
| Profiles and saved listings | Priya | `navigation/graphs/ProfileScreens.kt`, `ui/screens/profile/` |
| Chats, deal, reviews, notifications | Jerin | `navigation/graphs/ChatScreens.kt`, `ui/screens/chats/` |
