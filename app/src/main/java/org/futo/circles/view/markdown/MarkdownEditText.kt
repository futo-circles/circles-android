package org.futo.circles.view.markdown


import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import io.noties.markwon.*
import io.noties.markwon.core.spans.BulletListItemSpan
import io.noties.markwon.core.spans.EmphasisSpan
import io.noties.markwon.core.spans.LinkSpan
import io.noties.markwon.core.spans.StrongEmphasisSpan
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin
import io.noties.markwon.ext.tasklist.TaskListDrawable
import io.noties.markwon.ext.tasklist.TaskListItem
import io.noties.markwon.ext.tasklist.TaskListPlugin
import io.noties.markwon.ext.tasklist.TaskListSpan
import org.commonmark.node.SoftLineBreak
import org.futo.circles.R
import org.futo.circles.extensions.getGivenSpansAt

class MarkdownEditText(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {

    private val markwon: Markwon
    private var isSelectionStyling = false
    private var listSpanStart = 0
    private var currentListSpanNumber = 0
    private var currentListSpanLine = 0
    private val taskBoxColor by lazy { ContextCompat.getColor(context, R.color.blue) }
    private val taskBoxMarkColor = Color.WHITE
    private val listStyles =
        arrayOf(TextStyle.UNORDERED_LIST, TextStyle.ORDERED_LIST, TextStyle.TASKS_LIST)
    private var onHighlightSpanListener: ((List<TextStyle>) -> Unit)? = null
    private var selectedStyles: MutableList<TextStyle> = mutableListOf()

    init {
        movementMethod = EnhancedMovementMethod().getsInstance()
        markwon = markwonBuilder(context)
        doOnTextChanged { _, start, before, count ->
            styliseText(start, count)
            handleListSpanTextChange(before, count)
        }
    }

    override fun getText(): Editable {
        return super.getText() ?: Editable.Factory.getInstance().newEditable("")
    }

    fun setHighlightSelectedSpanListener(onHighlight: (List<TextStyle>) -> Unit) {
        onHighlightSpanListener = onHighlight
    }

    fun insertMention() {
        insertText("@")
    }

    fun insertText(message: String) {
        text.insert(selectionStart, message)
    }

    fun triggerStyle(textStyle: TextStyle, isSelected: Boolean) {
        if (isSelected) {
            if (textStyle in listStyles) {
                selectedStyles.removeAll { it in listStyles }
                triggerListStyle(textStyle)
            }
            selectedStyles.add(textStyle)
        } else selectedStyles.remove(textStyle)

        if (isSelectionStyling) {
            styliseText(selectionStart, selectionEnd)
            isSelectionStyling = false
        }
        onHighlightSpanListener?.invoke(selectedStyles)
    }

    private fun triggerListStyle(listSpanStyle: TextStyle) {
        currentListSpanNumber = 1
        val currentLineStart = layout.getLineStart(getCurrentCursorLine())
        if (selectionStart == currentLineStart)
            text.insert(selectionStart, " ")
        else text.insert(selectionStart, "\n ")

        listSpanStart = selectionStart - 1
        text.setSpan(
            getListSpan(listSpanStyle, "${currentListSpanNumber}.", false),
            listSpanStart,
            selectionStart,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        currentListSpanNumber++
        currentListSpanLine = lineCount
    }

    private fun handleListSpanTextChange(before: Int, count: Int) {
        val listSpanStyle = selectedStyles.firstOrNull { it in listStyles } ?: return
        if (before > count) return
        if (selectionStart == selectionEnd && currentListSpanLine < lineCount) {
            currentListSpanLine = lineCount
            val string = text.toString()
            // If user hit enter
            if (string[selectionStart - 1] == '\n') {
                listSpanStart = selectionStart
                text.insert(selectionStart, " ")
                text.setSpan(
                    getListSpan(listSpanStyle, "${currentListSpanNumber}.", false),
                    listSpanStart,
                    listSpanStart + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                currentListSpanNumber++
            } else {
                for (listSpan in text.getGivenSpansAt(
                    span = arrayOf(listSpanStyle),
                    listSpanStart,
                    listSpanStart + 1
                )) {
                    val number = (listSpan as? OrderedListItemSpan)?.number ?: ""
                    val isDone = (listSpan as? TaskListSpan)?.isDone ?: false
                    text.removeSpan(listSpan)
                    text.setSpan(
                        getListSpan(listSpanStyle, number, isDone),
                        listSpanStart,
                        selectionStart,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }
    }

    private fun getListSpan(listSpanStyle: TextStyle, currentNum: String, isDone: Boolean): Any =
        when (listSpanStyle) {
            TextStyle.ORDERED_LIST -> OrderedListItemSpan(
                markwon.configuration().theme(),
                currentNum
            )
            TextStyle.TASKS_LIST -> setTaskSpan(listSpanStart, selectionStart, isDone)
            else -> BulletListItemSpan(markwon.configuration().theme(), 0)
        }

    fun addLinkSpan(title: String?, link: String) {
        val newTitle = if (title.isNullOrEmpty()) link else title
        val cursorStart = selectionStart
        text.insert(cursorStart, newTitle)
        text.setSpan(
            LinkSpan(markwon.configuration().theme(), link, LinkResolverDef()),
            cursorStart,
            cursorStart + newTitle.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }

    private fun setTaskSpan(start: Int, end: Int, isDone: Boolean) {
        val taskSpan = TaskListSpan(
            markwon.configuration().theme(),
            TaskListDrawable(taskBoxColor, taskBoxColor, taskBoxMarkColor),
            isDone
        )
        text.setSpan(taskSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(getTaskClickableSpan(taskSpan), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun styliseText(start: Int, end: Int) {
        if (start >= end) return
        selectedStyles.forEach { textStyle ->
            val span = when (textStyle) {
                TextStyle.BOLD -> StrongEmphasisSpan()
                TextStyle.ITALIC -> EmphasisSpan()
                TextStyle.STRIKE -> StrikethroughSpan()
                else -> null
            }
            span?.let {
                text.setSpan(it, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

//    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
//        super.onSelectionChanged(selStart, selEnd)
//        if (selStart != selEnd) isSelectionStyling = true
//        if (selStart <= 0) {
//            onHighlightSpanListener?.invoke(emptyList())
//            return
//        }
//        val spans = mutableListOf<TextStyle>()
//        val currentLineStart = layout.getLineStart(getCurrentCursorLine())
//        val listsSpans = text.getGivenSpansAt(
//            span = arrayOf(
//                TextStyle.UNORDERED_LIST,
//                TextStyle.TASKS_LIST,
//                TextStyle.ORDERED_LIST
//            ),
//            start = currentLineStart, end = currentLineStart + 1
//        )
//        listsSpans.forEach {
//            when (it) {
//                is BulletListItemSpan -> spans.add(TextStyle.UNORDERED_LIST)
//                is OrderedListItemSpan -> spans.add(TextStyle.ORDERED_LIST)
//                is TaskListSpan -> spans.add(TextStyle.TASKS_LIST)
//            }
//        }
//        val textSpans = text.getGivenSpansAt(
//            span = arrayOf(TextStyle.BOLD, TextStyle.ITALIC, TextStyle.STRIKE),
//            start = selStart - 1, end = selStart
//        )
//        textSpans.forEach {
//            when (it) {
//                is StrongEmphasisSpan -> spans.add(TextStyle.BOLD)
//                is EmphasisSpan -> spans.add(TextStyle.ITALIC)
//                is StrikethroughSpan -> spans.add(TextStyle.STRIKE)
//            }
//        }
//        onHighlightSpanListener?.invoke(spans)
//    }

    private fun getTaskClickableSpan(taskSpan: TaskListSpan) = object : ClickableSpan() {
        override fun onClick(widget: View) {
            val spanStart = text.getSpanStart(taskSpan)
            val spanEnd = text.getSpanEnd(taskSpan)
            taskSpan.isDone = !taskSpan.isDone
            if (spanStart >= 0) {
                text.setSpan(taskSpan, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }


    private fun getCurrentCursorLine(): Int {
        return if (selectionStart != -1) layout.getLineForOffset(selectionStart) else -1
    }

    fun getTextWithMarkdown(): String {
        val textCopy = Editable.Factory.getInstance().newEditable(text)
        text.getGivenSpansAt(span = TextStyle.values()).forEach {
            val start = textCopy.getSpanStart(it)
            val end = textCopy.getSpanEnd(it)
            when (it) {
                is StrongEmphasisSpan -> {
                    val boldMark = "**"
                    textCopy.insert(start, boldMark)
                    textCopy.insert(end + boldMark.length, boldMark)
                }
                is EmphasisSpan -> {
                    val italicMark = "_"
                    textCopy.insert(start, italicMark)
                    textCopy.insert(end + italicMark.length, italicMark)
                }
                is StrikethroughSpan -> {
                    val strikeMark = "~~"
                    textCopy.insert(start, strikeMark)
                    textCopy.insert(end + strikeMark.length, strikeMark)
                }
                is LinkSpan -> {
                    val linkStartMark = "["
                    textCopy.insert(start, linkStartMark)
                    textCopy.insert(end + linkStartMark.length, "](${it.link})")
                }
                is BulletListItemSpan -> {

                }
                is OrderedListItemSpan -> {

                }
                is TaskListSpan -> {}
            }
        }
        return textCopy.toString()
    }

    private fun markwonBuilder(context: Context): Markwon = Markwon.builder(context)
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(TaskListPlugin.create(taskBoxColor, taskBoxColor, taskBoxMarkColor))
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun configureVisitor(builder: MarkwonVisitor.Builder) {
                super.configureVisitor(builder)
                builder.on(SoftLineBreak::class.java) { visitor, _ -> visitor.forceNewLine() }
            }

            override fun configureSpansFactory(builder: MarkwonSpansFactory.Builder) {
                val origin = builder.getFactory(TaskListItem::class.java)
                builder.setFactory(
                    TaskListItem::class.java
                ) { configuration, props ->
                    val span = origin?.getSpans(configuration, props)
                    (span as? TaskListSpan)?.let { arrayOf(span, getTaskClickableSpan(span)) }
                }
            }
        }).build()
}