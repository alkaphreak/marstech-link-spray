package fr.marstech.mtlinkspray.utils

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class BotUserAgentsTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0",
            "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
        ]
    )
    fun shouldTreatBrowsersAsPeople(userAgent: String) {
        // Given a browser User-Agent / When classified / Then it is a person
        assertFalse(BotUserAgents.isBot(userAgent))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Slackbot-LinkExpanding 1.0 (+https://api.slack.com/robots)",
            "Mozilla/5.0 (compatible; Discordbot/2.0; +https://discordapp.com)",
            "Twitterbot/1.0",
            "facebookexternalhit/1.1 (+http://www.facebook.com/externalhit_uatext.php)",
            "WhatsApp/2.23.20.0",
            "LinkedInBot/1.0 (compatible; Mozilla/5.0; Apache-HttpClient +http://www.linkedin.com)",
            "Mozilla/5.0 AppleWebKit/537.36 (KHTML, like Gecko; compatible; GPTBot/1.2; +https://openai.com/gptbot)",
            "Mozilla/5.0 (compatible; ChatGPT-User/1.0; +https://openai.com/bot)",
            "Claude-User (claude-code/2.1.308; +https://support.anthropic.com/)",
            "curl/8.7.1",
            "mozilla/5.0 (compatible; chatgpt-user/1.0; +https://openai.com/bot)",
            "python-requests/2.32.3",
        ]
    )
    fun shouldTreatPreviewsCrawlersAgentsAndHttpClientsAsRobots(userAgent: String) {
        // Given a robot User-Agent / When classified / Then it is a robot
        assertTrue(BotUserAgents.isBot(userAgent))
    }

    @Test
    fun shouldMatchARobotTokenWithinTheLengthCapAndStayFastOnAnOversizedHeader() {
        // Given an 8 KB User-Agent, the Tomcat header limit, with a robot token at the start
        val oversized = "Twitterbot/1.0 " + "x".repeat(8 * 1024)

        // When classified / Then it is a robot, and the scan stays bounded by the length cap
        val start = System.nanoTime()
        assertTrue(BotUserAgents.isBot(oversized))
        assertFalse(BotUserAgents.isBot("Mozilla/5.0 " + "x".repeat(8 * 1024) + " Twitterbot/1.0"))
        assertTrue(System.nanoTime() - start < 1_000_000_000, "classification of 8 KB headers took over 1 s")
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["   "])
    fun shouldTreatAMissingUserAgentAsARobot(userAgent: String?) {
        // Given no User-Agent / When classified / Then it is a robot, since every browser sends one
        assertTrue(BotUserAgents.isBot(userAgent))
    }
}
