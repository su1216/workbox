package com.su.workbox.ui.app.git

import android.text.TextUtils
import com.su.workbox.component.annotation.Searchable
import java.util.regex.Pattern

class GitCommit {
    var hash: String? = null
        private set

    @Searchable
    var authorLine: String? = null
        set(value) {
            val matcher = COMMIT_PATTERN.matcher(value!!)
            if (matcher.find()) {
                hash = matcher.group(1)
                extra = matcher.group(2)
            }
            field = value
        }
    var author: String? = null
        private set
    var email: String? = null
        private set

    @Searchable
    var message: String? = null
    var merge: String? = null
    var extra: String? = null
    var timestamp: Long = 0
        private set

    // hash/short hash/tags
    fun setCommitLine(line: String) {
        val matcher = COMMIT_PATTERN.matcher(line)
        if (matcher.find()) {
            hash = matcher.group(1)
            extra = matcher.group(2)
        }
    }

    fun setAuthorAndEmail(line: String) {
        val matcher = COMMIT_AUTHOR.matcher(line)
        if (matcher.find()) {
            author = matcher.group(1)
            email = matcher.group(2)
        }
    }

    fun setDateLine(line: String) {
        val matcher = COMMIT_DATE.matcher(line)
        if (matcher.find()) {
            timestamp = matcher.group(1)?.toLong() ?: 0
        }
    }

    fun getShortHash(length: Int): String? {
        return if (TextUtils.isEmpty(hash) || hash!!.length <= length) {
            hash
        } else hash!!.substring(0, length)
    }

    override fun toString(): String {
        return "GitCommit(hash=$hash, authorLine=$authorLine, author=$author, email=$email, message=$message, merge=$merge, extra=$extra, timestamp=$timestamp)"
    }

    companion object {
        private val COMMIT_PATTERN = Pattern.compile("commit\\s+(\\w{40})(?:\\s+\\((.*)\\))?")
        private val COMMIT_AUTHOR = Pattern.compile("Author:\\s+([^<]+)\\s+<(.+)>")
        private val COMMIT_DATE = Pattern.compile("Date:\\s+(\\d+)")
    }
}
