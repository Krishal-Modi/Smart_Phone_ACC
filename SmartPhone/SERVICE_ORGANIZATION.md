# Service Organization for Team Members

## Overview
This document describes how the 8 required features are organized across 5 team members, with each member having their own dedicated service file in the `service/` folder.

---

## Team Member Assignments

### Team Member 1: Spell Checking & Word Completion
**Service File:** `SpellCheckService.java`

**Assigned Features:**
1. **Spell Checking** - Auto-correct misspelled search queries
   - Method: `suggestions(String word, int limit)`
   - Returns top N spelling suggestions using edit distance algorithm
   - Example: "samung" → "samsung"

2. **Word Completion** - Auto-complete search terms as user types
   - Method: `completions(String prefix, int limit)`
   - Returns words from vocabulary that start with given prefix
   - Example: "sam" → ["samsung", "sample"]

**Technical Details:**
- Uses Levenshtein Edit Distance algorithm for spell checking
- Maintains vocabulary HashSet built from phone data
- Helper methods: `editDistance()`, `buildVocabulary()`, `getVocabulary()`

---

### Team Member 2: Frequency Analysis
**Service File:** `FrequencyService.java`

**Assigned Features:**
1. **Frequency Count** - Count word occurrences in web pages
   - Method: `countWordInUrl(String url, String word)`
   - Uses Jsoup to fetch and parse web pages
   - Returns count of how many times a word appears
   - UI Available at: `/word-frequency`

2. **Search Frequency** - Track and display popular searches
   - Methods: `logSearch()`, `topSearches()`, `recentSearches()`
   - Stores search queries in database with timestamps
   - Shows trending searches on dashboard
   - Helps understand user behavior

**Technical Details:**
- Dependency: `SearchLogRepository` for database operations
- Uses Jsoup library for web scraping
- Text processing: lowercase conversion, word tokenization

---

### Team Member 3: Pattern Finding with Regex
**Service File:** `PatternService.java`

**Assigned Features:**
1. **Pattern Finding Using Regular Expressions**
   - Extract structured data using regex patterns
   - Methods for various patterns:
     - `extractNumbers()` - Find all numeric values
     - `extractEmails()` - Extract email addresses
     - `extractStoragePatterns()` - Find storage specs (e.g., "512GB")
     - `extractRamPatterns()` - Find RAM specs (e.g., "8GB RAM")
     - `extractCameraPatterns()` - Find camera specs (e.g., "48MP")
     - `extractPricePatterns()` - Find price values
   - `tokenizeText()` - Split text into words using regex
   - `findPhonesByPattern()` - Search phones matching regex pattern

**Technical Details:**
- 256 lines of pure regex utilities
- No external dependencies
- Comprehensive pattern matching for phone specifications

---

### Team Member 4: Inverted Indexing
**Service File:** `IndexingService.java`

**Assigned Features:**
1. **Inverted Indexing** - Fast search using index structure
   - Method: `rebuildIndex()` - Build word→phone mapping
   - Method: `searchInIndex(String query)` - Quick lookup
   - Structure: Maps each word to list of phones containing it
   - Example Index:
     ```
     "samsung" → [Phone 1, Phone 5, Phone 12]
     "camera"  → [Phone 1, Phone 3, Phone 8]
     ```
   - Eliminates need to scan all 105 phones for each search

**Index Structure:**
```
Word      | Phone ID | Count
----------|----------|------
"samsung" | 1        | 3
"samsung" | 5        | 1
"camera"  | 1        | 5
```

**Technical Details:**
- Dependencies: `PhoneRepository`, `WordIndexRepository`
- Processes all phone fields: brand, model, processor, features
- Helper methods: `getWordEntries()`, `getMostCommonWords()`, `isIndexEmpty()`

---

### Team Member 5: Ranking & Validation
**Service File:** `RankingService.java`

**Assigned Features:**
1. **Page Ranking** - Rank search results by relevance
   - Method: `rankPhonesByQuery(String query)`
   - Algorithm: Count word occurrences, sort by relevance score
   - Higher score = more relevant = appears first
   - Example:
     ```
     Search: "5G camera"
     Phone A: "5G"×3 + "camera"×5 = Score 8 (ranked 1st)
     Phone B: "5G"×1 + "camera"×2 = Score 3 (ranked 2nd)
     ```

2. **Data Validation Using Regular Expressions**
   - Input validation methods:
     - `isValidEmail()` - Validate email format
     - `isValidPhoneNumber()` - Validate phone format
     - `isValidUrl()` - Validate URL format
     - `isValidPrice()` - Validate price format
     - `isValidStorageFormat()` - Validate storage specs
     - `isValidRamFormat()` - Validate RAM specs
   - Helper methods:
     - `containsDigits()` - Check for numeric content
     - `extractDigits()` - Extract numbers from text
     - `sanitizeQuery()` - Clean user input
     - `isStorageQuery()` - Detect storage-related searches

