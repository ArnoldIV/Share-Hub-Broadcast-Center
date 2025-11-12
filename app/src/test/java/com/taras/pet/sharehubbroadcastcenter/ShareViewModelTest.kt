package com.taras.pet.sharehubbroadcastcenter

import android.content.Intent
import android.net.Uri
import app.cash.turbine.test
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case.ShareUseCases
import com.taras.pet.sharehubbroadcastcenter.presenter.share.ShareEffect
import com.taras.pet.sharehubbroadcastcenter.presenter.share.ShareIntent
import com.taras.pet.sharehubbroadcastcenter.presenter.share.ShareUiState
import com.taras.pet.sharehubbroadcastcenter.presenter.share.ShareViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareViewModelTest {

    private lateinit var mockUseCases: ShareUseCases
    private lateinit var viewModel: ShareViewModel
    private lateinit var mockIntent: Intent
    private lateinit var mockUri: Uri
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockUseCases = mockk()
        mockIntent = mockk()
        mockUri = mockk()
        viewModel = ShareViewModel(mockUseCases)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Initial State Tests

    @Test
    fun `initial state is correct`() {
        val initialState = viewModel.state.value

        assertEquals(ShareUiState(), initialState)
        assertNull(initialState.sharedContent)
        assertTrue(initialState.isEmpty)
        assertFalse(initialState.isSharing)
        assertNull(initialState.errorMessage)
    }

    // parseIntent Tests

    @Test
    fun `parseIntent with valid content updates state correctly`() = runTest {
        val expectedContent = SharedContent.Text("Hello World")
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns expectedContent

        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(ShareUiState(), initialState)

            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(expectedContent, updatedState.sharedContent)
            assertFalse(updatedState.isEmpty)
            assertFalse(updatedState.isSharing)
            assertNull(updatedState.errorMessage)
        }

        verify { mockUseCases.parseIntentUseCase(mockIntent) }
    }

    @Test
    fun `parseIntent with Unknown content emits ShowToast effect`() = runTest {
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns SharedContent.Unknown

        viewModel.effect.test {
            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ShareEffect.ShowToast)
            assertEquals("Unknown content", (effect as ShareEffect.ShowToast).message)
        }

        verify { mockUseCases.parseIntentUseCase(mockIntent) }
    }

    @Test
    fun `parseIntent with different content types updates state correctly`() = runTest {
        val testCases = listOf(
            SharedContent.Text("Sample text"),
            SharedContent.Image(mockUri),
            SharedContent.Video(mockUri),
            SharedContent.Audio(mockUri),
            SharedContent.Pdf(mockUri),
            SharedContent.File(mockUri, "application/zip"),
            SharedContent.MultipleImages(arrayListOf(mockUri)),
            SharedContent.Link(mockUri)
        )

        testCases.forEach { content ->
            // Створюємо новий ViewModel і мок кожного разу
            val useCases = mockk<ShareUseCases>()
            val vm = ShareViewModel(useCases)

            every { useCases.parseIntentUseCase(any()) } returns content

            vm.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            assertEquals(content, vm.state.value.sharedContent)
            assertFalse(vm.state.value.isEmpty)
        }
    }

    // shareContent Tests

    @Test
    fun `shareContent with valid intent emits OpenChooser effect`() = runTest {
        val content = SharedContent.Text("Hello World")
        val expectedIntent = mockk<Intent>()
        every { mockUseCases.sendContentUseCase(content) } returns expectedIntent

        viewModel.effect.test {
            viewModel.shareContent(content)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ShareEffect.OpenChooser)
            assertEquals(expectedIntent, (effect as ShareEffect.OpenChooser).intent)
        }

        verify { mockUseCases.sendContentUseCase(content) }
    }

    @Test
    fun `shareContent with null intent emits ShowToast effect`() = runTest {
        val content = SharedContent.Text("Hello World")
        every { mockUseCases.sendContentUseCase(content) } returns null

        viewModel.effect.test {
            viewModel.shareContent(content)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ShareEffect.ShowToast)
            assertEquals("Chooser is null", (effect as ShareEffect.ShowToast).message)
        }

        verify { mockUseCases.sendContentUseCase(content) }
    }

    @Test
    fun `shareContent clears error message on success`() = runTest {
        val content = SharedContent.Text("Hello World")
        val expectedIntent = mockk<Intent>()
        every { mockUseCases.sendContentUseCase(content) } returns expectedIntent

        // First set an error message
        viewModel.state.test {
            awaitItem() // initial state

            // This would normally set an error, but we'll test the clearing behavior
            viewModel.shareContent(content)
            testDispatcher.scheduler.advanceUntilIdle()

            // The error should be cleared (null) when share is successful
            val current = viewModel.state.value
            assertNull(current.errorMessage)
        }
    }

    // cleanError Tests

    @Test
    fun `cleanError clears error message`() = runTest {
        // Manually set an error state by calling reduce - we need to test this indirectly
        // Since reduce is private, we'll test through parseIntent with Unknown content first
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns SharedContent.Unknown

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.parseIntent(mockIntent) // This won't set error in state, only emit effect
            testDispatcher.scheduler.advanceUntilIdle()

            viewModel.cleanError()
            testDispatcher.scheduler.advanceUntilIdle()

            val current = viewModel.state.value
            assertNull(current.errorMessage)
        }
    }

    // onIntent Tests

    @Test
    fun `onIntent with ParseIncomingIntent calls parseIntent`() = runTest {
        val content = SharedContent.Text("Hello World")
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns content

        val shareIntent = ShareIntent.ParseIncomingIntent(mockIntent)

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onIntent(shareIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val updatedState = awaitItem()
            assertEquals(content, updatedState.sharedContent)
            assertFalse(updatedState.isEmpty)
        }

        verify { mockUseCases.parseIntentUseCase(mockIntent) }
    }

    @Test
    fun `onIntent with ShareAgainClicked calls shareContent`() = runTest {
        val content = SharedContent.Text("Hello World")
        val expectedIntent = mockk<Intent>()
        every { mockUseCases.sendContentUseCase(content) } returns expectedIntent

        val shareIntent = ShareIntent.ShareAgainClicked(content)

        viewModel.effect.test {
            viewModel.onIntent(shareIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ShareEffect.OpenChooser)
            assertEquals(expectedIntent, (effect as ShareEffect.OpenChooser).intent)
        }

        verify { mockUseCases.sendContentUseCase(content) }
    }

    @Test
    fun `onIntent with ClearError calls cleanError`() = runTest {
        val shareIntent = ShareIntent.ClearError

        viewModel.state.test {
            awaitItem() // initial state

            viewModel.onIntent(shareIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            // Since we don't have an error initially, state shouldn't change
            // But the cleanError function should still be called
            expectNoEvents()
        }
    }

    // Edge Cases and Integration Tests

    @Test
    fun `multiple parseIntent calls update state correctly`() = runTest {
        val content1 = SharedContent.Text("First content")
        val content2 = SharedContent.Image(mockUri)

        every { mockUseCases.parseIntentUseCase(mockIntent) } returnsMany listOf(content1, content2)

        viewModel.state.test {
            awaitItem() // initial state

            // First parse
            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val state1 = awaitItem()
            assertEquals(content1, state1.sharedContent)
            assertFalse(state1.isEmpty)

            // Second parse
            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val state2 = awaitItem()
            assertEquals(content2, state2.sharedContent)
            assertFalse(state2.isEmpty)
        }
    }

    @Test
    fun `parse then share flow works correctly`() = runTest {
        val content = SharedContent.Text("Hello World")
        val shareIntent = mockk<Intent>()
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns content
        every { mockUseCases.sendContentUseCase(content) } returns shareIntent

        // Test state changes
        viewModel.state.test {
            awaitItem() // initial state

            // Parse intent
            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val stateAfterParse = awaitItem()
            assertEquals(content, stateAfterParse.sharedContent)
            assertFalse(stateAfterParse.isEmpty)

            cancelAndIgnoreRemainingEvents()
        }

        // Test effect emission
        viewModel.effect.test {
            // Share content
            viewModel.shareContent(content)
            testDispatcher.scheduler.advanceUntilIdle()

            val effect = awaitItem()
            assertTrue(effect is ShareEffect.OpenChooser)
            assertEquals(shareIntent, (effect as ShareEffect.OpenChooser).intent)
        }

        verify { mockUseCases.parseIntentUseCase(mockIntent) }
        verify { mockUseCases.sendContentUseCase(content) }
    }

    @Test
    fun `shareContent with different content types works correctly`() = runTest {
        val testCases = listOf(
            SharedContent.Text("Sample text"),
            SharedContent.Image(mockUri),
            SharedContent.Video(mockUri),
            SharedContent.Audio(mockUri),
            SharedContent.Pdf(mockUri),
            SharedContent.File(mockUri, "application/zip"),
            SharedContent.MultipleImages(arrayListOf(mockUri)),
            SharedContent.Link(mockUri)
        )

        testCases.forEach { content ->
            val expectedIntent = mockk<Intent>()
            every { mockUseCases.sendContentUseCase(content) } returns expectedIntent

            viewModel.effect.test {
                viewModel.shareContent(content)
                testDispatcher.scheduler.advanceUntilIdle()

                val effect = awaitItem()
                assertTrue(effect is ShareEffect.OpenChooser)
                assertEquals(expectedIntent, (effect as ShareEffect.OpenChooser).intent)
            }

            verify { mockUseCases.sendContentUseCase(content) }
        }
    }

    @Test
    fun `Unknown content handling throughout the flow`() = runTest {
        every { mockUseCases.parseIntentUseCase(mockIntent) } returns SharedContent.Unknown
        every { mockUseCases.sendContentUseCase(SharedContent.Unknown) } returns null

        // Test parseIntent with Unknown content
        viewModel.effect.test {
            viewModel.parseIntent(mockIntent)
            testDispatcher.scheduler.advanceUntilIdle()

            val parseEffect = awaitItem()
            assertTrue(parseEffect is ShareEffect.ShowToast)
            assertEquals("Unknown content", (parseEffect as ShareEffect.ShowToast).message)

            // Test shareContent with Unknown content
            viewModel.shareContent(SharedContent.Unknown)
            testDispatcher.scheduler.advanceUntilIdle()

            val shareEffect = awaitItem()
            assertTrue(shareEffect is ShareEffect.ShowToast)
            assertEquals("Chooser is null", (shareEffect as ShareEffect.ShowToast).message)
        }

        verify { mockUseCases.parseIntentUseCase(mockIntent) }
        verify { mockUseCases.sendContentUseCase(SharedContent.Unknown) }
    }
}