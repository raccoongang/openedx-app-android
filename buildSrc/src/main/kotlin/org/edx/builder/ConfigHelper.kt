package org.edx.builder

import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.net.URLEncoder

class ConfigHelper(private val projectDir: File, buildType: String) {

    companion object {
        private const val CONFIG_SETTINGS_YAML_FILENAME = "config_settings.yaml"
        private const val DEFAULT_CONFIG_PATH = "./default_config/$CONFIG_SETTINGS_YAML_FILENAME"
        private const val CONFIG_DIRECTORY = "config_directory"
        private const val CONFIG_MAPPING = "config_mapping"
        private const val MAPPINGS_FILENAME = "file_mappings.yaml"
        private const val ANDROID_CONFIG_KEY = "android"
        private const val FILES_CONFIG_KEY = "files"
    }

    private val configDir: String

    init {
        val environment = if (buildType == "develop" || buildType.isEmpty()) "dev" else buildType

        var configFile = File(CONFIG_SETTINGS_YAML_FILENAME)
        if (!configFile.exists()) {
            println("Configurations are missing at ${configFile.path}")
            println("Parsing Default configurations from $DEFAULT_CONFIG_PATH")
            configFile = File(DEFAULT_CONFIG_PATH)
        }
        if (!configFile.exists()) {
            throw Exception("Configurations are missing at ${configFile.path}")
        }

        val yaml = Yaml()
        @Suppress("UNCHECKED_CAST")
        val config = yaml.load<Map<String, Any>>(FileInputStream(configFile))
        val configDirectory = config[CONFIG_DIRECTORY] as? String
        @Suppress("UNCHECKED_CAST")
        val configMapping = config[CONFIG_MAPPING] as? Map<String, String>

        if (configDirectory != null && configMapping?.get(environment) != null) {
            configDir = "$configDirectory/${configMapping[environment]}"
        } else {
            throw Exception("$environment key doesn't exist in ${configFile.path}")
        }
    }

    fun fetchConfig(): LinkedHashMap<String, Any> {
        val configFilesMapping = File("$configDir/$MAPPINGS_FILENAME")
        if (!configFilesMapping.exists()) {
            throw Exception("Inappropriate config directory format: $configFilesMapping")
        }

        val yaml = Yaml()
        @Suppress("UNCHECKED_CAST")
        val configMappingFiles = yaml.load<Map<String, Any>>(FileInputStream(configFilesMapping))

        @Suppress("UNCHECKED_CAST")
        val androidConfig = configMappingFiles.getOrDefault(ANDROID_CONFIG_KEY, emptyMap<String, Any>()) as Map<String, Any>
        @Suppress("UNCHECKED_CAST")
        val androidConfigFiles = androidConfig.getOrDefault(FILES_CONFIG_KEY, emptyList<String>()) as List<String>

        val androidConfigs = LinkedHashMap<String, Any>()
        for (file in androidConfigFiles) {
            val configFile = File("$configDir/$file")
            if (configFile.exists()) {
                @Suppress("UNCHECKED_CAST")
                val config = yaml.load<Map<String, Any>>(FileInputStream(configFile))
                if (config != null) {
                    androidConfigs.putAll(config)
                }
            }
        }
        return androidConfigs
    }

    fun generateConfigJson() {
        val config = fetchConfig()
        val configJsonDir = File("${projectDir.path}/core/assets/config")
        configJsonDir.mkdirs()
        val jsonString = mapToJson(config)
        FileWriter("${configJsonDir.path}/config.json").use { writer ->
            writer.write(jsonString)
        }
    }

