package org.example.project

import presentation.UiState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UiStateTest {

    @Test
    fun `Loading is the initial state type`() {
        val state: UiState<List<String>> = UiState.Loading
        assertIs<UiState.Loading>(state)
    }

    @Test
    fun `Success wraps data correctly`() {
        val data = listOf("a", "b", "c")
        val state = UiState.Success(data)
        assertIs<UiState.Success<List<String>>>(state)
        assertEquals(data, state.data)
    }

    @Test
    fun `Error holds the message`() {
        val state = UiState.Error("network failure")
        assertIs<UiState.Error>(state)
        assertEquals("network failure", state.message)
    }

    @Test
    fun `Success with null data is valid`() {
        val state: UiState<String?> = UiState.Success(null)
        assertIs<UiState.Success<String?>>(state)
        assertEquals(null, state.data)
    }
}
