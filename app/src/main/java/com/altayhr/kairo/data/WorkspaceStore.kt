package com.altayhr.kairo.data

import android.content.Context
import org.json.JSONArray
import java.io.File
import java.net.URLConnection

/** Local, offline-first metadata store. Workspace bytes live in app-private filesDir. */
class WorkspaceStore(context: Context) {
    private val prefs = context.getSharedPreferences("kairo_workspace_v1", Context.MODE_PRIVATE)
    private val appFiles = context.filesDir
    private val root = File(appFiles, "workspaces").apply { mkdirs() }
    private val projectRoot = File(appFiles, "projects").apply { mkdirs() }

    @Synchronized fun getChats(): List<ChatSession> = try {
        val a = JSONArray(prefs.getString("chats", "[]"))
        (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(ChatSession::fromJson) }
    } catch (_: Exception) { emptyList() }

    @Synchronized fun saveChats(chats: List<ChatSession>) {
        prefs.edit().putString("chats", JSONArray().also { a -> chats.forEach { a.put(it.toJson()) } }.toString()).apply()
    }

    @Synchronized fun getProjects(): List<WorkspaceProject> = try {
        val a = JSONArray(prefs.getString("projects", "[]"))
        (0 until a.length()).mapNotNull { a.optJSONObject(it)?.let(WorkspaceProject::fromJson) }
    } catch (_: Exception) { emptyList() }

    @Synchronized fun saveProjects(projects: List<WorkspaceProject>) {
        prefs.edit().putString("projects", JSONArray().also { a -> projects.forEach { a.put(it.toJson()) } }.toString()).apply()
    }

    fun workspaceFolder(chatId: String): File = File(root, chatId).apply { mkdirs() }
    fun projectFolder(projectId: String): File = File(projectRoot, projectId).apply { mkdirs() }

    /** Copies a file into an isolated conversation workspace. */
    fun storeFile(chatId: String, displayName: String, mimeType: String, bytes: ByteArray, origin: String): WorkspaceFile =
        storeAt(workspaceFolder(chatId), "workspaces/$chatId", displayName, mimeType, bytes, origin)

    /** Project files remain distinct from every individual conversation workspace. */
    fun storeProjectFile(projectId: String, displayName: String, mimeType: String, bytes: ByteArray): WorkspaceFile =
        storeAt(projectFolder(projectId), "projects/$projectId", displayName, mimeType, bytes, "project")

    private fun storeAt(folder: File, relativePrefix: String, displayName: String, mimeType: String, bytes: ByteArray, origin: String): WorkspaceFile {
        val safe = displayName.replace(Regex("[^A-Za-z0-9._() -]"), "_").trim().ifBlank { "dosya" }.take(120)
        var candidate = safe
        var n = 2
        while (File(folder, candidate).exists()) {
            val dot = safe.lastIndexOf('.')
            candidate = if (dot > 0) "${safe.substring(0, dot)}-$n${safe.substring(dot)}" else "$safe-$n"
            n++
        }
        File(folder, candidate).writeBytes(bytes)
        return WorkspaceFile(name = candidate, mimeType = mimeType, size = bytes.size.toLong(), relativePath = "$relativePrefix/$candidate", origin = origin)
    }

    fun readFile(file: WorkspaceFile): File = File(appFiles, file.relativePath)

    /** Re-indexes safe project/chat files after a terminal command without copying or deleting bytes. */
    @Synchronized fun refreshTerminalWorkspace(chatId: String) {
        val chats = getChats().toMutableList()
        val index = chats.indexOfFirst { it.id == chatId }
        val chat = chats.getOrNull(index)
        if (chat?.projectId != null) {
            val projects = getProjects().toMutableList()
            val projectIndex = projects.indexOfFirst { it.id == chat.projectId }
            val project = projects.getOrNull(projectIndex) ?: return
            projects[projectIndex] = project.copy(
                files = scanFiles(projectFolder(project.id), "projects/${project.id}", project.files, "terminal-project" )
            )
            saveProjects(projects)
        } else {
            val fileChatId = chat?.id ?: chatId
            if (fileChatId.isBlank()) return
            val existing = chat?.files.orEmpty()
            val updatedFiles = scanFiles(workspaceFolder(fileChatId), "workspaces/$fileChatId", existing, "terminal-chat")
            if (index >= 0) {
                chats[index] = chat!!.copy(files = updatedFiles, updatedAt = System.currentTimeMillis())
                saveChats(chats)
            }
        }
    }

    private fun scanFiles(folder: File, relativePrefix: String, existing: List<WorkspaceFile>, origin: String): List<WorkspaceFile> {
        val rootCanonical = runCatching { folder.canonicalFile }.getOrNull() ?: return existing
        val oldByPath = existing.associateBy { it.relativePath }
        val ignoredDirs = setOf(".git", "node_modules", ".gradle", "build", "dist", "target", ".expo", ".next", ".cache", ".venv", "venv", "__pycache__", ".idea", ".kotlin")
        val result = ArrayList<WorkspaceFile>()
        folder.walkTopDown().onEnter { dir -> dir == folder || dir.name !in ignoredDirs }.forEach { file ->
            if (result.size >= 3000 || !file.isFile) return@forEach
            val canonical = runCatching { file.canonicalFile }.getOrNull() ?: return@forEach
            if (!canonical.toPath().startsWith(rootCanonical.toPath()) || file.length() > 150L * 1024 * 1024) return@forEach
            val rel = "$relativePrefix/${file.relativeTo(folder).invariantSeparatorsPath}"
            val previous = oldByPath[rel]
            result += WorkspaceFile(
                id = previous?.id ?: java.util.UUID.randomUUID().toString(),
                name = file.name,
                mimeType = URLConnection.guessContentTypeFromName(file.name) ?: previous?.mimeType ?: "application/octet-stream",
                size = file.length(), relativePath = rel, origin = previous?.origin ?: origin,
                createdAt = previous?.createdAt ?: file.lastModified()
            )
        }
        return result.sortedBy { it.relativePath.lowercase() }
    }

    fun deleteChatFiles(chatId: String) {
        runCatching { workspaceFolder(chatId).deleteRecursively() }
    }

    fun deleteWorkspaceFile(file: WorkspaceFile) {
        runCatching { readFile(file).delete() }
    }

    fun deleteProjectFiles(projectId: String) {
        runCatching { projectFolder(projectId).deleteRecursively() }
    }
}
