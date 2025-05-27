# SnackTrack - Appwrite Database Schema

Diese Dokumentation beschreibt alle erforderlichen Collections und deren Attribute für das SnackTrack Android Projekt.

## 📁 Datenbank

- **Database ID**: `snacktrack-db`
- **Name**: SnackTrack Database

## 📋 Collections

### 1. `dogs` - Hunde Collection

**Beschreibung**: Speichert alle Hundedaten der Benutzer.

| Attribut | Typ | Required | Beschreibung |
|----------|-----|----------|--------------|
| `ownerId` | string(255) | ✅ | ID des Besitzers (User ID) |
| `name` | string(255) | ✅ | Name des Hundes |
| `breed` | string(255) | ❌ | Rasse des Hundes |
| `birthDate` | string(10) | ❌ | Geburtsdatum im ISO-Format (YYYY-MM-DD) |
| `sex` | enum | ✅ | Geschlecht: `MALE`, `FEMALE`, `UNKNOWN` (default: `UNKNOWN`) |
| `weight` | float | ✅ | Aktuelles Gewicht in kg |
| `targetWeight` | float | ❌ | Zielgewicht in kg |
| `activityLevel` | enum | ✅ | Aktivitätslevel: `VERY_LOW`, `LOW`, `NORMAL`, `HIGH`, `VERY_HIGH` (default: `NORMAL`) |
| `imageId` | string(255) | ❌ | ID des Profilbildes im Storage |
| `dailyCalorieNeed` | integer | ❌ | Berechneter täglicher Kalorienbedarf |

**Berechtigungen**: `read("users")`, `create("users")`, `update("users")`, `delete("users")`

### 2. `weightEntries` - Gewichtseinträge Collection

**Beschreibung**: Speichert Gewichtsverlauf für jeden Hund.

| Attribut | Typ | Required | Beschreibung |
|----------|-----|----------|--------------|
| `dogId` | string(255) | ✅ | Referenz zur Hunde-ID |
| `weight` | float | ✅ | Gewicht in kg |
| `timestamp` | string(30) | ✅ | Zeitstempel im ISO-DateTime-Format |
| `note` | string(1000) | ❌ | Optionale Notiz |

**Berechtigungen**: `read("users")`, `create("users")`, `update("users")`, `delete("users")`

### 3. `foodIntake` - Futteraufnahmen Collection

**Beschreibung**: Speichert alle Mahlzeiten und Futteraufnahmen der Hunde.

| Attribut | Typ | Required | Beschreibung |
|----------|-----|----------|--------------|
| `dogId` | string(255) | ✅ | Referenz zur Hunde-ID |
| `foodId` | string(255) | ❌ | Referenz zur Food-DB (null = manueller Eintrag) |
| `foodName` | string(500) | ✅ | Name des Futters |
| `amountGram` | float | ✅ | Menge in Gramm |
| `calories` | integer | ✅ | Kalorien der Portion |
| `timestamp` | string(30) | ✅ | Zeitstempel im ISO-DateTime-Format |
| `note` | string(1000) | ❌ | Optionale Notiz |
| `protein` | float | ❌ | Protein-Gehalt (für manuelle Einträge) |
| `fat` | float | ❌ | Fett-Gehalt (für manuelle Einträge) |
| `carbs` | float | ❌ | Kohlenhydrat-Gehalt (für manuelle Einträge) |

**Berechtigungen**: `read("users")`, `create("users")`, `update("users")`, `delete("users")`

### 4. `foodDB` - Futterdatenbank Collection

**Beschreibung**: Zentrale Datenbank aller Futtermittel mit Nährwertangaben.

| Attribut | Typ | Required | Beschreibung |
|----------|-----|----------|--------------|
| `ean` | string(20) | ✅ | EAN-Code/Barcode (unique) |
| `brand` | string(255) | ✅ | Markenname |
| `product` | string(500) | ✅ | Produktname |
| `protein` | float | ✅ | Protein-Gehalt pro 100g |
| `fat` | float | ✅ | Fett-Gehalt pro 100g |
| `crudeFiber` | float | ✅ | Rohfaser-Gehalt pro 100g |
| `rawAsh` | float | ✅ | Rohasche-Gehalt pro 100g |
| `moisture` | float | ✅ | Feuchtigkeits-Gehalt pro 100g |
| `additives` | string(5000) | ❌ | Zusatzstoffe als JSON-String |
| `imageUrl` | string(500) | ❌ | URL zum Produktbild |

**Berechnete Werte** (im Client):
- `carbs` = 100 - (protein + fat + crudeFiber + rawAsh + moisture)
- `kcalPer100g` = (protein × 3.5) + (fat × 8.5) + (carbs × 3.5)

