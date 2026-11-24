package com.chronos.app.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Downloads the release APK, verifies its SHA-256, then hands it to the system
// installer.
//
// It cannot install silently, and pretending otherwise would be dishonest: a
// normal app may only *ask* Android to install, and the user confirms in a system
// dialog. What this class removes is the cable and the manual file copy.
class AndroidUpdateInstaller(
    private val context: Context,
) : UpdateInstaller {

    override suspend fun install(release: ReleaseInfo): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val apk = download(release)
            val expected = release.sha256Url?.let { fetchText(it).trim().substringBefore(' ') }
            if (!expected.isNullOrBlank()) {
                val actual = sha256(apk)
                if (!actual.equals(expected, ignoreCase = true)) {
                    apk.delete()
                    throw IOException("Downloaded APK does not match the published checksum")
                }
            }
            commitToInstaller(apk)
        }
    }

    private fun download(release: ReleaseInfo): File {
        val dir = File(context.cacheDir, "updates").apply { mkdirs() }
        val target = File(dir, "chronos-" + release.versionName + ".apk")
        if (target.exists()) target.delete()

        val connection = (URL(release.downloadUrl).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 20_000
            readTimeout = 60_000
            setRequestProperty("User-Agent", "Chronos-Updater")
        }
        try {
            val status = connection.responseCode
            if (status != HttpURLConnection.HTTP_OK) {
                throw IOException("Download failed with HTTP " + status)
            }
            connection.inputStream.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        } finally {
            connection.disconnect()
        }
        if (target.length() <= 0L) throw IOException("Downloaded APK is empty")
        return target
    }

    private fun fetchText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 15_000
            readTimeout = 20_000
            setRequestProperty("User-Agent", "Chronos-Updater")
        }
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Checksum download failed with HTTP " + connection.responseCode)
            }
            return connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }

    private fun commitToInstaller(apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            session.openWrite("chronos-update", 0, apk.length()).use { output ->
                apk.inputStream().use { input -> input.copyTo(output) }
                session.fsync(output)
            }
            val resultIntent = Intent(context, InstallResultReceiver::class.java)
                .setAction(InstallResultReceiver.ACTION_INSTALL_RESULT)
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
            val pending = PendingIntent.getBroadcast(context, sessionId, resultIntent, flags)
            session.commit(pending.intentSender)
        }
        Log.i(TAG, "install session " + sessionId + " committed")
    }

    private companion object {
        const val TAG = "ChronosUpdater"
    }
}
