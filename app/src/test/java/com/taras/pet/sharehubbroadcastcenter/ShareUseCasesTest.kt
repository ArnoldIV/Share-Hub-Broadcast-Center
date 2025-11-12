package com.taras.pet.sharehubbroadcastcenter

import android.content.Intent
import android.net.Uri
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case.ParseShareIntentUseCase
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case.SendShareContentUseCase
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case.ShareUseCases
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class ShareUseCasesTest {

    private lateinit var mockRepository: ShareRepository
    private lateinit var parseShareIntentUseCase: ParseShareIntentUseCase
    private lateinit var sendShareContentUseCase: SendShareContentUseCase
    private lateinit var shareUseCases: ShareUseCases
    private lateinit var mockIntent: Intent
    private lateinit var mockUri: Uri

    @Before
    fun setup() {
        mockRepository = mockk()
        parseShareIntentUseCase = ParseShareIntentUseCase(mockRepository)
        sendShareContentUseCase = SendShareContentUseCase(mockRepository)
        shareUseCases = ShareUseCases(
            parseIntentUseCase = parseShareIntentUseCase,
            sendContentUseCase = sendShareContentUseCase
        )
        mockIntent = mockk()
        mockUri = mockk()
    }

    // ParseShareIntentUseCase Tests

    @Test
    fun `parseShareIntentUseCase delegates to repository parseSharedIntent`() {
        val expectedContent = SharedContent.Text("Hello World")
        every { mockRepository.parseSharedIntent(mockIntent) } returns expectedContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(expectedContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Unknown for invalid intent`() {
        every { mockRepository.parseSharedIntent(mockIntent) } returns SharedContent.Unknown

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(SharedContent.Unknown, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Text content for text intent`() {
        val textContent = SharedContent.Text("Sample text")
        every { mockRepository.parseSharedIntent(mockIntent) } returns textContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(textContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Image content for image intent`() {
        val imageContent = SharedContent.Image(mockUri)
        every { mockRepository.parseSharedIntent(mockIntent) } returns imageContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(imageContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Video content for video intent`() {
        val videoContent = SharedContent.Video(mockUri)
        every { mockRepository.parseSharedIntent(mockIntent) } returns videoContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(videoContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Audio content for audio intent`() {
        val audioContent = SharedContent.Audio(mockUri)
        every { mockRepository.parseSharedIntent(mockIntent) } returns audioContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(audioContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Pdf content for pdf intent`() {
        val pdfContent = SharedContent.Pdf(mockUri)
        every { mockRepository.parseSharedIntent(mockIntent) } returns pdfContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(pdfContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns File content for generic file intent`() {
        val fileContent = SharedContent.File(mockUri, "application/zip")
        every { mockRepository.parseSharedIntent(mockIntent) } returns fileContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(fileContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns MultipleImages content for multiple images intent`() {
        val multipleImagesContent = SharedContent.MultipleImages(arrayListOf(mockUri, mockUri))
        every { mockRepository.parseSharedIntent(mockIntent) } returns multipleImagesContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(multipleImagesContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `parseShareIntentUseCase returns Link content for link intent`() {
        val linkContent = SharedContent.Link(mockUri)
        every { mockRepository.parseSharedIntent(mockIntent) } returns linkContent

        val result = parseShareIntentUseCase(mockIntent)

        assertEquals(linkContent, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    // SendShareContentUseCase Tests

    @Test
    fun `sendShareContentUseCase delegates to repository sendShareIntent`() {
        val content = SharedContent.Text("Hello World")
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns null for Unknown content`() {
        val content = SharedContent.Unknown
        every { mockRepository.sendShareIntent(content) } returns null

        val result = sendShareContentUseCase(content)

        assertNull(result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Text content`() {
        val content = SharedContent.Text("Sample text")
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Image content`() {
        val content = SharedContent.Image(mockUri)
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Video content`() {
        val content = SharedContent.Video(mockUri)
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Audio content`() {
        val content = SharedContent.Audio(mockUri)
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Pdf content`() {
        val content = SharedContent.Pdf(mockUri)
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for File content`() {
        val content = SharedContent.File(mockUri, "application/zip")
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for MultipleImages content`() {
        val content = SharedContent.MultipleImages(arrayListOf(mockUri, mockUri))
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `sendShareContentUseCase returns intent for Link content`() {
        val content = SharedContent.Link(mockUri)
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = sendShareContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    // ShareUseCases Integration Tests

    @Test
    fun `shareUseCases contains correct use case instances`() {
        assertEquals(parseShareIntentUseCase, shareUseCases.parseIntentUseCase)
        assertEquals(sendShareContentUseCase, shareUseCases.sendContentUseCase)
    }

    @Test
    fun `shareUseCases parseIntentUseCase works correctly`() {
        val content = SharedContent.Text("Test content")
        every { mockRepository.parseSharedIntent(mockIntent) } returns content

        val result = shareUseCases.parseIntentUseCase(mockIntent)

        assertEquals(content, result)
        verify { mockRepository.parseSharedIntent(mockIntent) }
    }

    @Test
    fun `shareUseCases sendContentUseCase works correctly`() {
        val content = SharedContent.Text("Test content")
        val expectedIntent = mockk<Intent>()
        every { mockRepository.sendShareIntent(content) } returns expectedIntent

        val result = shareUseCases.sendContentUseCase(content)

        assertEquals(expectedIntent, result)
        verify { mockRepository.sendShareIntent(content) }
    }

    @Test
    fun `shareUseCases complete flow from parsing to sending`() {
        // Setup: parse intent returns content, sending content returns intent
        val parsedContent = SharedContent.Text("Parsed content")
        val shareIntent = mockk<Intent>()
        every { mockRepository.parseSharedIntent(mockIntent) } returns parsedContent
        every { mockRepository.sendShareIntent(parsedContent) } returns shareIntent

        // Execute: parse intent first, then send the parsed content
        val parseResult = shareUseCases.parseIntentUseCase(mockIntent)
        val sendResult = shareUseCases.sendContentUseCase(parseResult)

        // Verify: both operations work correctly
        assertEquals(parsedContent, parseResult)
        assertEquals(shareIntent, sendResult)
        verify { mockRepository.parseSharedIntent(mockIntent) }
        verify { mockRepository.sendShareIntent(parsedContent) }
    }

    @Test
    fun `shareUseCases handles Unknown content correctly in complete flow`() {
        // Setup: parse intent returns Unknown, sending Unknown returns null
        every { mockRepository.parseSharedIntent(mockIntent) } returns SharedContent.Unknown
        every { mockRepository.sendShareIntent(SharedContent.Unknown) } returns null

        // Execute: parse intent first, then try to send the Unknown content
        val parseResult = shareUseCases.parseIntentUseCase(mockIntent)
        val sendResult = shareUseCases.sendContentUseCase(parseResult)

        // Verify: parse returns Unknown, send returns null
        assertEquals(SharedContent.Unknown, parseResult)
        assertNull(sendResult)
        verify { mockRepository.parseSharedIntent(mockIntent) }
        verify { mockRepository.sendShareIntent(SharedContent.Unknown) }
    }
}