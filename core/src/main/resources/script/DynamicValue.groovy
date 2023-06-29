import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import static java.util.UUID.randomUUID

/**
 * Get current date with specified format.
 *
 * @param format String of data format.  default : yyyy-MM-dd
 */
static def currentDate(String format = "yyyy-MM-dd") {
    LocalDateTime.now().format(DateTimeFormatter.ofPattern(format))
}

/**
 * Get calculated date with specified format.
 *
 * @param format String of data format. default : yyyy-MM-dd
 * @param addDays Number of date add or minus. default : 0
 */
static def calculatedDate(String format = "yyyy-MM-dd", int addDays = 0) {
    LocalDateTime.now().plusDays(addDays).format(DateTimeFormatter.ofPattern(format))
}

/**
 * Get random int value.
 *
 * @param min default : 0
 * @param max default : 10
 */
static def randomInt(int min = 0, int max = 10) {
    Math.random() * max + min as int
}

/**
 * Get random float value.
 *
 * @param digits default : 2
 */
static def randomFloat(int digits = 2) {
    String.format("%.${digits}f", Math.random()) as float
}

/**
 * Get random sting with a to Z and 0 to 9 and '_'.
 *
 * @param length default : 16
 */
static def randomString(int length = 16) {
    def chars = (0..9) + ('a'..'z') + ('A'..'Z') + '_'
    new Random().with { (1..length).collect { chars[nextInt(chars.size())] }.join() }
}

/**
 * Get random boolean value.
 */
static def randomBoolean() {
    randomInt() % 2 == 0 ? true : false
}

/**
 * Get uuid as string
 */
static def uuid() {
    randomUUID() as String
}

/**
 * Create message digest of src string
 *
 * @param src input string which has UTF-8 as encoding.
 * @param algorithm default : SHA-256, You can select other algorithm as well like MD5, SHA-512.
 */
static def messageDigest(String src, String algorithm = "SHA-256") {
    new BigInteger(1, MessageDigest.getInstance(algorithm).digest(src.getBytes())).toString(16)
}

/**
 * Create url encoded string
 *
 * @param src input string which has UTF-8 as charset.
 */
static def urlEncode(String src) {
    URLEncoder.encode(src, StandardCharsets.UTF_8.name())
}

/**
 * Create url decode string
 *
 * @param src input url encoded string which has UTF-8 as charset.
 */
static def urlDecode(String src) {
    URLDecoder.decode(src, StandardCharsets.UTF_8)
}