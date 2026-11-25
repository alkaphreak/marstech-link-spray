package fr.marstech.mtlinkspray.domain.shortener
import fr.marstech.mtlinkspray.adapter.outbound.botagents.ClasspathBotUserAgentSource
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullAndEmptySource
import org.junit.jupiter.params.provider.ValueSource

class BotUserAgentsTest {

    private val bots = BotUserAgents(ClasspathBotUserAgentSource())

    @ParameterizedTest
    @ValueSource(
        strings = [
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (X11; Linux x86_64; rv:133.0) Gecko/20100101 Firefox/133.0",
            "Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36 Edg/131.0.0.0",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/22B83 [FBAN/FBIOS;FBAV/490.0.0.38.109;FBBV/659171230;FBDV/iPhone15,2;FBMD/iPhone;FBSN/iOS;FBSV/18.1;FBSS/3;FBCR/;FBID/phone;FBLC/fr_FR;FBOP/5]",
            "Mozilla/5.0 (Linux; Android 15; Pixel 9 Build/AP3A.241105.007; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.6778.81 Mobile Safari/537.36 Instagram 356.0.0.41.101 Android (35/15; 420dpi; 1080x2424; Google/google; Pixel 9; tokay; tokay; fr_CA; 657131320)",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 18_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 [LinkedInApp]/9.30.1234",
            "Podcasts/1.1.0 CFNetwork/1568.200.51 Darwin/24.1.0",
            "Dalvik/2.1.0 (Linux; U; Android 15; Pixel 9 Build/AP3A.241105.007)",
        ]
    )
    fun shouldTreatBrowsersAsPeople(userAgent: String) {
        // Given a browser User-Agent / When classified / Then it is a person
        assertFalse(bots.isBot(userAgent))
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
            "node",
            "undici",
            "Java/21.0.1",
            "axios/1.7.9",
            "mozilla/5.0 (compatible; chatgpt-user/1.0; +https://openai.com/bot)",
            "python-requests/2.32.3",
        ]
    )
    fun shouldTreatPreviewsCrawlersAgentsAndHttpClientsAsRobots(userAgent: String) {
        // Given a robot User-Agent / When classified / Then it is a robot
        assertTrue(bots.isBot(userAgent))
    }

    @Test
    fun shouldMatchARobotTokenWithinTheLengthCapAndStayFastOnAnOversizedHeader() {
        // Given an 8 KB User-Agent, the Tomcat header limit, with a robot token at the start
        val oversized = "Twitterbot/1.0 " + "x".repeat(8 * 1024)

        // When classified / Then it is a robot, and the scan stays bounded by the length cap
        val start = System.nanoTime()
        assertTrue(bots.isBot(oversized))
        assertFalse(bots.isBot("Mozilla/5.0 " + "x".repeat(8 * 1024) + " Twitterbot/1.0"))
        assertTrue(System.nanoTime() - start < 1_000_000_000, "classification of 8 KB headers took over 1 s")
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["Twitterbot/1.0"])
    fun shouldCountEveryVisitAsAClickWhenNoPatternIsLoaded(userAgent: String?) {
        // Given patterns that failed to load / When classified / Then nothing is a robot, so every visit counts
        assertFalse(BotUserAgents.isBot(userAgent, emptyList()))
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = ["   "])
    fun shouldTreatAMissingUserAgentAsARobot(userAgent: String?) {
        // Given no User-Agent / When classified / Then it is a robot, since every browser sends one
        assertTrue(bots.isBot(userAgent))
    }

    @Test
    fun aFailingSourceShouldNotBreakStartupAndCountsEveryVisit() {
        val broken = BotUserAgents { error("unreadable bot lists") }

        assertFalse(broken.isBot("Twitterbot/1.0"))
        assertFalse(broken.isBot(null))
    }
}
