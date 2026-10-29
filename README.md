# WatchApp

Aplicație Android care afișează trei ceasuri analogice, unul lângă altul, pentru fusuri orare diferite:

| Ceas | Fus orar | Cadran | Limba zilei | Format dată |
|------|----------|--------|-------------|-------------|
| România | `Europe/Bucharest` | tricolorul României | română | `dd.MM.yyyy` |
| India | `Asia/Kolkata` | tricolorul Indiei cu roata Ashoka | hindi | `dd/MM/yyyy` |
| Global (UTC) | `UTC` | glob cu meridiane și paralele | engleză | `yyyy-MM-dd` |

Sub fiecare ceas se afișează ora digitală (`HH:mm:ss`), ziua săptămânii și data.

## Funcționalități

- Ceasuri analogice desenate de la zero pe `Canvas`, cu limbă pentru secunde actualizată în fiecare secundă
- Fiecare ceas folosește propriul fus orar, independent de setările telefonului
- Ziua săptămânii este afișată în limba țării respective, nu în limba telefonului
- Mod imersiv: bara de stare și bara de navigare sunt ascunse (un swipe de la margine le arată temporar)
- Temă de zi și de noapte

## Structura codului

```
app/src/main/
├── java/com/example/watchapp/
│   ├── MainActivity.kt         # ecranul principal + modul imersiv
│   ├── AnalogClockView.kt      # view-ul care desenează ceasul analogic
│   └── ZonedDateTextView.kt    # afișează data/ziua într-un fus orar și o limbă alese
└── res/
    ├── layout/activity_main.xml  # cele trei coloane cu ceasuri
    └── values/attrs.xml          # atributele XML: timeZone, face, language, pattern
```

### Componente reutilizabile

`AnalogClockView` se configurează direct din XML:

```xml
<com.example.watchapp.AnalogClockView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:face="romania"
    app:timeZone="Europe/Bucharest" />
```

Valori pentru `app:face`: `plain`, `romania`, `india`, `globe`.

`ZonedDateTextView` afișează data într-un fus orar și o limbă date:

```xml
<com.example.watchapp.ZonedDateTextView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:language="ro"
    app:pattern="EEEE"
    app:timeZone="Europe/Bucharest" />
```

## Cerințe

- Android Studio (versiune recentă)
- Android 10 (API 29) sau mai nou pe telefon / emulator

## Rulare

1. Clonează proiectul:
   ```
   git clone https://github.com/Mithrutz/WatchApp.git
   ```
2. Deschide folderul în Android Studio și așteaptă sincronizarea Gradle.
3. Apasă **Run ▶** pe un emulator sau pe un telefon conectat.

Sau din linia de comandă:

```
./gradlew installDebug
```
