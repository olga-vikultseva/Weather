# Weather

[![Kotlin Version](https://img.shields.io/badge/kotlin-1.4.10-blue.svg)](https://kotlinlang.org)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
![](https://img.shields.io/github/languages/count/olga-vikultseva/Weather.svg)
![](https://img.shields.io/github/repo-size/olga-vikultseva/Weather.svg)
![](https://img.shields.io/github/last-commit/olga-vikultseva/Weather.svg)

### Features

Application for displaying the weather forecast for the current day and the next week anywhere in the world.

The application is written in Kotlin and designed according to the MVVM architecture pattern using Android Architecture Components.

The application interacts with two services to obtain data on available locations and weather.

- https://openweathermap.org/
- https://opencagedata.com/

Asynchronous requests are made using the Retrofit library and Coroutines. Dagger2 is used for dependency injection. Navigation between application screens is implemented using the Navigation Component.

### Architecture

The application architecture is designed in accordance with the following points:

-   [Android Architecture Components](https://developer.android.com/topic/libraries/architecture/), part of Android Jetpack for give to project a robust design, testable and maintainable.
-   Pattern [Model-View-ViewModel](https://en.wikipedia.org/wiki/Model%E2%80%93view%E2%80%93viewmodel) (MVVM) facilitating a [separation](https://en.wikipedia.org/wiki/Separation_of_concerns) of development of the graphical user interface.
-   [S.O.L.I.D](https://en.wikipedia.org/wiki/SOLID) design principles intended to make software designs more understandable, flexible and maintainable.

### Dependencies

-   [Jetpack](https://developer.android.com/jetpack):
    -   [Android KTX](https://developer.android.com/kotlin/ktx.html) - provide concise, idiomatic Kotlin to Jetpack and Android platform APIs.
    -   [AndroidX](https://developer.android.com/jetpack/androidx) - major improvement to the original Android [Support Library](https://developer.android.com/topic/libraries/support-library/index), which is no longer maintained.
    -   [Lifecycle](https://developer.android.com/topic/libraries/architecture/lifecycle) - perform actions in response to a change in the lifecycle status of another component, such as activities and fragments.
    -   [LiveData](https://developer.android.com/topic/libraries/architecture/livedata) - lifecycle-aware, meaning it respects the lifecycle of other app components, such as activities, fragments, or services.
    -   [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) - designed to store and manage UI-related data in a lifecycle conscious way. The ViewModel class allows data to survive configuration changes such as screen rotations.
-   [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html) - managing background threads with simplified code and reducing needs for callbacks.
-   [Dagger2](https://dagger.dev/) - for [dependency injection](https://developer.android.com/training/dependency-injection).
-   [Retrofit2](https://square.github.io/retrofit/) - type-safe HTTP client.
-   [Navigation Component](https://developer.android.com/guide/navigation/navigation-getting-started/) - handle everything needed for in-app navigation.
-   [Room](https://developer.android.com/training/data-storage/room/) - access your app's SQLite database with in-app objects and compile-time checks.
-   [Glide](https://github.com/bumptech/glide) - image loading library for Android.
-   [Gson](https://github.com/google/gson) - makes it easy to parse JSON into Kotlin objects.