    fun generateMicrosoftConfig() {
        val config = fetchConfig()
        val applicationId = config.getOrDefault("APPLICATION_ID", "") as String
        var clientId = ""
        var packageSign = ""
        @Suppress("UNCHECKED_CAST")
        val microsoft = config["MICROSOFT"] as? Map<String, Any>
        if (microsoft != null) {
            packageSign = microsoft.getOrDefault("PACKAGE_SIGNATURE", "") as String
            clientId = microsoft.getOrDefault("CLIENT_ID", "") as String
        }
        val microsoftConfigsJsonPath = "${projectDir.path}/core/src/main/res/raw/"
        File(microsoftConfigsJsonPath).mkdirs()
        val sign = URLEncoder.encode(packageSign, "UTF-8")
        val configJson = linkedMapOf(
            "client_id" to clientId,
            "authorization_user_agent" to "DEFAULT",
            "redirect_uri" to "msauth://$applicationId/$sign",
            "account_mode" to "MULTIPLE",
            "broker_redirect_uri_registered" to false
        )
        FileWriter("${microsoftConfigsJsonPath}microsoft_auth_config.json").use { writer ->
            writer.write(mapToJsonPretty(configJson))
        }
    }

    fun generateGoogleServicesJson(applicationId: String) {
        val config = fetchConfig()
        @Suppress("UNCHECKED_CAST")
        val firebase = config["FIREBASE"] as? Map<String, Any> ?: return
        if (firebase.getOrDefault("ENABLED", false) != true) return

        val googleServicesJsonPath = "${projectDir.path}/app/"
        File(googleServicesJsonPath).mkdirs()

        val projectInfo = linkedMapOf(
            "project_number" to (firebase.getOrDefault("PROJECT_NUMBER", "") as String),
            "project_id" to (firebase.getOrDefault("PROJECT_ID", "") as String),
            "storage_bucket" to "${firebase.getOrDefault("PROJECT_ID", "")}.appspot.com"
        )
        val clientInfo = linkedMapOf(
            "mobilesdk_app_id" to (firebase.getOrDefault("APPLICATION_ID", "") as String),
            "android_client_info" to linkedMapOf(
                "package_name" to applicationId
            )
        )
        val client = linkedMapOf(
            "client_info" to clientInfo,
            "oauth_client" to emptyList<Any>(),
            "api_key" to listOf(linkedMapOf("current_key" to (firebase.getOrDefault("API_KEY", "") as String))),
            "services" to linkedMapOf(
                "appinvite_service" to linkedMapOf(
                    "other_platform_oauth_client" to emptyList<Any>()
                )
            )
        )
        val configJson = linkedMapOf(
            "project_info" to projectInfo,
            "client" to listOf(client),
            "configuration_version" to "1"
        )

        FileWriter("${googleServicesJsonPath}google-services.json").use { writer ->
            writer.write(mapToJsonPretty(configJson))
        }
    }

    fun removeGoogleServicesJson() {
        val googleServicesJsonPath = "${projectDir.path}/app/google-services.json"
        val file = File(googleServicesJsonPath)
        if (file.exists()) {
            file.delete()
        }
    }

    // Simple JSON serialization without external dependencies
    private fun mapToJson(obj: Any?): String {
        return when (obj) {
            null -> "null"
            is Boolean -> obj.toString()
            is Number -> obj.toString()
            is String -> "\"${escapeJson(obj)}\""
            is Map<*, *> -> {
                val entries = obj.entries.joinToString(",") { (k, v) ->
                    "\"${escapeJson(k.toString())}\":${mapToJson(v)}"
                }
                "{$entries}"
            }
            is List<*> -> {
                val items = obj.joinToString(",") { mapToJson(it) }
                "[$items]"
            }
            else -> "\"${escapeJson(obj.toString())}\""
        }
    }

    private fun mapToJsonPretty(obj: Any?, indent: String = ""): String {
        val nextIndent = "$indent  "
        return when (obj) {
            null -> "null"
            is Boolean -> obj.toString()
            is Number -> obj.toString()
            is String -> "\"${escapeJson(obj)}\""
            is Map<*, *> -> {
                if (obj.isEmpty()) return "{}"
                val entries = obj.entries.joinToString(",\n") { (k, v) ->
                    "$nextIndent\"${escapeJson(k.toString())}\": ${mapToJsonPretty(v, nextIndent)}"
                }
                "{\n$entries\n$indent}"
            }
            is List<*> -> {
                if (obj.isEmpty()) return "[]"
                val items = obj.joinToString(",\n") { "$nextIndent${mapToJsonPretty(it, nextIndent)}" }
                "[\n$items\n$indent]"
            }
            else -> "\"${escapeJson(obj.toString())}\""
        }
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
