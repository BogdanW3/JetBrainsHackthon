/**
 * Regex Examples for Testing the RegexHoverProvider
 *
 * HOW TO TEST:
 * 1. Open this file in IntelliJ IDEA with the plugin installed
 * 2. Hover over any regex string to see the explanation
 * 3. Select any regex and right-click -> "Explain Regex" for a popup explanation
 */

fun regexExamples() {
    // ========== BASIC CHARACTER CLASSES ==========

    // Digits
    val phonePattern = "\\d\\d\\d-\\d\\d\\d-\\d\\d\\d\\d"  // Phone number format
    val year = "\\d{4}"  // Four digit year

    // Word characters
    val username = "\\w+"  // One or more word characters
    val variable = "\\w{3,20}"  // Variable name 3-20 chars

    // Whitespace
    val spaces = "\\s+"  // One or more spaces
    val trimPattern = "^\\s+|\\s+$"  // Leading/trailing whitespace

    // ========== QUANTIFIERS ==========

    // Zero or more (*)
    val optionalSpaces = "a\\s*b"  // 'a' followed by zero or more spaces then 'b'
    val anyDigits = "\\d*"  // Zero or more digits

    // One or more (+)
    val atLeastOneDigit = "\\d+"  // At least one digit
    val words = "\\w+\\s+\\w+"  // Two words separated by space(s)

    // Zero or one (?)
    val optional = "colou?r"  // Matches 'color' or 'colour'
    val maybeSign = "-?\\d+"  // Optional minus sign before digits

    // Exact count {n}
    val zipCode = "\\d{5}"  // Exactly 5 digits
    val hexColor = "#[0-9a-fA-F]{6}"  // Hex color code

    // Range {m,n}
    val password = "\\w{8,16}"  // 8 to 16 characters
    val phoneNum = "\\d{3,4}-\\d{4}"  // Phone with 3-4 digit area code

    // Minimum {m,}
    val longText = "\\w{10,}"  // At least 10 word characters

    // ========== ANCHORS ==========

    // Start anchor
    val startsWithDigit = "^\\d"  // Starts with a digit
    val lineStart = "^Hello"  // Line starts with "Hello"

    // End anchor
    val endsWithDot = "\\.$"  // Ends with a period
    val lineEnd = "world$"  // Line ends with "world"

    // Both anchors (exact match)
    val exactEmail = "^\\w+@\\w+\\.com$"  // Complete email pattern
    val exactNumber = "^\\d{3}$"  // Exactly 3 digits, nothing else

    // ========== CHARACTER CLASSES ==========

    // Wildcard (any character)
    val anyChar = "a.b"  // 'a', any char, then 'b'
    val threeChars = "..."  // Any three characters

    // Custom character class
    val vowels = "[aeiou]"  // Any vowel
    val hexDigit = "[0-9a-fA-F]"  // Hex digit
    val notDigit = "[^0-9]"  // Anything but a digit
    val range = "[A-Z]"  // Uppercase letter

    // ========== GROUPS ==========

    // Simple groups
    val repeated = "(ab)+"  // 'ab' repeated one or more times
    val options = "(cat|dog)"  // Either 'cat' or 'dog'

    // Nested groups
    val complex = "(\\d{2})-(\\d{2})-(\\d{4})"  // Date format DD-MM-YYYY
    val grouped = "((\\w+)\\s+)+"  // Words with spaces

    // ========== REAL-WORLD EXAMPLES ==========

    // Email validation
    val email = "^\\w+@\\w+\\.\\w+$"
    val emailComplex = "^[a-zA-Z0-9._]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    // URL patterns
    val url = "^https?://\\w+"
    val urlFull = "^https?://[\\w.-]+\\.[a-z]{2,}$"

    // Phone numbers
    val phone1 = "^\\d{3}-\\d{3}-\\d{4}$"
    val phone2 = "^\\(\\d{3}\\)\\s*\\d{3}-\\d{4}$"
    val phoneIntl = "^\\+?\\d{1,3}[-.\\s]?\\d{3}[-.\\s]?\\d{4}$"

    // IP Address
    val ipSimple = "^\\d+\\.\\d+\\.\\d+\\.\\d+$"
    val ipAddress = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"

    // Date formats
    val dateSlash = "^\\d{2}/\\d{2}/\\d{4}$"  // DD/MM/YYYY
    val dateDash = "^\\d{4}-\\d{2}-\\d{2}$"   // YYYY-MM-DD
    val dateWords = "^[A-Z][a-z]+\\s+\\d{1,2},\\s+\\d{4}$"  // January 1, 2025

    // Credit card
    val creditCard = "^\\d{4}-\\d{4}-\\d{4}-\\d{4}$"
    val ccNoSpaces = "^\\d{16}$"

    // Username validation
    val usernamePattern = "^\\w{3,16}$"  // 3-16 alphanumeric chars
    val usernameAdvanced = "^[a-zA-Z][a-zA-Z0-9_]{2,15}$"  // Must start with letter

    // Password requirements
    val passwordSimple = "^.{8,}$"  // At least 8 chars
    val passwordComplex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$"  // Mixed case + digit

    // File extensions
    val imageFile = "\\.(jpg|jpeg|png|gif)$"
    val textFile = "\\.txt$"

    // HTML tags
    val htmlTag = "<\\w+>"
    val htmlClosing = "</\\w+>"

    // Time formats
    val time24 = "^\\d{2}:\\d{2}$"  // 24-hour HH:MM
    val time12 = "^\\d{1,2}:\\d{2}\\s?(AM|PM)$"  // 12-hour with AM/PM

    // Postal codes
    val usZip = "^\\d{5}(-\\d{4})?$"  // US ZIP code
    val ukPostcode = "^[A-Z]{1,2}\\d{1,2}\\s?\\d[A-Z]{2}$"  // UK postcode

    // Social Security Number (US)
    val ssn = "^\\d{3}-\\d{2}-\\d{4}$"

    // Currency
    val currency = "^\\$\\d+(\\.\\d{2})?$"  // Dollar amount
    val price = "^\\d+\\.\\d{2}$"  // Price format

    // ========== ADVANCED PATTERNS ==========

    // Multiple word boundaries
    val sentence = "^\\w+\\s+(\\w+\\s+)*\\w+\\.$"  // Sentence ending with period

    // Repeated groups
    val csvLine = "^\\w+(,\\w+)*$"  // CSV values
    val pathPattern = "^/?(\\w+/)*\\w+$"  // Unix-like path

    // Complex email
    val emailFull = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"

    // MAC address
    val macAddress = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$"

    // IPv6 (simplified)
    val ipv6Simple = "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$"

    // Version number
    val version = "^\\d+\\.\\d+\\.\\d+$"  // Semantic versioning

    // Hashtag
    val hashtag = "#\\w+"

    // Mention
    val mention = "@\\w+"

    // ========== ESCAPING SPECIAL CHARACTERS ==========

    // Literal dots
    val domain = "example\\.com"  // Matches "example.com" literally
    val fileExt = "\\.txt$"  // Ends with .txt

    // Literal parentheses
    val mathExpr = "\\(\\d+\\)"  // Number in parentheses

    // Literal dollar sign
    val dollarAmount = "\\$\\d+"  // Dollar sign followed by digits

    // Literal question mark
    val query = "\\?\\w+=\\w+"  // Query string parameter

    // ========== COMMON VALIDATION PATTERNS ==========

    // Alphanumeric only
    val alphanumeric = "^[a-zA-Z0-9]+$"

    // Letters only
    val lettersOnly = "^[a-zA-Z]+$"

    // Numbers only
    val numbersOnly = "^\\d+$"

    // No special characters
    val noSpecial = "^[a-zA-Z0-9\\s]+$"

    // Must contain uppercase
    val hasUppercase = "[A-Z]+"

    // Must contain lowercase
    val hasLowercase = "[a-z]+"

    // Must contain digit
    val hasDigit = "\\d+"

    // ========== EDGE CASES ==========

    // Empty groups
    val emptyGroup = "()"

    // Nested quantifiers (be careful!)
    val nestedQuant = "(\\w+)*"

    // Multiple consecutive quantifiers
    val multiQuant = "a+*"  // Usually invalid but good to test

    // Very long pattern
    val longPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
}

/**
 * Additional test strings to match against
 */
fun testStrings() {
    // Test against these strings with the patterns above
    val testData = listOf(
        // Phone numbers
        "123-456-7890",
        "(123) 456-7890",
        "+1 123-456-7890",

        // Emails
        "test@example.com",
        "user.name+tag@domain.co.uk",

        // URLs
        "https://example.com",
        "http://www.google.com",

        // Dates
        "01/15/2025",
        "2025-12-13",
        "December 13, 2025",

        // Times
        "14:30",
        "2:30 PM",

        // Misc
        "password123",
        "$19.99",
        "192.168.1.1",
        "v1.2.3",
        "#kotlin",
        "@username"
    )
}

