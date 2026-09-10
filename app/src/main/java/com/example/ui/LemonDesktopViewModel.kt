package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.entity.ScriptEntity
import com.example.engine.*
import com.example.ui.theme.LemonThemes
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LemonDesktopViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.scriptDao(), database.historyDao(), database.settingDao())

    val terminalEngine: TerminalEngine = TerminalEngine(application)

    private val _currentTheme = MutableStateFlow<TerminalTheme>(LemonThemes.LemonAmberCrt)
    val currentTheme: StateFlow<TerminalTheme> = _currentTheme.asStateFlow()

    private val _crtScanlinesEnabled = MutableStateFlow(true)
    val crtScanlinesEnabled: StateFlow<Boolean> = _crtScanlinesEnabled.asStateFlow()

    private val _panelPosition = MutableStateFlow(PanelPosition.TOP)
    val panelPosition: StateFlow<PanelPosition> = _panelPosition.asStateFlow()

    private val _activeWorkspace = MutableStateFlow(1)
    val activeWorkspace: StateFlow<Int> = _activeWorkspace.asStateFlow()

    private val _windows = MutableStateFlow<List<WindowState>>(
        listOf(
            WindowState(
                id = "win_terminal_1",
                type = DesktopWindowType.TERMINAL,
                title = DesktopWindowType.TERMINAL.defaultTitle,
                workspace = 1,
                zIndex = 10f
            )
        )
    )
    val windows: StateFlow<List<WindowState>> = _windows.asStateFlow()

    private val _activeWindowId = MutableStateFlow<String?>("win_terminal_1")
    val activeWindowId: StateFlow<String?> = _activeWindowId.asStateFlow()

    private val _resourceSnapshot = MutableStateFlow(terminalEngine.getResourceSnapshot())
    val resourceSnapshot: StateFlow<SystemResourceSnapshot> = _resourceSnapshot.asStateFlow()

    val scripts: StateFlow<List<ScriptEntity>> = repository.allScripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Periodic background resource monitor updater
        viewModelScope.launch {
            while (true) {
                _resourceSnapshot.value = terminalEngine.getResourceSnapshot()
                delay(2000)
            }
        }
    }

    fun openWindow(type: DesktopWindowType) {
        val current = _windows.value
        val existing = current.find { it.type == type && it.workspace == _activeWorkspace.value }
        if (existing != null) {
            // Unminimize and bring to front
            _windows.value = current.map {
                if (it.id == existing.id) it.copy(isMinimized = false) else it
            }
            focusWindow(existing.id)
            return
        }

        val newId = "win_${type.name.lowercase()}_${System.currentTimeMillis() % 10000}"
        val newWindow = WindowState(
            id = newId,
            type = type,
            title = type.defaultTitle,
            workspace = _activeWorkspace.value,
            zIndex = (current.maxOfOrNull { it.zIndex } ?: 0f) + 1f
        )
        _windows.value = current + newWindow
        _activeWindowId.value = newId
    }

    fun closeWindow(id: String) {
        _windows.value = _windows.value.filter { it.id != id }
        if (_activeWindowId.value == id) {
            _activeWindowId.value = _windows.value.filter { it.workspace == _activeWorkspace.value }.lastOrNull()?.id
        }
    }

    fun minimizeWindow(id: String) {
        _windows.value = _windows.value.map {
            if (it.id == id) it.copy(isMinimized = true) else it
        }
        if (_activeWindowId.value == id) {
            _activeWindowId.value = _windows.value.filter { it.workspace == _activeWorkspace.value && !it.isMinimized }.lastOrNull()?.id
        }
    }

    fun toggleMaximizeWindow(id: String) {
        _windows.value = _windows.value.map {
            if (it.id == id) it.copy(isMaximized = !it.isMaximized) else it
        }
        focusWindow(id)
    }

    fun focusWindow(id: String) {
        _windows.value = _windows.value.map {
            if (it.id == id) it.copy(isMinimized = false) else it
        }
        _activeWindowId.value = id
    }

    fun switchWorkspace(ws: Int) {
        _activeWorkspace.value = ws
        val topInWs = _windows.value.filter { it.workspace == ws && !it.isMinimized }.lastOrNull()
        _activeWindowId.value = topInWs?.id
    }

    fun cycleNextTheme() {
        val themes = LemonThemes.allThemes
        val currentIndex = themes.indexOfFirst { it.id == _currentTheme.value.id }
        val nextIndex = (currentIndex + 1) % themes.size
        _currentTheme.value = themes[nextIndex]
    }

    fun selectTheme(theme: TerminalTheme) {
        _currentTheme.value = theme
    }

    fun setPanelPosition(pos: PanelPosition) {
        _panelPosition.value = pos
    }

    fun toggleCrtScanlines(enabled: Boolean) {
        _crtScanlinesEnabled.value = enabled
    }

    fun runScriptInTerminal(script: ScriptEntity) {
        // Ensure terminal window is open on active workspace
        openWindow(DesktopWindowType.TERMINAL)
        viewModelScope.launch {
            repository.recordScriptRun(script.id)
            if (script.type == "bash") {
                terminalEngine.executeCommand("sh -c \"${script.name}\"")
                script.code.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                        terminalEngine.executeCommand(trimmed)
                    }
                }
            } else {
                terminalEngine.executeCommand("python -c \"${script.code.replace("\n", "; ")}\"")
            }
        }
    }

    fun saveScript(script: ScriptEntity) {
        viewModelScope.launch {
            if (script.id == 0L) {
                repository.saveScript(script)
            } else {
                repository.updateScript(script)
            }
        }
    }

    fun deleteScript(script: ScriptEntity) {
        viewModelScope.launch {
            repository.deleteScript(script)
        }
    }

    fun toggleFavorite(script: ScriptEntity) {
        viewModelScope.launch {
            repository.updateScript(script.copy(isFavorite = !script.isFavorite))
        }
    }

    fun dropCaches() {
        terminalEngine.executeCommand("drop_caches")
        _resourceSnapshot.value = terminalEngine.getResourceSnapshot()
    }

    fun killProcess(pid: Int) {
        terminalEngine.executeCommand("kill $pid")
        _resourceSnapshot.value = terminalEngine.getResourceSnapshot()
    }
}
