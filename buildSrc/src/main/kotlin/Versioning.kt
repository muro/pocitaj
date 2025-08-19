import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

// We use an 'object' to make the function easily accessible (like a static class)
object Versioning {
    fun getVersionInfo(): Pair<Int, String> {
        val buildTime = Date()
        val timeZone = TimeZone.getTimeZone("Europe/Zurich")

        val epochStart = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US).apply {
            this.timeZone = timeZone
        }.parse("2024-01-01T00:00:00")!!

        // versionCode: Total minutes since our start date.
        val diff = buildTime.time - epochStart.time
        val versionCode = TimeUnit.MILLISECONDS.toMinutes(diff).toInt()

        // versionName: Human-readable string.
        val versionNameFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).apply {
            this.timeZone = timeZone
        }
        val versionName = versionNameFormat.format(buildTime)

        return Pair(versionCode, versionName)
    }
}