**Technical Details:**
- Dependencies: `PhoneRepository`, `WordIndexRepository`
- Uses regex patterns for validation
- Integrates with inverted index for ranking

---

## Service File Summary

| Team Member | Service File | Features | Line Count |
|-------------|--------------|----------|------------|
| Member 1 | `SpellCheckService.java` | Spell Checking, Word Completion | 147 |
| Member 2 | `FrequencyService.java` | Frequency Count, Search Frequency | ~150 |
| Member 3 | `PatternService.java` | Pattern Finding (Regex) | 256 |
| Member 4 | `IndexingService.java` | Inverted Indexing | 205 |
| Member 5 | `RankingService.java` | Page Ranking, Data Validation (Regex) | ~230 |

**Total:** 5 service files implementing all 8 required features

---

## Integration Points

### How Services Work Together:

1. **Search Flow:**
   ```
   User types → SpellCheckService (auto-complete/spell-check)
             → FrequencyService (log search)
             → IndexingService (quick lookup)
             → RankingService (rank results)
             → Display results
   ```

2. **Data Processing:**
   ```
   Phone Data → PatternService (extract specs using regex)
             → IndexingService (build inverted index)
             → SpellCheckService (build vocabulary)
   ```

3. **Validation:**
   ```
   User Input → RankingService (validate format)
             → SpellCheckService (check spelling)
             → Process query
   ```

### Controllers Using Services:

**DashboardController:**
- Uses `FrequencyService` - Show top searches
- Uses `SpellCheckService` - Provide vocabulary for autocomplete

**ApiController:**
- Uses `SpellCheckService` - `/api/suggest` endpoint
- Uses `RankingService` - `/api/search` endpoint (ranked results)
- Uses `FrequencyService` - `/api/word-frequency` endpoint

**CsvService:**
- Uses `FrequencyService` - Log all searches
- Uses `SpellCheckService` - Auto-correct search queries
- Uses `RankingService` - Validate search inputs

---

## Testing Each Feature

### Member 1 - SpellCheckService:
1. **Spell Checking:** Search for "samung" → Should suggest "samsung"
2. **Word Completion:** Type "sam" in search → Should show ["samsung", ...]

### Member 2 - FrequencyService:
1. **Frequency Count:** Go to `/word-frequency`, enter URL and word → See count
2. **Search Frequency:** Check dashboard → See "Top Searches" section

### Member 3 - PatternService:
1. **Pattern Finding:** Call `extractStoragePatterns()` with phone text → Returns ["512GB", "256GB"]
2. **Regex Search:** Call `findPhonesByPattern()` with pattern → Returns matching phones

### Member 4 - IndexingService:
1. **Inverted Index:** Call `rebuildIndex()` → Creates word→phone mapping
2. **Quick Search:** Call `searchInIndex("samsung")` → Returns phone IDs instantly

### Member 5 - RankingService:
1. **Page Ranking:** Search for "5G camera" → Results ordered by relevance score
2. **Data Validation:** Call `isValidEmail("test@example.com")` → Returns true

---

## File Locations

All service files are located in:
```
d:\Project\SmartPhone\src\main\java\com\example\SmartPhone\service\
```

**Team Member Service Files:**
- `SpellCheckService.java` (Member 1)
- `FrequencyService.java` (Member 2)
- `PatternService.java` (Member 3)
- `IndexingService.java` (Member 4)
- `RankingService.java` (Member 5)

**Supporting Service Files:**
- `CsvService.java` - Business logic, uses all team services
- `UserService.java` / `UserServiceImpl.java` - User authentication

---

## Development Notes

### Each Team Member Should:
1. **Understand their service file** - Read the code and comments
2. **Test their features** - Verify methods work correctly
3. **Document changes** - Add comments for any modifications
4. **Coordinate with others** - Some features depend on each other

### Code Quality Standards:
- ✅ All methods have JavaDoc comments
- ✅ Error handling with try-catch blocks
- ✅ Input validation (null checks, length limits)
- ✅ Logging using SLF4J Logger
- ✅ Clean, readable code with descriptive names

### Dependencies Between Services:
- **IndexingService** should be built before **RankingService** can rank results
- **SpellCheckService** vocabulary should be loaded before auto-complete works
- **FrequencyService** logs are stored in database (requires `SearchLogRepository`)

---

## Conclusion

The project is organized so each team member has:
- ✅ Their own dedicated service file
- ✅ Clear feature ownership (1-2 features per person)
- ✅ Well-documented code with examples
- ✅ Independent work area (minimal conflicts)

All 8 required features are implemented and working across the 5 service files!
