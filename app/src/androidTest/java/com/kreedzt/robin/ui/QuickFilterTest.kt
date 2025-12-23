package com.kreedzt.robin.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android UI tests for Quick Filter functionality
 */
@RunWith(AndroidJUnit4::class)
class QuickFilterTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private data class QuickFilter(
        val id: String,
        val label: String
    )

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun QuickFilterScreen(
        initialMultiSelectMode: Boolean = false
    ) {
        var isMultiSelectMode by remember { mutableStateOf(initialMultiSelectMode) }
        var selectedFilters by remember { mutableStateOf<List<String>>(emptyList()) }

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
                                isMultiSelectMode = enabled
                                if (!enabled && selectedFilters.size > 1) {
                                    selectedFilters = listOf(selectedFilters.first())
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
                        val selected = selectedFilters.contains(filter.id)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedFilters = if (selected) {
                                    emptyList()
                                } else {
                                    if (isMultiSelectMode) {
                                        selectedFilters + filter.id
                                    } else {
                                        listOf(filter.id)
                                    }
                                }
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
        composeTestRule.setContent {
            QuickFilterScreen(initialMultiSelectMode = false)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official WW2 Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").assertIsNotSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsSelected()
    }

    @Test
    fun quickFilter_enableMultiSelect_allowsMultipleSelections() {
        composeTestRule.setContent {
            QuickFilterScreen(initialMultiSelectMode = false)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()

        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()
    }

    @Test
    fun quickFilter_switchFromMultiToSingle_keepsOnlyFirstSelection() {
        composeTestRule.setContent {
            QuickFilterScreen(initialMultiSelectMode = true)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official WW2 Invasion").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()

        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()
        composeTestRule.onNodeWithText("Official WW2 Invasion").assertIsNotSelected()
        composeTestRule.onNodeWithText("Official Dominance").assertIsNotSelected()
    }

    @Test
    fun quickFilter_singleClickToDeselect_worksInBothModes() {
        composeTestRule.setContent {
            QuickFilterScreen(initialMultiSelectMode = false)
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Invasion").assertIsSelected()

        composeTestRule.onNodeWithText("Official Invasion").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Invasion").assertIsNotSelected()

        composeTestRule.onNode(hasTestTag("multi_select_switch")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Dominance").assertIsSelected()

        composeTestRule.onNodeWithText("Official Dominance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Official Dominance").assertIsNotSelected()
    }
}
