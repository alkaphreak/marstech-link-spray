package fr.marstech.mtlinkspray.entity

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class ChecklistProgressPropertyTest {

    private fun instance(todo: Int, done: Int, skipped: Int) = ChecklistInstanceEntity(
        namespace = "ns", templateId = "t", templateVersion = 0, title = "t",
        items = List(todo) { ChecklistInstanceItem("t$it", "x") } +
            List(done) { ChecklistInstanceItem("d$it", "x", status = ChecklistItemStatus.DONE) } +
            List(skipped) { ChecklistInstanceItem("s$it", "x", status = ChecklistItemStatus.SKIPPED) },
    )

    @Property(seed = "222")
    fun shouldStayBetweenZeroAndHundred(
        @ForAll @IntRange(max = 200) todo: Int, @ForAll @IntRange(max = 200) done: Int, @ForAll @IntRange(max = 200) skipped: Int,
    ) {
        assertTrue(instance(todo, done, skipped).progressPercent() in 0..100)
    }

    @Property(seed = "222")
    fun shouldBeHundredWhenNothingIsLeftToDo(@ForAll @IntRange(max = 200) done: Int, @ForAll @IntRange(max = 200) skipped: Int) {
        assertEquals(100, instance(0, done, skipped).progressPercent())
    }

    @Property(seed = "222")
    fun shouldIgnoreSkippedItems(@ForAll @IntRange(min = 1, max = 100) todo: Int, @ForAll @IntRange(max = 100) done: Int, @ForAll @IntRange(max = 100) skipped: Int) {
        assertEquals(instance(todo, done, 0).progressPercent(), instance(todo, done, skipped).progressPercent())
    }
}
