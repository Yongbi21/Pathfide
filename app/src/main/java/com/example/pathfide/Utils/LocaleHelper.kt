import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {
    fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        // Update configuration and create new context
        context.createConfigurationContext(config)

        // Update the app's resources
        val resources = context.resources
        val metrics = resources.displayMetrics
        val currentConfig = resources.configuration
        currentConfig.setLocale(locale)
        resources.updateConfiguration(currentConfig, metrics)

        // Save the selected language
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("Language", languageCode).apply()
    }

    fun getCurrentLocale(context: Context): String {
        val prefs = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return prefs.getString("Language", "en") ?: "en"
    }

    fun getLocalizedContext(context: Context): Context {
        val currentLanguage = getCurrentLocale(context)
        val locale = Locale(currentLanguage)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}