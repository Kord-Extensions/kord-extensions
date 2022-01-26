/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.LeafASTNode
import org.intellij.markdown.ast.visitors.RecursiveVisitor
import org.intellij.markdown.flavours.MarkdownFlavourDescriptor
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes
import org.intellij.markdown.flavours.gfm.StrikeThroughParser
import org.intellij.markdown.flavours.gfm.lexer._GFMLexer
import org.intellij.markdown.html.GeneratingProvider
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.html.URI
import org.intellij.markdown.lexer.MarkdownLexer
import org.intellij.markdown.parser.LinkMap
import org.intellij.markdown.parser.MarkdownParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParser
import org.intellij.markdown.parser.sequentialparsers.SequentialParserManager
import org.intellij.markdown.parser.sequentialparsers.impl.AutolinkParser
import org.intellij.markdown.parser.sequentialparsers.impl.BacktickParser
import org.intellij.markdown.parser.sequentialparsers.impl.EmphStrongParser
import org.intellij.markdown.parser.sequentialparsers.impl.InlineLinkParser

/**
 * Parses this String to [Markdown].
 *
 * @see Markdown
 */
public fun String.parseMarkdown(): Markdown {
    val tree = MarkdownParser(DiscordMarkFlavourDescriptor).buildMarkdownTreeFromString(this)
    return Markdown(this, tree)
}

/**
 * Container for Markdown operations.
 *
 * @property text the unchanged Markdown text
 *
 * @see parseMarkdown
 */
public class Markdown internal constructor(public val text: String, private val tree: ASTNode) {
    /**
     * Converts this markdown to HTML.
     */
    public fun toHTML(): String = HtmlGenerator(text, tree, DiscordMarkFlavourDescriptor).generateHtml()

    /**
     * Escapes all of this markdown, so Discord will render this as the raw input text.
     */
    public fun escape(): String = buildString { EscapingVisitor(text, this).visitNode(tree) }

    /**
     * Strips this text from all unescaped markdown identifiers.
     */
    public fun strip(): String = buildString { StrippingVisitor(text, this).visitNode(tree) }
}

/**
 * A [MarkdownFlavourDescriptor] for Discords markdown flavour.
 *
 * @see CommonMarkFlavourDescriptor
 */
public object DiscordMarkFlavourDescriptor : CommonMarkFlavourDescriptor() {
    private val supportedElements = listOf(
        MarkdownElementTypes.MARKDOWN_FILE,
        MarkdownElementTypes.BLOCK_QUOTE,
        MarkdownElementTypes.AUTOLINK,
        MarkdownElementTypes.LINK_LABEL,
        MarkdownElementTypes.LINK_TEXT,
        MarkdownElementTypes.LINK_DEFINITION,
        MarkdownElementTypes.CODE_FENCE,
        MarkdownElementTypes.CODE_BLOCK,
        MarkdownElementTypes.EMPH,
        MarkdownElementTypes.STRONG,
        MarkdownElementTypes.CODE_SPAN,
        GFMElementTypes.STRIKETHROUGH
    )

    /**
     * List of [IElementType]s which are plain text.
     */
    public val plainTextTypes: List<IElementType> = listOf(
        MarkdownTokenTypes.WHITE_SPACE,
        MarkdownTokenTypes.TEXT,
        MarkdownTokenTypes.EOL,
        MarkdownTokenTypes.CODE_LINE,
        MarkdownTokenTypes.CODE_FENCE_CONTENT,
        MarkdownTokenTypes.ESCAPED_BACKTICKS
    )

    override val sequentialParserManager: SequentialParserManager = object : SequentialParserManager() {
        override fun getParserSequence(): List<SequentialParser> {
            return listOf(
                AutolinkParser(listOf(MarkdownTokenTypes.AUTOLINK)),
                BacktickParser(),
                InlineLinkParser(),
                StrikeThroughParser(),
                EmphStrongParser()
            )
        }
    }

    override fun createHtmlGeneratingProviders(linkMap: LinkMap, baseURI: URI?): Map<IElementType, GeneratingProvider> =
        super.createHtmlGeneratingProviders(linkMap, baseURI).filter { (key, _) ->
            key in supportedElements
        }

    // We need to use thr GFM lexer, so we can use the strikethrough parser
    override fun createInlinesLexer(): MarkdownLexer = MarkdownLexer(_GFMLexer())
}

private class EscapingVisitor(private val source: String, private val escapedString: StringBuilder) :
    RecursiveVisitor() {
    override fun visitNode(node: ASTNode) {
        if (node is LeafASTNode) {
            when (node.type) {
                MarkdownTokenTypes.BACKTICK -> {
                    val ticks = source.substring(
                        node.startOffset, node.endOffset
                    )

                    ticks.forEach {
                        escapedString.append('\\')
                        escapedString.append(it)
                    }
                }
                in DiscordMarkFlavourDescriptor.plainTextTypes,
                MarkdownTokenTypes.FENCE_LANG,
                GFMTokenTypes.GFM_AUTOLINK -> escapedString.append(
                    source.substring(
                        node.startOffset, node.endOffset
                    )
                )
                MarkdownTokenTypes.CODE_FENCE_START, MarkdownTokenTypes.CODE_FENCE_END -> {
                    @Suppress("MagicNumber")
                    repeat(3) {
                        // according to spec \``` is valid however Discord only accepts \`\`\`
                        escapedString.append(escapeChar).append('`')
                    }
                }
                else -> escapedString.append(escapeChar).append(source.substring(node.startOffset, node.endOffset))
            }
        } else {
            super.visitNode(node)
        }
    }

    companion object {
        const val escapeChar = '\\'
    }
}

private class StrippingVisitor(private val source: String, private val strippedString: StringBuilder) :
    RecursiveVisitor() {
    override fun visitNode(node: ASTNode) {
        if (node is LeafASTNode) {
            if (node.type in DiscordMarkFlavourDescriptor.plainTextTypes) {
                strippedString.append(source.substring(node.startOffset, node.endOffset))
            }
        } else {
            super.visitNode(node)
        }
    }
}
