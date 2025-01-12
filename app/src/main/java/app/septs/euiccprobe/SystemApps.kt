package app.septs.euiccprobe

import android.content.Context
import android.content.pm.PackageManager
import java.io.File

object SystemApps {
    private val requiredPermissions = setOf(
        "android.permission.MODIFY_PHONE_STATE",
        "android.permission.READ_PRIVILEGED_PHONE_STATE",
        "android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS",
    )

    private val optionalPermissions = setOf(
        "android.permission.SECURE_ELEMENT_PRIVILEGED_OPERATION",
        "com.android.permission.WRITE_EMBEDDED_SUBSCRIPTIONS",
    )

    fun getSystemLPAs(): List<PrivAppPermissionParser.Companion.PrivAppPermission> {
        val directories = listOf("/", "/system", "/vendor", "/product")
        val parser = PrivAppPermissionParser()
        for (directory in directories) {
            val permissions = File(directory, "etc/permissions/")
            if (!permissions.exists()) continue
            val files = permissions.listFiles() ?: continue
            for (file in files) {
                if (!file.canRead()) continue
                if (!file.name.startsWith("privapp-permissions")) continue
                if (file.extension != "xml") continue
                file.inputStream().use(parser::parse)
            }
        }
        return parser.filter { perm ->
            perm.allowedPermissions.containsAll(requiredPermissions) &&
                    perm.allowedPermissions.any(optionalPermissions::contains)
        }
    }

    fun getApplicationLabel(pm: PackageManager, packageName: String): String? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }
}