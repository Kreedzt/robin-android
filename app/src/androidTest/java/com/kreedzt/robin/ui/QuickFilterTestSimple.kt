package com.kreedzt.robin.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simplified Android UI tests for Quick Filter functionality
 */
@RunWith(AndroidJUnit4::class)
class QuickFilterTestSimple {

    @get:Rule
    val composeTestRule = createComposeRule()

    private data class QuickFilter(
        val id: String,
        val label: String
    )

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun SimpleQuickFilterScreen(
        initialMultiSelectMode: Boolean = false
    ) {
        var isMultiSelectMode by remember { mutableStateOf(initialMultiSelectMode) }

        // Use a single state to track both the active filters and their order
        var selectedFilters by remember { mutableStateOf<List<String>>(emptyList()) }

        // Debug: Log state changes
        LaunchedEffect(selectedFilters, isMultiSelectMode) {
            println("DEBUG: selectedFilters=$selectedFilters, isMultiSelectMode=$isMultiSelectMode")
        }

        val quickFilters = listOf(
            QuickFilter("invasion", "Official Invasion"),
            QuickFilter("ww2_invasion", "Official WW2 Invasion"),
            QuickFilter("dominance", "Official Dominance")
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Quick Filters with toggle
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Quick Filters",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Multi-select",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            modifier = Modifier.testTag("multi_select_switch"),
                            checked = isMultiSelectMode,
                            onCheckedChange = { enabled ->
                                println("DEBUG: Switch clicked, enabled=$enabled, current selectedFilters=$selectedFilters")

                                isMultiSelectMode = enabled

                                // If switching to single mode and multiple filters are selected, keep only the first one
                                if (!enabled && selectedFilters.size > 1) {
                                    val firstFilter = selectedFilters.first()
                                    println("DEBUG: Switching to single mode, keeping first filter: $firstFilter")
                                    selectedFilters = listOf(firstFilter)
                                }

                                println("DEBUG: After switch, selectedFilters=$selectedFilters")
                            }
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickFilters.forEach { filter ->
                        val selected = selectedFilters.contains(filter.id)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                println("DEBUG: Clicked filter ${filter.label}, selected=$selected, isMultiSelectMode=$isMultiSelectMode")
                                println("DEBUG: Before click - selectedFilters=$selectedFilters")

                                selectedFilters = if (selected) {
                                    // Deselect
                                    println("DEBUG: Deselecting filter")
                                    emptyList()
                                } else {
                                    // Select new filter
                                    if (isMultiSelectMode) {
                                        // Multi-select mode: add to existing selection
                                        println("DEBUG: Multi-select: adding to existing filters")
                                        selectedFilters + filter.id
                                    } else {
                                        // Single-select mode: only select this one
                                        println("DEBUG: Single-select: replacing with ${filter.id}")
                                        listOf(filter.id)
                                    }
                                }

                                println("DEBUG: After click - selectedFilters=$selectedFilters")
                            },
                            label = {
                                Text(text = filter.label)
                            }
                        )
                    }
                }
            }
        }
    }

    @Test
    fun quickFilter_defaultMode_isSingleSelect() {
        // Test in single-select mode by default
        composeTestRule.setContent {
            SimpleQuickFilterScreen(initialMultiSelectMode = false)
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Try to select multiple filters
        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official WW2 Invasion").performClick()
        composeTestRule.waitForIdle()

        // Verify only one filter is selected (the last one)
        composeTestRule.onNodeWithText("Official Invasion").assertIsNotSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsSelected()
    }

      @Test
    fun quickFilter_enableMultiSelect_allowsMultipleSelections() {
        // Test starting in single-select mode
        composeTestRule.setContent {
            SimpleQuickFilterScreen(initialMultiSelectMode = false)
        }

        composeTestRule.waitForIdle()

        // Enable multi-select mode - click the Switch directly using testTag
        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        // Select first filter
        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        // Verify first filter is selected
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()

        // Select second filter
        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()

        // Verify both filters are selected
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()
    }

    @Test
    fun quickFilter_switchFromMultiToSingle_keepsOnlyFirstSelection() {
        // Test starting in multi-select mode
        composeTestRule.setContent {
            SimpleQuickFilterScreen(initialMultiSelectMode = true)
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Select multiple filters in multi-select mode
        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official WW2 Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()

        // Verify all three are selected
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()

        // Switch to single-select mode - click the Switch directly using testTag
        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        // Verify only the first selected filter remains (Order of selection matters)
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsNotSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsNotSelected()
    }

    @Test
    fun quickFilter_singleClickToDeselect_worksInBothModes() {
        // Test in single-select mode
        composeTestRule.setContent {
            SimpleQuickFilterScreen(initialMultiSelectMode = false)
        }

        // Wait for composition to complete
        composeTestRule.waitForIdle()

        // Select a filter
        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()

        // Click the same filter to deselect
        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Invasion").assertIsNotSelected()

        // Enable multi-select mode - click the Switch directly using testTag
        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        // Select a filter in multi-select mode
        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()

        // Click the same filter to deselect in multi-select mode
        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Dominance").assertIsNotSelected()
    }
}