package com.kreedzt.robin.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kreedzt.robin.R
import com.kreedzt.robin.data.GameServer
import com.kreedzt.robin.data.ServerRepository
import com.kreedzt.robin.data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlin.text.Regex

private data class QuickFilter(
    val id: String,
    val label: String,
    val predicate: (GameServer) -> Boolean
)

@OptIn(FlowPreview::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(repository: ServerRepository? = null) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    val repo = remember(repository) { repository ?: ServerRepository(settingsManager, context) }

    val initialQuery = remember { settingsManager.lastSearchQuery }
    val initialFilters = remember { settingsManager.lastQuickFilters }

    var query by remember { mutableStateOf(initialQuery) }
    var isLoading by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var autoRefreshEnabled by remember { mutableStateOf(false) }
    var servers by remember { mutableStateOf<List<GameServer>>(emptyList()) }
    var allServers by remember { mutableStateOf<List<GameServer>>(emptyList()) }
    var lastUpdateTime by remember { mutableStateOf(0L) }
    var activeFilters by remember { mutableStateOf(initialFilters) }
    var countdownSeconds by remember { mutableStateOf(5) }
    var isMultiSelectMode by remember { mutableStateOf(settingsManager.isQuickFilterMultiSelectMode) }

    // 创建搜索查询的StateFlow用于防抖处理
    val searchQueryFlow = remember { MutableStateFlow(initialQuery) }

    val castlingRegex = remember { Regex("""^\[Castling](\[Global])?\[[\w!\\?]+(-\d)?\s(LV\d|FOV)]""") }
    val helldiversRegex = remember { Regex("""^\[地狱潜兵]""") }
    val quickFilters = listOf(
        QuickFilter(
            id = "invasion",
            label = stringResource(R.string.app_filter_official_invasion),
            predicate = { it.realm == "official_invasion" }
        ),
        QuickFilter(
            id = "ww2_invasion",
            label = stringResource(R.string.app_filter_official_ww2_invasion),
            predicate = { it.realm == "official_pacific" }
        ),
        QuickFilter(
            id = "dominance",
            label = stringResource(R.string.app_filter_official_dominance),
            predicate = { it.realm == "official_dominance" }
        ),
        QuickFilter(
            id = "castling",
            label = stringResource(R.string.app_filter_official_mod_castling),
            predicate = {
                it.mode.lowercase().contains("castling") && castlingRegex.containsMatchIn(it.name)
            }
        ),
        QuickFilter(
            id = "helldivers",
            label = stringResource(R.string.app_filter_official_mod_helldivers),
            predicate = {
                it.mode.lowercase().contains("hd") && helldiversRegex.containsMatchIn(it.name)
            }
        )
    )

    fun applyActiveFilters(base: List<GameServer>, filterIds: Set<String>): List<GameServer> {
        if (filterIds.isEmpty()) return base
        val selectedFilters = quickFilters.filter { filterIds.contains(it.id) }
        if (selectedFilters.isEmpty()) return base
        return base.filter { server -> selectedFilters.any { it.predicate(server) } }
    }

    // 初始加载数据
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val loaded = repo.getServers()
            allServers = loaded

            // 在单选模式下，如果有多个选择，只保留第一个
            val filtersToApply = if (!isMultiSelectMode && activeFilters.size > 1) {
                setOf(activeFilters.first())
            } else {
                activeFilters
            }
            activeFilters = filtersToApply

            servers = applyActiveFilters(loaded, activeFilters)
            lastUpdateTime = System.currentTimeMillis()
        } catch (e: Exception) {
            // 处理错误
        } finally {
            isLoading = false
        }
    }

    // 实时防抖搜索 - 只在本地数据进行筛选
    LaunchedEffect(Unit) {
        searchQueryFlow
            .debounce(300) // 300ms防抖延迟
            .distinctUntilChanged() // 只有当查询真正改变时才处理
            .flowOn(Dispatchers.Default)
            .collect { searchQuery ->
                if (searchQuery != query) {
                    return@collect // 如果与当前查询不匹配，跳过
                }

                isSearching = true
                try {
                    // 只在本地数据进行搜索，不触发网络请求
                    val result = if (searchQuery.isEmpty()) {
                        // 如果搜索为空，使用当前完整的服务器列表
                        allServers
                    } else {
                        // 在现有数据中进行搜索筛选
                        repo.searchServersInLocalData(allServers, searchQuery)
                    }
                    servers = applyActiveFilters(result, activeFilters)
                } catch (e: Exception) {
                    // 处理错误，可以保持之前的搜索结果
                } finally {
                    isSearching = false
                }
            }
    }

    // 自动刷新逻辑
    LaunchedEffect(autoRefreshEnabled, activeFilters) {
        if (!autoRefreshEnabled) {
            countdownSeconds = 5
            return@LaunchedEffect
        }

        while (autoRefreshEnabled) {
            for (sec in 5 downTo 1) {
                countdownSeconds = sec
                delay(1000)
                if (!autoRefreshEnabled) break
            }
            if (!autoRefreshEnabled) break

            isRefreshing = true
            try {
                val refreshed = repo.getServers(forceRefresh = false)
                allServers = refreshed
                servers = applyActiveFilters(refreshed, activeFilters)
                lastUpdateTime = System.currentTimeMillis()
            } catch (e: Exception) {
                // 静默处理自动刷新错误
            } finally {
                isRefreshing = false
                countdownSeconds = 5
            }
        }
    }

    
    val topPadding = WindowInsets.systemBars.only(WindowInsetsSides.Top).asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 0.dp)
                .padding(top = topPadding)
        ) {
        SearchBar(
            query = query,
            onQueryChange = { newQuery ->
                query = newQuery
                settingsManager.lastSearchQuery = newQuery
                // 更新搜索查询Flow，触发防抖搜索
                searchQueryFlow.value = newQuery
            },
            onSearch = {
                // 保持onSearch为空，因为已经在onQueryChange中处理了
            },
            placeholder = stringResource(R.string.search_placeholder)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 快捷筛选
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.app_quick_filters),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.multi_select),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = isMultiSelectMode,
                        onCheckedChange = { enabled ->
                            isMultiSelectMode = enabled
                            settingsManager.isQuickFilterMultiSelectMode = enabled

                            // 如果切换到单选模式且当前有多个选择，只保留第一个
                            if (!enabled && activeFilters.size > 1) {
                                val firstFilter = activeFilters.first()
                                activeFilters = setOf(firstFilter)
                                settingsManager.lastQuickFilters = activeFilters
                                servers = applyActiveFilters(allServers, activeFilters)
                            }
                        }
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickFilters.forEach { filter ->
                    val selected = activeFilters.contains(filter.id)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            activeFilters = if (selected) {
                                // 取消选择
                                emptySet()
                            } else {
                                // 选择新的筛选器
                                if (isMultiSelectMode) {
                                    // 多选模式：添加到现有选择
                                    activeFilters + filter.id
                                } else {
                                    // 单选模式：只选择这一个
                                    setOf(filter.id)
                                }
                            }
                            settingsManager.lastQuickFilters = activeFilters
                            servers = applyActiveFilters(allServers, activeFilters)
                        },
                        label = {
                            Text(text = filter.label)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 自动刷新开关与状态 + 倒计时
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.auto_refresh),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = autoRefreshEnabled,
                    onCheckedChange = { enabled ->
                        autoRefreshEnabled = enabled
                        if (!enabled) countdownSeconds = 5
                    }
                )
            }
            Text(
                text = when {
                    isRefreshing -> stringResource(R.string.refreshing_data)
                    autoRefreshEnabled -> stringResource(R.string.auto_refresh_countdown, countdownSeconds)
                    else -> stringResource(R.string.auto_refresh_disabled)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading || isSearching -> {
                // 加载中状态
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isLoading) stringResource(R.string.loading_servers) else stringResource(R.string.searching),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            isRefreshing && servers.isEmpty() -> {
                // 刷新中状态（如果没有现有数据）
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.refreshing_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            servers.isEmpty() -> {
                // 空状态
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (query.isEmpty()) stringResource(R.string.no_servers) else stringResource(R.string.no_search_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (query.isNotEmpty()) {
                            Text(
                                text = stringResource(R.string.try_other_keywords),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                val displayedTotalPlayers = remember(servers) {
                    servers.sumOf { it.currentPlayers }
                }
                val totalPlayersText = stringResource(R.string.total_players, displayedTotalPlayers)
                // 显示服务器列表
                Text(
                    text = if (query.isEmpty())
                        "${stringResource(R.string.all_servers, servers.size)} · $totalPlayersText"
                    else
                        "${stringResource(R.string.search_results, servers.size)} · $totalPlayersText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    items(servers) { server ->
                        ServerRow(server, query)
                    }
                }
            }
        }
        }
        // 悬浮手动刷新按钮，避免挤占列表空间
        val infiniteTransition = rememberInfiniteTransition()
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        ExtendedFloatingActionButton(
            text = { Text(text = stringResource(R.string.refresh)) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.refresh),
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(if (isRefreshing) rotation else 0f)
                )
            },
            onClick = {
                if (isRefreshing || isLoading) return@ExtendedFloatingActionButton
                coroutineScope.launch {
                    isRefreshing = true
                    try {
                        // 刷新服务器数据
                        val refreshed = repo.refreshServers()
                        allServers = refreshed

                        // 如果当前有搜索查询，需要重新应用搜索
                        val filteredResult = if (query.isEmpty()) {
                            refreshed
                        } else {
                            repo.searchServersInLocalData(refreshed, query)
                        }

                        servers = applyActiveFilters(filteredResult, activeFilters)
                        lastUpdateTime = System.currentTimeMillis()
                        countdownSeconds = 5
                    } catch (e: Exception) {
                        // 处理错误，可以添加用户提示
                        e.printStackTrace()
                    } finally {
                        isRefreshing = false
                    }
                }
            },
            expanded = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}
