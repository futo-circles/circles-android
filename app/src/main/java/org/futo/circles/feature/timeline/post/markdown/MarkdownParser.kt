package org.futo.circles.feature.timeline.post.markdown

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.style.StrikethroughSpan
import androidx.core.content.ContextCompat
import androidx.core.text.getSpans
import io.noties.markwon.Markwon
import io.noties.markwon.SpanFactory
import io.noties.markwon.core.spans.BulletListItemSpan
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tasklist.TaskListSpan
import io.noties.markwon.linkify.LinkifyPlugin
import io.noties.markwon.simple.ext.SimpleExtPlugin
import org.futo.circles.R
import org.futo.circles.extensions.getGivenSpansAt
import org.futo.circles.feature.timeline.post.markdown.span.MentionSpan
import org.futo.circles.feature.timeline.post.markdown.span.OrderedListItemSpan
import org.futo.circles.feature.timeline.post.markdown.span.TextStyle


object MarkdownParser {

    const val mentionMark = "@"
    private const val boldMark = "**"
    private const val italicMark = "_"
    private const val strikeMark = "~~"
    private const val notDoneMark = "* [ ]"
    private const val doneMark = "* [x]"

    fun editableToMarkdown(text: Editable): String {
        val textCopy = Editable.Factory.getInstance().newEditable(text)
        text.getGivenSpansAt(span = TextStyle.values()).forEach {
            val start = textCopy.getSpanStart(it)
            val end = textCopy.getSpanEnd(it)
            when (it) {
                is StrongEmphasisSpan -> {
                    val endIndex = calculateLastIndexToInsert(textCopy, end, boldMark)
                    textCopy.insert(start, boldMark)
                    textCopy.insert(endIndex, boldMark)
                }
                is EmphasisSpan -> {
                    val endIndex = calculateLastIndexToInsert(textCopy, end, italicMark)
                    textCopy.insert(start, italicMark)
                    textCopy.insert(endIndex, italicMark)
                }
                is StrikethroughSpan -> {
                    val endIndex = calculateLastIndexToInsert(textCopy, end, strikeMark)
                    textCopy.insert(start, strikeMark)
                    textCopy.insert(endIndex, strikeMark)
                }
                is LinkSpan -> {
                    val linkStartMark = "["
                    textCopy.insert(start, linkStartMark)
                    textCopy.insert(end + linkStartMark.length, "](${it.link})")
                }
                is BulletListItemSpan -> textCopy.insert(start, "*")
                is OrderedListItemSpan -> textCopy.insert(start, it.number)
                is TaskListSpan -> {
                    val taskSpanMark = if (it.isDone) doneMark else notDoneMark
                    textCopy.insert(start, taskSpanMark)
                }
            }
        }
        text.getSpans<MentionSpan>().forEach {
            val end = textCopy.getSpanEnd(it)
            val textToInsert = it.name + mentionMark
            textCopy.insert(end, textToInsert)
        }
        return textCopy.toString()
    }

    fun markwonBuilder(context: Context): Markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(LinkifyPlugin.create())
        .usePlugin(SimpleExtPlugin.create().addExtension(
            1, '@'
        ) { configuration, props ->
            val span =
                (configuration.spansFactory() as? SpanFactory)?.getSpans(configuration, props)
            val name = (span as? MentionSpan)?.name ?: "test"
            MentionSpan(context, name)
        })
        .usePlugin(
            TaskListPlugin.create(
                ContextCompat.getColor(context, R.color.blue),
                ContextCompat.getColor(context, R.color.blue),
                Color.WHITE
            )
        ).build()

    private fun calculateLastIndexToInsert(textCopy: Editable, spanEnd: Int, mark: String): Int {
        var endIndex = spanEnd + mark.length
        val lastChar = textCopy.getOrNull(spanEnd - 1).toString()
        if (lastChar == " " || lastChar == "\n") endIndex -= 1
        return endIndex
    }

}