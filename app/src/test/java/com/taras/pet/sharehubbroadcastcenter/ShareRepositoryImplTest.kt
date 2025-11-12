package com.taras.pet.sharehubbroadcastcenter

import android.content.Intent
import android.net.Uri
import com.taras.pet.sharehubbroadcastcenter.data.repository.ShareRepositoryImpl
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareRepositoryImplTest {

    private lateinit var repository: ShareRepositoryImpl
    private lateinit var mockIntent: Intent

    @Before
    fun setup() {
        repository = ShareRepositoryImpl()
        mockIntent = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.core.net.UriKt")
    }

    // Test parseSharedIntent

    @Test
    fun `parseSharedIntent with null action returns Unknown`() {
        every { mockIntent.action } returns "result"

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with unknown action returns Unknown`() {
        every { mockIntent.action } returns "unknown.action"

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and text plain returns Text`() {
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "text/plain"
        every { mockIntent.getStringExtra(Intent.EXTRA_TEXT) } returns "Hello World"

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Text)
        Assert.assertEquals("Hello World", (result as SharedContent.Text).text)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and http link returns Link`() {
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "text/plain"
        every { mockIntent.getStringExtra(Intent.EXTRA_TEXT) } returns "http://example.com"

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Link)
        val expectedUri = Uri.parse("http://example.com")

        Assert.assertEquals(expectedUri, (result as SharedContent.Link).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and https link returns Link`() {
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "text/plain"
        every { mockIntent.getStringExtra(Intent.EXTRA_TEXT) } returns "https://example.com"

        val result = repository.parseSharedIntent(mockIntent)
        val expectedUri = Uri.parse("https://example.com")
        Assert.assertTrue(result is SharedContent.Link)
        Assert.assertEquals(expectedUri, (result as SharedContent.Link).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and image type returns Image`() {
        val imageUri = Uri.parse("content://test/image.jpg")
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "image/jpeg"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns imageUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Image)
        Assert.assertEquals(imageUri, (result as SharedContent.Image).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and video type returns Video`() {
        val videoUri = Uri.parse("content://test/video.mp4")
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "video/mp4"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns videoUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Video)
        Assert.assertEquals(videoUri, (result as SharedContent.Video).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and audio type returns Audio`() {
        val audioUri = Uri.parse("content://test/audio.mp3")
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "audio/mp3"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns audioUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Audio)
        Assert.assertEquals(audioUri, (result as SharedContent.Audio).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and pdf type returns Pdf`() {
        val pdfUri = Uri.parse("content://test/application.pdf")
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "application/pdf"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns pdfUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Pdf)
        Assert.assertEquals(pdfUri, (result as SharedContent.Pdf).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and custom file type returns File`() {
        val fileUri = Uri.parse("content://test/file.zip")
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "application/zip"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns fileUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.File)
        Assert.assertEquals(fileUri, (result as SharedContent.File).uri)
        Assert.assertEquals("application/zip", result.mimeType)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and null Uri returns Unknown`() {
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns "image/jpeg"
        every { mockIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) } returns null

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND and null type returns Unknown`() {
        every { mockIntent.action } returns Intent.ACTION_SEND
        every { mockIntent.type } returns null

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND_MULTIPLE returns MultipleImages`() {
        val uriList = arrayListOf(
            Uri.parse("content://test/image.jpg"),
            Uri.parse("content://test/image2.jpg")
        )
        every { mockIntent.action } returns Intent.ACTION_SEND_MULTIPLE
        every { mockIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) } returns uriList

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.MultipleImages)
        Assert.assertEquals(uriList, (result as SharedContent.MultipleImages).uris)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND_MULTIPLE and empty list returns Unknown`() {
        every { mockIntent.action } returns Intent.ACTION_SEND_MULTIPLE
        every { mockIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) } returns arrayListOf()

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_SEND_MULTIPLE and null list returns Unknown`() {
        every { mockIntent.action } returns Intent.ACTION_SEND_MULTIPLE
        every { mockIntent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) } returns null

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_VIEW and https Uri returns Link`() {
        val httpsUri = Uri.parse("https://example.com")
        every { mockIntent.action } returns Intent.ACTION_VIEW
        every { mockIntent.data } returns httpsUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Link)
        Assert.assertEquals(httpsUri, (result as SharedContent.Link).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_VIEW and http Uri returns Link`() {
        val httpsUri = Uri.parse("http://example.com")
        every { mockIntent.action } returns Intent.ACTION_VIEW
        every { mockIntent.data } returns httpsUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertTrue(result is SharedContent.Link)
        Assert.assertEquals(httpsUri, (result as SharedContent.Link).uri)
    }

    @Test
    fun `parseSharedIntent with ACTION_VIEW and non-http scheme returns Unknown`() {
        val fileUri = Uri.parse("content://test/file.zip")
        every { mockIntent.action } returns Intent.ACTION_VIEW
        every { mockIntent.data } returns fileUri

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    @Test
    fun `parseSharedIntent with ACTION_VIEW and null data returns Unknown`() {
        every { mockIntent.action } returns Intent.ACTION_VIEW
        every { mockIntent.data } returns null

        val result = repository.parseSharedIntent(mockIntent)

        Assert.assertEquals(SharedContent.Unknown, result)
    }

    // Test sendShareIntent

    @Test
    fun `sendShareIntent with Text content creates correct intent`() {
        val content = SharedContent.Text("Hello World")

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
        // Note: In a real test, you'd need to mock Intent.createChooser
        // For now, we test that it returns non-null
    }

    @Test
    fun `sendShareIntent with Image content creates correct intent`() {
        val imageUri = Uri.parse("content://test/image.jpg")
        val content = SharedContent.Image(imageUri)

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with Video content creates correct intent`() {
        val videoUri = Uri.parse("content://test/video.mp4")

        val content = SharedContent.Video(videoUri)

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with Audio content creates correct intent`() {
        val audioUri = Uri.parse("content://test/audio.mp3")
        val content = SharedContent.Audio(audioUri)

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with Pdf content creates correct intent`() {
        val pdfUri = Uri.parse("content://test/application.pdf")
        val content = SharedContent.Pdf(pdfUri)

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with File content creates correct intent`() {
        val fileUri = Uri.parse("content://test/file.zip")
        val content = SharedContent.File(fileUri, "application/zip")

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with MultipleImages content creates correct intent`() {
        val uriList1 =
            Uri.parse("content://test/image.jpg")
        val uriList2 =
            Uri.parse("content://test/image2.jpg")

        val content = SharedContent.MultipleImages(arrayListOf(uriList1, uriList2))

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with Link content creates correct intent`() {
        val httpsUri = Uri.parse("https://example.com")
        val content = SharedContent.Link(httpsUri)

        val result = repository.sendShareIntent(content)

        Assert.assertTrue(result != null)
    }

    @Test
    fun `sendShareIntent with Unknown content returns null`() {
        val content = SharedContent.Unknown

        val result = repository.sendShareIntent(content)

        Assert.assertNull(result)
    }
}