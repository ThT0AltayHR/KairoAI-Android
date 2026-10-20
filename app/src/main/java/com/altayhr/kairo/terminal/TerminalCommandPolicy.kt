package com.altayhr.kairo.terminal

/**
 * Conservative deny-list for AI-proposed commands. This is a guardrail, not an OS sandbox:
 * approved shell commands still run with Kairo's normal Android app UID and filesystem access.
 */
object TerminalCommandPolicy {
    private val forbidden = listOf(
        Regex("(^|[;&|\\s])su([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])sudo([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])mkfs(\\.[a-z0-9]+)?([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])wipefs([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])reboot([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])shutdown([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])mount([;&|\\s]|$)"),
        Regex("(^|[;&|\\s])umount([;&|\\s]|$)"),
        Regex("pm\\s+(clear|uninstall)"),
        Regex("settings\\s+put"),
        Regex("curl[^\\n]*\\|\\s*(sh|bash)"),
        Regex("wget[^\\n]*\\|\\s*(sh|bash)"),
        Regex("/data/data/com\\.termux/"),
        Regex("/data/user/0/com\\.termux/"),
        Regex("/data/data/com\\.altayhr\\.kairo(?:/|\\s|$)"),
        Regex("/data/user/0/com\\.altayhr\\.kairo(?:/|\\s|$)"),
        Regex("(^|[;&|\\s])dd\\b[^\\n]*\\bof=/dev/"),
        Regex("(^|[;&|\\s])fastboot([;&|\\s]|$)")
    )

    private val dangerousRecursiveDelete = Regex(
        """(?is)(^|[;&|\s])(?:(?:busybox|toybox)\s+)?rm\s+(?:(?:-[a-z]*[rf][a-z]*)|(?:--recursive)|(?:--force))(?:\s+(?:(?:-[a-z]*[rf][a-z]*)|(?:--recursive)|(?:--force)))*\s+(?:/|~(?:/|[\"'\s;&|]|$)|[\"']?[$]HOME\b[\"']?(?:/|[\"'\s;&|]|$)|[\"']?[$]\{HOME\}[\"']?(?:/|[\"'\s;&|]|$))"""
    )

    private val parentTraversal = Regex("(^|[\\s/])\\.\\.(?:/|[\\s;&|]|$)")

    private val secrets = listOf(
        Regex("ghp_[a-z0-9]{20,}", RegexOption.IGNORE_CASE),
        Regex("github_pat_[a-z0-9_]{20,}", RegexOption.IGNORE_CASE),
        Regex("AIza[0-9a-z_-]{30,}", RegexOption.IGNORE_CASE),
        Regex("sk-[a-z0-9_-]{24,}", RegexOption.IGNORE_CASE),
        Regex("(?i)(?:[A-Z0-9_]*(?:API[_-]?KEY|ACCESS[_-]?TOKEN|REFRESH[_-]?TOKEN|CLIENT[_-]?SECRET|PASSWORD|GITHUB_TOKEN|GH_TOKEN|NPM_TOKEN|SECRET_ACCESS_KEY)[A-Z0-9_]*|authorization)\\s*[:=]\\s*(?:bearer\\s+)?[^\\s,;]{12,}")
    )

    fun validate(command: String) {
        require(command.isNotBlank()) { "Terminal komutu boş olamaz." }
        val normalized = command.lowercase()
        require(forbidden.none { it.containsMatchIn(normalized) } && !dangerousRecursiveDelete.containsMatchIn(normalized) && !parentTraversal.containsMatchIn(normalized)) {
            "Bu komut güvenlik ilkesi nedeniyle engellendi. Kairo root kullanmaz ve sistem/uygulama verisini hedefleyen işlemleri çalıştırmaz."
        }
        require(secrets.none { it.containsMatchIn(command) }) {
            "Komutta anahtar/şifreye benzeyen düz metin algılandı. Değeri komuta yazma; kasayı kullan ve gizliyi komuta gömme."
        }
    }
}