**Berechtigungen**: `read("users")`, `create("users")`, `update("users")`, `delete("users")`

### 5. `foodSubmissions` - Futtervorschläge Collection

**Beschreibung**: Speichert Vorschläge von Benutzern für neue Futtermittel.

| Attribut | Typ | Required | Beschreibung |
|----------|-----|----------|--------------|
| `userId` | string(255) | ✅ | ID des vorschlagenden Benutzers |
| `ean` | string(20) | ✅ | EAN-Code/Barcode |
| `brand` | string(255) | ✅ | Markenname |
| `product` | string(500) | ✅ | Produktname |
| `protein` | float | ✅ | Protein-Gehalt pro 100g |
| `fat` | float | ✅ | Fett-Gehalt pro 100g |
| `crudeFiber` | float | ✅ | Rohfaser-Gehalt pro 100g |
| `rawAsh` | float | ✅ | Rohasche-Gehalt pro 100g |
| `moisture` | float | ✅ | Feuchtigkeits-Gehalt pro 100g |
| `additives` | string(5000) | ❌ | Zusatzstoffe als JSON-String |
| `imageUrl` | string(500) | ❌ | URL zum Produktbild |
| `status` | enum | ✅ | Status: `PENDING`, `APPROVED`, `REJECTED` (default: `PENDING`) |
| `submittedAt` | string(30) | ✅ | Einreichungs-Zeitstempel |
| `reviewedAt` | string(30) | ❌ | Review-Zeitstempel |

**Berechtigungen**: `read("users")`, `create("users")`, `update("users")`, `delete("users")`

## 🗂️ Storage Buckets

### `dog_images` - Hundebilder Bucket

**Beschreibung**: Speichert Profilbilder der Hunde.

| Einstellung | Wert |
|-------------|------|
| **Bucket ID** | `dog_images` |
| **Name** | Dog Images |
| **Max File Size** | 10 MB (10485760 bytes) |
| **Erlaubte Dateiformate** | jpg, jpeg, png, webp |
| **Kompression** | gzip |
| **Verschlüsselung** | Aktiviert |
| **Antivirus** | Aktiviert |
| **Berechtigungen** | `read("users")`, `create("users")`, `update("users")`, `delete("users")` |

**URL-Format für Bilder**:
```
https://parse.nordburglarp.de/v1/storage/buckets/dog_images/files/{FILE_ID}/view?project=6829fc47b73f5bc2be1f
```

## 🔍 Indexes

Für optimale Performance werden folgende Indexes erstellt:

### dogs Collection
- `ownerId` (key) - Für Abfragen nach Besitzer

### weightEntries Collection
- `dogId` (key) - Für Abfragen nach Hund
- `dogId_timestamp` (compound key) - Für zeitliche Sortierung

### foodIntake Collection
- `dogId` (key) - Für Abfragen nach Hund
- `dogId_timestamp` (compound key) - Für zeitliche Sortierung

### foodDB Collection
- `ean` (unique) - Eindeutige EAN-Codes
- `brand_product` (fulltext) - Für Suchfunktion

### foodSubmissions Collection
- `userId` (key) - Für Abfragen nach Benutzer
- `status` (key) - Für Filterung nach Status

## 🚀 Setup

### Automatisches Setup mit Script

1. **API Key besorgen**:
   - Appwrite Console öffnen: https://parse.nordburglarp.de/console
   - Settings → API Keys → Create API Key
   - Scope: `databases.write`, `collections.write`, `attributes.write`, `indexes.write`, `buckets.write`

2. **Script ausführen**:
   ```bash
   # API Key in setup_appwrite_collections.py eintragen
   python3 setup_appwrite_collections.py
   ```

### Manuelles Setup

Falls das Script nicht funktioniert, können alle Collections manuell in der Appwrite Console erstellt werden. Die genauen Attribute sind in dieser Dokumentation aufgeführt.

## 📱 Android Integration

Das Android Projekt verwendet diese Konstanten in `AppwriteService.kt`:

```kotlin
const val DATABASE_ID = "snacktrack-db"
const val COLLECTION_DOGS = "dogs"
const val COLLECTION_WEIGHT_ENTRIES = "weightEntries"
const val COLLECTION_FOOD_INTAKE = "foodIntake"
const val COLLECTION_FOOD_DB = "foodDB"
const val COLLECTION_FOOD_SUBMISSIONS = "foodSubmissions"
const val BUCKET_DOG_IMAGES = "dog_images"
```

Stellen Sie sicher, dass diese IDs exakt mit den in Appwrite erstellten Collections übereinstimmen! 