package com.github.shingyx.youtubedlgui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class HelpersKtTest {

    @Nested
    inner class IsValidUrl {

        @Test
        fun google() {
            assertTrue(isValidUrl("https://www.google.com/"))
        }

        @Test
        fun youtube() {
            assertTrue(isValidUrl("https://www.youtube.com/"))
        }

        @Test
        fun youtubeVideo() {
            assertTrue(isValidUrl("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        }

        @Test
        fun youtubeVideoShareLink() {
            assertTrue(isValidUrl("https://youtu.be/dQw4w9WgXcQ"))
        }

        @Test
        fun helloWorld() {
            assertFalse(isValidUrl("Hello world"))
        }

        @Test
        fun emptyString() {
            assertFalse(isValidUrl(""))
        }

        @Test
        fun `null`() {
            assertFalse(isValidUrl(null))
        }

        @Test
        fun missingProtocol() {
            assertFalse(isValidUrl("www.google.com"))
        }
    }
}
