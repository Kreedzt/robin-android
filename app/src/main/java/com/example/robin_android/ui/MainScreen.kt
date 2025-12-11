package com.example.robin_android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.robin_android.data.GameServer
import com.example.robin_android.data.ServerRepository
import kotlinx.coroutines.launch

@Composable
fun MainScreen() {
    val repository = remember { ServerRepository() }
    val coroutineScope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSearching by remember { mutableStateOf(false) }
    var servers by remember { mutableStateOf<List<GameServer>>(emptyList()) }

    // 加载服务器数据
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            servers = repository.getServers()
        } catch (e: Exception) {
            // 处理错误
        } finally {
            isLoading = false
        }
    }

    // 搜索服务器
    suspend fun performSearch(searchQuery: String) {
        isSearching = true
        try {
            servers = if (searchQuery.isEmpty()) {
                repository.getServers()
            } else {
                repository.searchServers(searchQuery)
            }
        } catch (e: Exception) {
            // 处理错误
        } finally {
            isSearching = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        SearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {
                coroutineScope.launch {
                    performSearch(query)
                }
            },
            placeholder = "搜索服务器..."
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading || isSearching -> {
                // 加载中状态
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isLoading) "加载服务器列表..." else "搜索中...",
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
                            text = if (query.isEmpty()) "暂无服务器" else "未找到相关服务器",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (query.isNotEmpty()) {
                            Text(
                                text = "尝试其他关键词",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            else -> {
                // 显示服务器列表
                Text(
                    text = if (query.isEmpty()) "所有服务器 (${servers.size})"
                          else "搜索结果 (${servers.size})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(servers) { server ->
                        ServerRow(server)
                    }
                }
            }
        }
    }
}
