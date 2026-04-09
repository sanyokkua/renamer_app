---
title: "Add a Language"
description: "Step-by-step guide to adding a new language/locale to the Renamer App UI"
audience: "developers"
last_validated: "2026-04-09"
last_commit: "3c570e2"
related_modules:
  - "app/ui"
---

# Add a Language

This guide explains how to add a new UI language (locale) to the Renamer App. The process has two parts: create the translated properties file and register the language code in the settings dialog.

## Overview

The Renamer App uses Java's `ResourceBundle` for all user-visible strings. At startup, `DIAppModule.provideResourceBundle()` reads the saved language code from settings, resolves it to a `Locale`, and loads the matching `lang_{code}.properties` file from `app/ui/src/main/resources/langs/`. This bundle is a Guice singleton — it is fixed for the entire session. **A language change takes effect only after the app is restarted.** The settings dialog shows a restart badge when the selection differs from the active locale.

If no matching bundle exists for the requested locale, the app falls back to English (`lang.properties`) without crashing. A warning is logged to help you identify the missing file.

## Step 1: Create the Properties File

**Location:** `app/ui/src/main/resources/langs/`

**Naming convention:**

| Language type      | File name                          | Example                 |
|--------------------|------------------------------------|-------------------------|
| Language only      | `lang_{ISO 639-1}.properties`      | `lang_fr.properties`    |
| Language + country | `lang_{lang}_{COUNTRY}.properties` | `lang_pt_BR.properties` |

The code you use in the filename must match the key you add to `SUPPORTED_LANGUAGES` in Step 2 exactly.

**Workflow:**

1. Copy `lang.properties` (English base) to a new file with the appropriate name.
2. Translate each value (the text to the right of `=`). Never change the keys (the text to the left of `=`).
3. Save the file as UTF-8 — Java 9+ reads `.properties` files as UTF-8 natively. No Unicode escape sequences needed.

**Example — creating Portuguese (Brazil):**

```bash
cp app/ui/src/main/resources/langs/lang.properties \
   app/ui/src/main/resources/langs/lang_pt_BR.properties
```

Then open `lang_pt_BR.properties` and translate each line:

```properties
# Original (lang.properties):
app_header=Renamer App. Rename your files.
btn_rename=Rename files
# Translated (lang_pt_BR.properties):
app_header=Renamer App. Renomeie seus arquivos.
btn_rename=Renomear arquivos
```

## Step 2: Register the Locale

**File:** `app/ui/src/main/java/ua/renamer/app/ui/controller/SettingsDialogController.java`

The `SUPPORTED_LANGUAGES` static map (lines 62–81) is the only registration point. It maps language code → native display name. The order of entries determines the order in the Settings language dropdown.

Add one line to the static initializer:

```java
static {
    SUPPORTED_LANGUAGES = new LinkedHashMap<>();
    SUPPORTED_LANGUAGES.put("en", "English");
    SUPPORTED_LANGUAGES.put("cs", "Čeština");
    // ... existing entries ...
    SUPPORTED_LANGUAGES.put("uk_UA", "Українська");
    SUPPORTED_LANGUAGES.put("pt_BR", "Português (Brasil)");   // add your entry here

    LANGUAGE_CODE_BY_DISPLAY = new LinkedHashMap<>();
    SUPPORTED_LANGUAGES.forEach((code, display) -> LANGUAGE_CODE_BY_DISPLAY.put(display, code));
}
```

**Rules for the display name:**

- Write the language name in the target language, not in English ("Deutsch" not "German").
- For regional variants, include the region in parentheses: "Português (Brasil)".

**How the code is resolved at startup:**

`DIAppModule.resolveLocale("pt_BR")` calls `Locale.forLanguageTag("pt_BR".replace('_', '-'))`, which produces `Locale("pt", "BR")`. The runtime then looks for `lang_pt_BR.properties`. If the file is missing, the app falls back to English and logs a warning — no crash.

## Step 3: Test

1. Build and run the app:

   ```bash
   cd app/ui && mvn javafx:run
   ```

2. Open **Settings** (menu → Settings or the gear icon).

3. Select your new language from the dropdown. A restart badge appears — this is expected.

4. Click **Save**, then restart the app.

5. Verify that all visible UI strings render in the new language. Check:
    - Main window labels and buttons
    - Mode panel labels for each transformation mode
    - Table column headers
    - Dialog buttons and messages
    - Settings dialog itself

6. Check the log for `MissingResourceException` warnings — each warning identifies a key that has a translation in English but is missing from your file. Add the missing entries and restart.

## Translation Guidelines

### Keys are immutable

Key names (left of `=`) are referenced throughout the codebase via the `TextKeys` enum and direct string lookups. **Never rename, delete, or add keys** — only the value (right of `=`) is yours to change. Missing keys cause a silent fallback to an empty string in the UI.

### MessageFormat placeholders

Some values use `{0}`, `{1}` positional placeholders. Preserve them in your translation — they are replaced at runtime with dynamic values:

```properties
# lang.properties
dialog_folder_header_multiple={0} folders were dropped. How should they be handled?
# lang_de.properties — placeholder preserved, position may change
dialog_folder_header_multiple={0} Ordner wurden abgelegt. Wie sollen sie behandelt werden?
```

### String length

JavaFX labels in the mode panels and table headers have limited horizontal space. If a translation is significantly longer than the English original, the label may be clipped or cause layout overflow. Test all mode panels after translating.

### Character encoding

Save files as UTF-8. Do not use `\uXXXX` Unicode escapes for non-ASCII characters — the Java 9+ properties reader handles UTF-8 natively. Escapes are only necessary if your editor cannot save as UTF-8.

## Existing Locales

| Code    | Display Name | File                    |
|---------|--------------|-------------------------|
| `en`    | English      | `lang.properties`       |
| `bg`    | Български    | `lang_bg.properties`    |
| `cnr`   | Crnogorski   | `lang_cnr.properties`   |
| `cs`    | Čeština      | `lang_cs.properties`    |
| `de`    | Deutsch      | `lang_de.properties`    |
| `es`    | Español      | `lang_es.properties`    |
| `et`    | Eesti        | `lang_et.properties`    |
| `fr`    | Français     | `lang_fr.properties`    |
| `hr`    | Hrvatski     | `lang_hr.properties`    |
| `hu`    | Magyar       | `lang_hu.properties`    |
| `it`    | Italiano     | `lang_it.properties`    |
| `lt`    | Lietuvių     | `lang_lt.properties`    |
| `lv`    | Latviešu     | `lang_lv.properties`    |
| `pl`    | Polski       | `lang_pl.properties`    |
| `ro`    | Română       | `lang_ro.properties`    |
| `sk`    | Slovenčina   | `lang_sk.properties`    |
| `sl`    | Slovenščina  | `lang_sl.properties`    |
| `sq`    | Shqip        | `lang_sq.properties`    |
| `uk_UA` | Українська   | `lang_uk_UA.properties` |

Use `lang_de.properties` as a reference translation — it is complete and covers every key in `lang.properties`.
