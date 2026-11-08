# Terrier Trade

Buy, sell and swap with verified BU students.

Terrier Trade is an Android app where Boston University students can list things they no longer need, find things they want, and agree on a trade through an in-app chat. A trade can include items, money, or both. Payment happens outside the app (Venmo, Zelle or cash).

> Final project for CS 501 Mobile Application Development (Fall 2026).

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
- Request an item, which opens a chat with the seller and starts a deal
- Trade window in the chat where both people agree on the items and the amount (in progress)
- Both people confirm when the trade is done, or either can cancel (in progress)

## Tech Stack

- Kotlin and Jetpack Compose, Navigation Compose
- MVVM: ViewModels expose UI state, screens are stateless, repositories hide where data comes from
- Firebase Auth (verified @bu.edu email) and Cloud Firestore (listings, profiles, chats, deals)
- Open Library API through Retrofit, for book details from an ISBN
- CameraX and ML Kit for barcode scanning
- Coil for images
- Planned: Room for an offline cache, Firebase Storage for listing photos

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
2. Ask a teammate for `google-services.json` and put it in the `app` folder. It is gitignored and must never be committed.
3. Wait for Gradle sync to finish. If Android Studio asks for a JDK, pick the bundled one.
4. Choose the `app` run configuration and press Run.
5. Sign up with a @bu.edu email and open the verification link. Firestore only lets verified BU accounts read or write (see `firestore.rules`).

Useful Gradle commands (use `./gradlew` on macOS and Linux):

| Command | What it does |
|---|---|
| `.\gradlew testDebugUnitTest` | Runs every unit test |
| `.\gradlew assembleDebug` | Builds the debug APK |
| `.\gradlew installDebug` | Builds and installs on the running emulator or phone |

## Project structure

```
app/src/main/java/edu/bu/terriertrade/
  MainActivity.kt, TerrierTradeApplication.kt   app entry point, creates the AppContainer
  di/                      AppContainer (manual dependency injection) and ViewModel factory helpers
  data/
    model/                 User, Listing, Chat, Message, Deal, Review, BookInfo
    repository/            interfaces the ViewModels depend on, plus DataError and observeUsers
    firebase/              Firebase Auth and Firestore versions of the repositories, and the field mappers
    openlibrary/           Open Library book lookup
    deal/                  deal rules: what a request does (RequestRules) and the money total (DealMath)
    fake/                  in-memory repositories and SampleData, for tests and previews
    network/               NetworkMonitor for the offline banner
  navigation/              Routes, NavHost, bottom bar, sign-in redirects
    graphs/                one file of screens per feature area
  ui/
    TerrierTradeApp.kt     app frame: header, bottom bar, offline banner, NavHost
    theme/                 colors, fonts, shapes, spacing
    common/                UiState (Loading / Success / Error) and UiStateContent
    model/                 display-ready models (ListingUi, ProfileHeaderUi) and their mappers
    components/            shared TT components, grouped by purpose:
                           buttons, inputs, selection, feedback, layout, listing, profile
    preview/               PreviewData and TTPreviewColumn, used only by @Preview functions
    screens/               one package per screen: auth, browse, listingdetail, newlisting,
                           chats, chat, profile, placeholder
  util/                    email, password and ISBN checks, price and time formatting

app/src/test/java/edu/bu/terriertrade/
  testing/                 shared test doubles (TestAuthRepository, InMemoryRepositories, ...)
                           and MainDispatcherRule
  (everything else)        mirrors the main packages: a test lives in the package of the code it tests
```

### How a screen is built

Each screen package has the same parts:

- `XUiState.kt`: a data class with everything the screen shows.
- `XViewModel.kt`: takes repositories in its constructor, exposes the UI state, and has one function per user action. Its `Factory` gets the repositories from `appContainer`.
- `XScreen.kt`: an `XRoute` composable that owns the ViewModel and handles navigation, and a stateless `XScreen` that only draws the state. Previews use the stateless one with `PreviewData`.

## Building a feature

1. Branch from `main`: `git checkout main && git pull && git checkout -b feature/<name>`.
2. Create the screen, its UI state and its ViewModel in `ui/screens/<name>/` (see "How a screen is built").
3. Add the route to `navigation/Routes.kt` and the screen to the matching file in `navigation/graphs/`. Routes still waiting for their screen show `PlaceholderScreen`.
4. If the screen needs new data, add a function to the repository interface in `data/repository/`, then implement it in both the Firestore version (`data/firebase/`) and the fake (`data/fake/`).
5. Put reusable UI in `ui/components/<group>/` with a `TT` prefix. Anything used by one screen stays in that screen's package.
6. Add unit tests for the ViewModel and any rules or mappers. Shared test doubles go in `testing/`.
7. Open a pull request into `main` and ask the other person to review it. Merge with a merge commit, not squash.

| Area | Owner | Files |
|---|---|---|
| Sign up, sign in, verify | Priya | `navigation/graphs/AuthScreens.kt`, `ui/screens/auth/` |
| Browse, new listing, listing detail, ISBN scan | Priya | `navigation/graphs/ListingScreens.kt`, `ui/screens/browse/`, `ui/screens/newlisting/`, `ui/screens/listingdetail/`, `data/openlibrary/` |
| Profiles and saved listings | Priya | `navigation/graphs/ProfileScreens.kt`, `ui/screens/profile/` |
| Chats, requests, deals, reviews, notifications | Jerin | `navigation/graphs/ChatScreens.kt`, `ui/screens/chats/`, `ui/screens/chat/`, `data/deal/` |
