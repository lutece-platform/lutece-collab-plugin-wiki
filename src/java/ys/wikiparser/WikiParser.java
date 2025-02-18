/*
 * Copyright 2007-2009 Yaroslav Stavnichiy, yarosla@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Latest version of this software can be obtained from:
 *
 *     http://t4-wiki-parser.googlecode.com
 *
 * If you make use of this code, I'd appreciate hearing about it.
 * Comments, suggestions, and bug reports welcome: yarosla@gmail.com
 */
package ys.wikiparser;

import static ys.wikiparser.Utils.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.util.data.MutableDataSet;
import com.vladsch.flexmark.util.misc.Extension;
import java.net.*;

import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
/**
 * WikiParser.renderXHTML() is the main method of this class. It takes wiki-text and returns XHTML.
 *
 * WikiParser's behavior can be customized by overriding appendXxx() methods, which should make integration of this class into any wiki/blog/forum software easy
 * and painless.
 *
 * @author Yaroslav Stavnichiy (yarosla@gmail.com)
 *
 */
public class WikiParser
{
    private static final int MAX_LIST_LEVELS = 100;
    private static final String [ ] ESCAPED_INLINE_SEQUENCES = {
            "{{{", "{{", "}}}", "**", "//", "__", "##", "\\\\", "[[", "<<<", "~", "--", "|"
    };
    private static final String LIST_CHARS = "*-#>:!";
    private static final String [ ] LIST_OPEN = {
            "<ul><li>", "<ul><li>", "<ol><li>", "<blockquote>", "<div class='wiki_indent'>", "<div class='wiki_center'>"
    };
    private static final String [ ] LIST_CLOSE = {
            "</li></ul>\n", "</li></ul>\n", "</li></ol>\n", "</blockquote>\n", "</div>\n", "</div>\n"
    };
    private static final String FORMAT_CHARS = "*/_#";
    private static final String [ ] FORMAT_DELIM = {
            "**", "//", "__", "##"
    };
    private static final String [ ] FORMAT_TAG_OPEN = {
            "<strong>", "<em>", "<span class=\"wiki_underline\">", "<tt>"
    };
    private static final String [ ] FORMAT_TAG_CLOSE = {
            "</strong>", "</em>", "</span>", "</tt>"
    };
    public static String CUSTOM_INPUTS_TO_REEMPLACE = "//CustomInputToReEmplace//";
    private int wikiLength;
    private char [ ] wikiChars;
    protected StringBuilder sb = new StringBuilder( );
    protected StringBuilder toc = new StringBuilder( );
    protected int tocLevel = 0;
    private HashSet<String> tocAnchorIds = new HashSet<String>( );
    private String wikiText;
    private int pos = 0;
    private int listLevel = -1;
    private char [ ] listLevels = new char [ MAX_LIST_LEVELS + 1]; // max number of levels allowed
    private boolean blockquoteBR = false;
    private boolean inTable = false;
    private int mediawikiTableLevel = 0;
    protected int HEADING_LEVEL_SHIFT = 1; // make =h2, ==h3, ...
    protected String HEADING_ID_PREFIX = null;
    private String _strTableClass = "";
    private String _strParentTableClass = "";
    private String _strTocClass = "toc";
    public String markdown = "";
    protected WikiParser( )
    {
        // for use by subclasses only
        // subclasses should call parse() to complete construction
    }

    protected WikiParser( String wikiText )
    {
        parse( wikiText );
    }

    protected void setTableClass( String strClass )
    {
        _strTableClass = strClass;
    }


    protected void setParentTableClass( String strParentClass )
    {
        _strParentTableClass = strParentClass;
    }


    protected void setTocClass( String strClass )
    {
        _strTocClass = strClass;
    }

    public static String renderXHTML( String wikiText )
    {
        return new WikiParser( wikiText ).toString( );
    }

    protected void parse( String wikiText )
    {
        wikiText = preprocessWikiText( wikiText );

        this.wikiText = wikiText;
        wikiLength = this.wikiText.length( );
        wikiChars = new char [ wikiLength];
        this.wikiText.getChars( 0, wikiLength, wikiChars, 0 );

        while ( parseBlock( ) )
            ;

        closeListsAndTables( );

        while ( mediawikiTableLevel-- > 0 )
            sb.append( "</td></tr></table></div>\n" );

        completeTOC( );
    }
    protected void parseMD( String wikiText )
    {
        MutableDataSet options = new MutableDataSet( );


        List<Extension> extensions =
                java.util.Arrays.asList(
                        com.vladsch.flexmark.ext.escaped.character.EscapedCharacterExtension.create(),
                    /* Input and Output Example of escaped.character.EscapedCharacterExtension.create(),

                    ```markdown
                    This is a text with escaped character: \*star\*
                    ```
                    **Output:**
                    ```html
                    <p>This is a text with escaped character: *star*</p>
                    ```
                     */
                        com.vladsch.flexmark.ext.media.tags.MediaTagsExtension.create( ),
                      /* Input and Output Example of com.vladsch.flexmark.ext.media.tags.MediaTagsExtension.create( ),
                        ```markdown
                        Here is a video link: ![Video](http://example.com/video.mp4)
                        ```
                        **Output:**
                        ```html
                        <p>Here is a video link: <video src="http://example.com/video.mp4" controls></video></p>
                        ```
                         */

                        // marche pas
                    //    com.vladsch.flexmark.ext.xwiki.macros.MacroExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.xwiki.macros.MacroExtension.create( ),
                        ```markdown
                        This is a macro in XWiki syntax: {{info}}This is an info macro in XWiki syntax.{{/info}}
                                ```
                                **Output:**
                                ```html
                                <div class="info">This  is an info macro in XWiki syntax.</div>
                                ```
                         */

                        // marche pas
                     //   com.vladsch.flexmark.ext.enumerated.reference.EnumeratedReferenceExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.enumerated.reference.EnumeratedReferenceExtension
                        ```markdown
                        This is a reference: [(1)]
                         [(1)]: This is the enumerated reference.
                        *
                        **Output:**
                        ```html
                        <p>This is a reference: <a href="#enumerated-reference-1" id="enumerated-reference-link-1">(1)</a></p>
                        <p id="enumerated-reference-1">This is the enumerated reference.</p>
                         */


                        com.vladsch.flexmark.ext.tables.TablesExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.tables.TablesExtension
                        **Input (Markdown with table):**
                        ```markdown
                        | Header 1 | Header 2 |
                        |----------|----------|
                        | Cell 1   | Cell 2   |
                        | Cell 3   | Cell 4   |
                        ```
                        **Output (HTML):**

                        ```html
                        <table>
                        <thead>
                        <tr>
                        <th>Header 1</th>
                        <th>Header 2</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                        <td>Cell 1</td>
                        <td>Cell 2</td>
                        </tr>
                        <tr>
                        <td>Cell 3</td>
                        <td>Cell 4</td>
                        </tr>
                        </tbody>
                        </table>
                        ```
                         */
                        com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension
                        **Input (Markdown with strikethrough):**
                        * **Input (Markdown with strikethrough):**

                        ```markdown
                        This is a ~~strikethrough~~ text.
                        ```

                        **Output (HTML):**

                        ```html
                        <p>This is a <del>strikethrough</del> text.</p>
                        ```
                         */


                        com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
                          **Input (Markdown with task list):**
                            ```markdown
                            - [ ] Task 1
                            - [x] Task 2
                            - [ ] Task 3
                            ```

                            **Output (HTML):**

                            ```html
                            <ul>
                            <li class="task-list-item"><input disabled="" type="checkbox"> Task 1</li>
                            <li class="task-list-item"><input checked="" disabled="" type="checkbox"> Task 2</li>
                            <li class="task-list-item"><input disabled="" type="checkbox"> Task 3</li>
                            </ul>
                            ```
                         */

// images a ajouter aux liens suivant /img/heart.png
//                        com.vladsch.flexmark.ext.emoji.EmojiExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.emoji.EmojiExtension
                        **Input (Markdown with emoji):**
                        ```markdown
                        * :smile:
                        * :heart:
                        * :+1:
                        * :-1:
                        * :100:
                        *
                         ** Output (HTML):**
                         *
                         ```html
                                              <p><img src="/img/smile.png" alt="emoji people:smile" height="20" width="20" align="absmiddle">
                        <img src="/img/heart.png" alt="emoji people:heart" height="20" width="20" align="absmiddle">
                        <img src="/img/plus1.png" alt="emoji people:+1" height="20" width="20" align="absmiddle">
                        <img src="/img/-1.png" alt="emoji people:-1" height="20" width="20" align="absmiddle">
                        <img src="/img/100.png" alt="emoji symbols:100" height="20" width="20" align="absmiddle"></p>
                         */


                      // fonction pas
                        // com.vladsch.flexmark.ext.toc.TocExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.toc.TocExtension
                          com.vladsch.flexmark.ext.toc.TocExtension.create()

                        # Heading 1

                        ## Subheading 1.1

                        ### Subheading 1.1.1

                        ## Subheading 1.2

                        # Heading 2

                        ## Subheading 2.1

                        <div class="toc">
                          <ul>
                            <li><a href="#heading-1">Heading 1</a>
                              <ul>
                                <li><a href="#subheading-1-1">Subheading 1.1</a>
                                  <ul>
                                    <li><a href="#subheading-1-1-1">Subheading 1.1.1</a></li>
                                  </ul>
                                </li>
                                <li><a href="#subheading-1-2">Subheading 1.2</a></li>
                              </ul>
                            </li>
                            <li><a href="#heading-2">Heading 2</a>
                              <ul>
                                <li><a href="#subheading-2-1">Subheading 2.1</a></li>
                              </ul>
                            </li>
                          </ul>
                        </div>
                        <h1 id="heading-1">Heading 1</h1>
                        <h2 id="subheading-1-1">Subheading 1.1</h2>
                        <h3 id="subheading-1-1-1">Subheading 1.1.1</h3>
                        <h2 id="subheading-1-2">Subheading 1.2</h2>
                        <h1 id="heading-2">Heading 2</h1>
                        <h2 id="subheading-2-1">Subheading 2.1</h2>
                         */


                        com.vladsch.flexmark.ext.typographic.TypographicExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.typographic.TypographicExtension
                        **Input (Markdown with typographic characters):**
                            ```markdown
                            "Hello, World!"

                            'Hello, World!'

                            --Hello, World!--

                            ...Hello, World...
                            ```

                            **Output (HTML):**

                            ```html
                            <p>“Hello, World!”</p>

                            <p>‘Hello, World!’</p>

                            <p>–Hello, World!–</p>

                            <p>…Hello, World…</p>
                            ```
                         */
                        com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.youtube.embedded.YouTubeLinkExtension
                        ```markdown
                            Check out this cool video: @[](https://www.youtube.com/watch?v=dQw4w9WgXcQ)
                            ```
                            **Output:**

                            The output will be an HTML string with the YouTube link converted into an embedded YouTube video.

                            ```html
                            <p>Check out this cool video: <iframe width="560" height="315" src="https://www.youtube.com/embed/dQw4w9WgXcQ" frameborder="0" allowfullscreen></iframe></p>
                            ```
                         */

                       com.vladsch.flexmark.ext.macros.MacrosExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.macros.MacrosExtension
                        https://github.com/vsch/flexmark-java/wiki/Macros-Extension
                        ```markdown
                         >>>blockMacro
                            1. item 1
                            1. item 2
                            <<<

                            <<<blockMacro>>>
                            ```
                            **Output:**

                            ```html
                        <p>Paragraph with a
                        <ol>
                          <li>item 1</li>
                          <li>item 2</li>
                        </ol>
                        inserted.</p>                            ```
                         */
                        com.vladsch.flexmark.ext.definition.DefinitionExtension.create( ),
                         /* Input and Output Example of com.vladsch.flexmark.ext.definition.DefinitionExtension
                        ```markdown
                            Term 1
                            :   Definition 1

                            Term 2
                            :   Definition 2
                            ```
                            **Output:**

                            The output will be an HTML string with the definition list converted into a definition list.

                            ```html
                            <dl>
                            <dt>Term 1</dt>
                            <dd>Definition 1</dd>
                            <dt>Term 2</dt>
                            <dd>Definition 2</dd>
                            </dl>
                            ```
                         */

                         com.vladsch.flexmark.ext.autolink.AutolinkExtension.create( )
                       /* Input and Output Example of com.vladsch.flexmark.ext.autolink.AutolinkExtension
                        ```markdown
                            This is a link: <http://www.google.com>
                            ```
                            **Output:**

                            The output will be an HTML string with the link converted into an HTML anchor tag.

                            ```html
                            <p>This is a link: <a href="http://www.google.com">http://www.google.com</a></p>
                            ```
                         */

   //                 com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.create( )
                        /* Input and Output Example of com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension

                        ```markdown
                            ## Table of Contents
                            [TOC]
                            ```
                            **Output:**

                            The output will be an HTML string with the table of contents converted into a list of links to the headings in the document.

                            ```html
                            <h2 id="table-of-contents">Table of Contents</h2>
                            <div class="toc">
                            <ul>
                            <li><a href="#heading-1">Heading 1</a></li>
                            <li><a href="#heading-2">Heading 2</a></li>
                            </ul>
                            </div>
                            <h1 id="heading-1">Heading 1</h1>
                            <h2 id="heading-2">Heading 2</h2>
                            ```
                         */
                   //     com.vladsch.flexmark.ext.wikilink.WikiLinkExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.wikilink.WikiLinkExtension
                        ```markdown
                            This is a wiki link: [[Wiki Link]]
                            ```
                            **Output:**

                            The output will be an HTML string with the wiki link converted into an HTML anchor tag.

                            ```html
                            <p>This is a wiki link: <a href="Wiki Link">Wiki Link</a></p>
                            ```
                         */

                        // fonctionne pas
                     //   com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
                        ```markdown
                            *[HTML]: Hyper Text Markup Language
                            The abbreviation for HTML is *[HTML].
                            ```
                            **Output:**

                            The output will be an HTML string with the abbreviation converted

                                ```html
                                <p>The abbreviation for <abbr title="Hyper Text Markup Language">HTML</abbr> is <abbr title="Hyper Text Markup Language">HTML</abbr>.</p>
                                ```
                         */


           //             com.vladsch.flexmark.ext.admonition.AdmonitionExtension.create( ),
                       /* Input and Output Example of com.vladsch.flexmark.ext.admonition.AdmonitionExtension
                        ```markdown
                            !!! note
                            This is a note admonition.
                            ```
                            **Output:**

                            The output will be an HTML string with the admonition converted into a styled block.

                            ```html
                               <div class="adm-block adm-note">
                                <div class="adm-heading">
                                <svg class="adm-icon"><use xlink:href="#adm-note"></use></svg><span>Note</span>
                                </div>
                                <div class="adm-body">
                                <p>This is a note admonition.
                                This Wiki aims to be a very simple collaborative tool fully integrated to your Lutece site</p>
                                </div>
                                </div>

                            ```
                        */

                        // fonctionne pas
                    //    com.vladsch.flexmark.ext.aside.AsideExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.aside.AsideExtension
                        ```markdown
                            ::: aside
                            This is an aside block.
                            :::
                            ```
                            **Output:**

                            The output will be an HTML string with the aside block converted into a styled block.

                            ```html
                            <div class="aside">
                            <p>This is an aside block.</p>
                            </div>
                            ```
                         */

                        // pour ajouter des class, id et autres attributs aux balises html
              //          com.vladsch.flexmark.ext.attributes.AttributesExtension.create( ),
                        /* Input and Output Example of com.vladsch.flexmark.ext.attributes.AttributesExtension
                        https://github.com/vsch/flexmark-java/wiki/Attributes-Extension
                        ```markdown
                            This is a paragraph with a custom attribute {#my-id}
                            ```info {#not-id} not {title="Title" caption="Cap"} {caption="Caption"}
                            ```
                            ```
                            **Output:**

                            The output will be an HTML string with the custom attribute converted.

                            ```html
                            <p id="my-id">This is a paragraph with a custom attribute</p>
                            <pre title="Title" caption="Caption"><code class="language-info"></code></pre>
                            ```
                         */




                        // github flavoured markdown extentions
                //        com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create( ),
                //        com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create( ),
                        //        com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension.create( ),


                 // Gitlab markdown extention fonctionne pas
                   //    com.vladsch.flexmark.ext.gitlab.GitLabExtension.create()
                  /*
                  **Input (GitLab-flavored Markdown):**

                    ```markdown
                    # Heading

                    Hello @username, this is a test.

                    - [ ] Task 1
                    - [x] Task 2

                    See issue #123 for more details.

                    ```

                    **Output (HTML):**

                    ```html
                    <h1>Heading</h1>

                    <p>Hello <a href="/username">@username</a>, this is a test.</p>

                    <ul>
                      <li><input type="checkbox" disabled> Task 1</li>
                      <li><input type="checkbox" disabled checked> Task 2</li>
                    </ul>

                    <p>See issue <a href="/issues/123">#123</a> for more details.</p>


                    ```
                   */

                        // Set the EXTANCHORLINKS extension to prevent HTML conversion
                   //     com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension.create( )
                );
        options.set(Parser.EXTENSIONS, extensions);
        options.set(HtmlRenderer.INDENT_SIZE, 2);
            options.set(HtmlRenderer.PERCENT_ENCODE_URLS, true);
        options.set(HtmlRenderer.GENERATE_HEADER_ID, true);
        options.set(HtmlRenderer.RENDER_HEADER_ID, true);
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        markdown = wikiText;
        List<String> customInputsHtml = extractCustomInputs();

        com.vladsch.flexmark.util.ast.Node document = parser.parse(markdown);
        String html = renderer.render(document);
        String htmlWithCustomInputs = remplaceCustomInputs( html, customInputsHtml );
        sb = new StringBuilder( htmlWithCustomInputs);

    }
    /**
     * Preprocess wiki text before parsing
     * @param wikiText
     * @return
     */
    public String remplaceCustomInputs( String html, List<String> customInputsHtml )
    {
        for ( int i = 0; i < customInputsHtml.size( ); i++ )
        {
            html = html.replace( CUSTOM_INPUTS_TO_REEMPLACE +"_"+ i, customInputsHtml.get( i ) );
        }
        return html;
    }
    /**
     * get custom inputs from the wiki text
     * @param wikiText
     * @return
     */
    public List<String> extractCustomInputs ()
    {
        // Check what is writen at first between two $$ markers to find custom inputs names
        List <String> customInputsHtml = new ArrayList <>( );
        String regex = "\\$\\$(.*?)\\$\\$";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(markdown);
        int iteration = 0;
        while (matcher.find()) {
            // get customInputName if there is one (only letters and numbers)
            String customInput = matcher.group(1).trim();
              String customInputName = "";
            for ( int i = 0; i < customInput.length( ); i++ )
            {

                char c = customInput.charAt( i );
                String regexText = "[a-zA-Z0-9]";
                if (Character.toString(c).matches(regexText))
                {
                    customInputName += c;
                }
                else {
                    break;
                }
            }
            // if the custom input is not empty extract params and fill the template thought MarkdownCustomInputs.renderCustomInHtml ( customInput, customInputName );
            if(customInputName != null && !customInputName.isEmpty())
            {
                String html = MarkdownCustomInputs.renderCustomInHtml( customInput, customInputName );

                if(html != null && !html.isEmpty())
                {
                    customInputsHtml.add(html);
                    // Reemplace the custom input by a marker, to reemplace it with the html after the markdown is converted to html
                    int startIntpot = markdown.indexOf("$$");
                    int endPostion = markdown.indexOf("$$", startIntpot + 2);
                    markdown = markdown.substring(0, startIntpot) + markdown.substring(endPostion + 2);
                    markdown = markdown.substring(0, startIntpot) + CUSTOM_INPUTS_TO_REEMPLACE +"_"+ iteration + markdown.substring(startIntpot);
                    iteration++;
                }
            }
        }
      return customInputsHtml;
    }

    public String toString( )
    {
        return sb.toString( );
    }

    private void closeListsAndTables( )
    {
        // close unclosed lists
        while ( listLevel >= 0 )
        {
            sb.append( LIST_CLOSE [LIST_CHARS.indexOf( listLevels [listLevel--] )] );
        }

        if ( inTable )
        {
            sb.append( "</table></div>\n" );
            inTable = false;
        }
    }

    private boolean parseBlock( )
    {
        for ( ; ( pos < wikiLength ) && ( wikiChars [pos] <= ' ' ) && ( wikiChars [pos] != '\n' ); pos++ )
            ; // skip whitespace

        if ( pos >= wikiLength )
        {
            return false;
        }

        char c = wikiChars [pos];

        if ( c == '\n' )
        { // blank line => end of list/table; no other meaning
            closeListsAndTables( );
            pos++;

            return true;
        }

        if ( c == '|' )
        { // table

            if ( mediawikiTableLevel > 0 )
            {
                int pp = pos + 1;

                if ( pp < wikiLength )
                {
                    boolean newRow = false;
                    boolean endTable = false;

                    if ( wikiChars [pp] == '-' )
                    { // mediawiki-table new row
                        newRow = true;
                        pp++;
                    }
                    else
                        if ( wikiChars [pp] == '}' )
                        { // mediawiki-table end table
                            endTable = true;
                            pp++;
                        }

                    for ( ; ( pp < wikiLength ) && ( ( wikiChars [pp] == ' ' ) || ( wikiChars [pp] == '\t' ) ); pp++ )
                        ; // skip spaces

                    if ( ( pp == wikiLength ) || ( wikiChars [pp] == '\n' ) )
                    { // nothing else on the line => it's mediawiki-table markup
                        closeListsAndTables( ); // close lists if any
                        sb.append( newRow ? "</td></tr>\n<tr><td>" : ( endTable ? "</td></tr></table></div>\n" : "</td>\n<td>" ) );

                        if ( endTable )
                        {
                            mediawikiTableLevel--;
                        }

                        pos = pp + 1;

                        return pp < wikiLength;
                    }
                }
            }

            if ( !inTable )
            {
                closeListsAndTables( ); // close lists if any
                sb.append( "<div class=\"").append( _strParentTableClass ).append( "\" >" ).append("<table class=\"" ).append( _strTableClass ).append( "\" >" );
                inTable = true;
            }

            pos = parseTableRow( pos + 1 );

            return true;
        }
        else
        {
            if ( inTable )
            {
                sb.append( "</table></div>\n" );
                inTable = false;
            }
        }

        if ( ( listLevel >= 0 ) || ( LIST_CHARS.indexOf( c ) >= 0 ) )
        { // lists

            int lc;

            // count list level
            for ( lc = 0; ( lc <= listLevel ) && ( ( pos + lc ) < wikiLength ) && ( wikiChars [pos + lc] == listLevels [lc] ); lc++ )
                ;

            if ( lc <= listLevel )
            { // end list block(s)

                do
                {
                    sb.append( LIST_CLOSE [LIST_CHARS.indexOf( listLevels [listLevel--] )] );
                }
                while ( lc <= listLevel );

                // list(s) closed => retry from the same position
                blockquoteBR = true;

                return true;
            }
            else
            {
                if ( ( pos + lc ) >= wikiLength )
                {
                    return false;
                }

                char cc = wikiChars [pos + lc];
                int listType = LIST_CHARS.indexOf( cc );

                if ( ( listType >= 0 ) && ( ( pos + lc + 1 ) < wikiLength ) && ( wikiChars [pos + lc + 1] != cc ) && ( listLevel < MAX_LIST_LEVELS ) )
                { // new list block
                    sb.append( LIST_OPEN [listType] );
                    listLevels [++listLevel] = cc;
                    blockquoteBR = true;
                    pos = parseListItem( pos + lc + 1 );

                    return true;
                }
                else
                    if ( listLevel >= 0 )
                    { // list item - same level

                        if ( ( listLevels [listLevel] == '>' ) || ( listLevels [listLevel] == ':' ) )
                        {
                            sb.append( '\n' );
                        }
                        else
                            if ( listLevels [listLevel] == '!' )
                            {
                                sb.append( "</div>\n<div class='wiki_center'>" );
                            }
                            else
                            {
                                sb.append( "</li>\n<li>" );
                            }

                        pos = parseListItem( pos + lc );

                        return true;
                    }
            }
        }

        if ( c == '=' )
        { // heading

            int hc;

            // count heading level
            for ( hc = 1; ( hc < 6 ) && ( ( pos + hc ) < wikiLength ) && ( wikiChars [pos + hc] == '=' ); hc++ )
                ;

            if ( ( pos + hc ) >= wikiLength )
            {
                return false;
            }

            int p;

            for ( p = pos + hc; ( p < wikiLength ) && ( ( wikiChars [p] == ' ' ) || ( wikiChars [p] == '\t' ) ); p++ )
                ; // skip spaces

            String tagName = "h" + ( hc + HEADING_LEVEL_SHIFT );
            sb.append( "<" + tagName + " id=''>" ); // real id to be inserted after parsing this item

            int hStart = sb.length( );
            pos = parseItem( p, wikiText.substring( pos, pos + hc ), ContextType.HEADER );

            String hText = sb.substring( hStart, sb.length( ) );
            sb.append( "</" + tagName + ">\n" );

            String anchorId = generateTOCAnchorId( hc, hText );
            sb.insert( hStart - 2, anchorId );
            appendTOCItem( hc, anchorId, hText );

            return true;
        }
        else
            if ( c == '{' )
            { // nowiki-block?

                if ( ( ( pos + 2 ) < wikiLength ) && ( wikiChars [pos + 1] == '{' ) && ( wikiChars [pos + 2] == '{' ) )
                {
                    int startNowiki = pos + 3;
                    int endNowiki = findEndOfNowiki( startNowiki );
                    int endPos = endNowiki + 3;

                    if ( wikiText.lastIndexOf( '\n', endNowiki ) >= startNowiki )
                    { // block <pre>

                        if ( wikiChars [startNowiki] == '\n' )
                        {
                            startNowiki++; // skip the very first '\n'
                        }

                        if ( wikiChars [endNowiki - 1] == '\n' )
                        {
                            endNowiki--; // omit the very last '\n'
                        }

                        // sb.append( "<pre>" );
                        appendNowiki( wikiText.substring( startNowiki, endNowiki ) );
                        // sb.append( "</pre>\n" );
                        pos = endPos;

                        return true;
                    }

                    // else inline <nowiki> - proceed to regular paragraph handling
                }
                else
                    if ( ( ( pos + 1 ) < wikiLength ) && ( wikiChars [pos + 1] == '|' ) )
                    { // mediawiki-table?

                        int pp;

                        for ( pp = pos + 2; ( pp < wikiLength ) && ( ( wikiChars [pp] == ' ' ) || ( wikiChars [pp] == '\t' ) ); pp++ )
                            ; // skip spaces

                        if ( ( pp == wikiLength ) || ( wikiChars [pp] == '\n' ) )
                        { // yes, it's start of a table
                            sb.append( "<div class=\"").append( _strParentTableClass ).append( "\" >" ).append("<table class=\"" ).append( _strTableClass ).append( "\"><tr><td>" );
                            mediawikiTableLevel++;
                            pos = pp + 1;

                            return pp < wikiLength;
                        }
                    }
            }
            else
                if ( ( c == '-' ) && wikiText.startsWith( "----", pos ) )
                {
                    int p;

                    for ( p = pos + 4; ( p < wikiLength ) && ( ( wikiChars [p] == ' ' ) || ( wikiChars [p] == '\t' ) ); p++ )
                        ; // skip spaces

                    if ( ( p == wikiLength ) || ( wikiChars [p] == '\n' ) )
                    {
                        sb.append( "\n<hr/>\n" );
                        pos = p;

                        return true;
                    }
                }
                else
                    if ( c == '~' )
                    { // block-level escaping: '*' '-' '#' '>' ':' '!' '|' '='

                        if ( ( pos + 1 ) < wikiLength )
                        {
                            char nc = wikiChars [pos + 1];

                            if ( ( nc == '>' ) || ( nc == ':' ) || ( nc == '-' ) || ( nc == '|' ) || ( nc == '=' ) || ( nc == '!' ) )
                            { // can't be inline markup
                                pos++; // skip '~' and proceed to regular paragraph handling
                                c = nc;
                            }
                            else
                                if ( ( nc == '*' ) || ( nc == '#' ) )
                                { // might be inline markup so need to double check

                                    char nnc = ( ( pos + 2 ) < wikiLength ) ? wikiChars [pos + 2] : 0;

                                    if ( nnc != nc )
                                    {
                                        pos++; // skip '~' and proceed to regular paragraph handling
                                        c = nc;
                                    }

                                    // otherwise escaping will be done at line level
                                }
                                else
                                    if ( nc == '{' )
                                    { // might be inline {{{ markup so need to double check

                                        char nnc = ( ( pos + 2 ) < wikiLength ) ? wikiChars [pos + 2] : 0;

                                        if ( nnc == '|' )
                                        { // mediawiki-table?
                                            pos++; // skip '~' and proceed to regular paragraph handling
                                            c = nc;
                                        }

                                        // otherwise escaping will be done at line level
                                    }
                        }
                    }

        sb.append( "<p>" );
        pos = parseItem( pos, null, ContextType.PARAGRAPH );
        sb.append( "</p>\n" );

        return true;
    }

    /**
     * Finds first closing '}}}' for nowiki block or span. Skips escaped sequences: '~}}}'.
     *
     * @param startBlock
     *            points to first char after '{{{'
     * @return position of first '}' in closing '}}}'
     */
    private int findEndOfNowiki( int startBlock )
    {
        // NOTE: this method could step back one char from startBlock position
        int endBlock = startBlock - 3;

        do
        {
            endBlock = wikiText.indexOf( "}}}", endBlock + 3 );

            if ( endBlock < 0 )
            {
                return wikiLength; // no matching '}}}' found
            }

            while ( ( ( endBlock + 3 ) < wikiLength ) && ( wikiChars [endBlock + 3] == '}' ) )
                endBlock++; // shift to end of sequence of more than 3x'}' (eg. '}}}}}')
        }
        while ( wikiChars [endBlock - 1] == '~' );

        return endBlock;
    }

    /**
     * Greedy version of findEndOfNowiki(). It finds the last possible closing '}}}' before next opening '{{{'. Also uses escapes '~{{{' and '~}}}'.
     *
     * @param startBlock
     *            points to first char after '{{{'
     * @return position of first '}' in closing '}}}'
     */
    @SuppressWarnings( "unused" )
    private int findEndOfNowikiGreedy( int startBlock )
    {
        // NOTE: this method could step back one char from startBlock position
        int nextBlock = startBlock - 3;

        do
        {
            do
            {
                nextBlock = wikiText.indexOf( "{{{", nextBlock + 3 );
            }
            while ( ( nextBlock > 0 ) && ( wikiChars [nextBlock - 1] == '~' ) );

            if ( nextBlock < 0 )
            {
                nextBlock = wikiLength;
            }

            int endBlock = wikiText.lastIndexOf( "}}}", nextBlock );

            if ( ( endBlock >= startBlock ) && ( wikiChars [endBlock - 1] != '~' ) )
            {
                return endBlock;
            }
        }
        while ( nextBlock < wikiLength );

        return wikiLength;
    }

    /**
     * @param start
     *            points to first char after pipe '|'
     * @return
     */
    private int parseTableRow( int start )
    {
        if ( start >= wikiLength )
        {
            return wikiLength;
        }

        sb.append( "<tr>" );

        boolean endOfRow = false;

        do
        {
            int colspan = 0;

            while ( ( ( start + colspan ) < wikiLength ) && ( wikiChars [start + colspan] == '|' ) )
                colspan++;

            start += colspan;
            colspan++;

            boolean th = ( start < wikiLength ) && ( wikiChars [start] == '=' );
            start += ( th ? 1 : 0 );

            while ( ( start < wikiLength ) && ( wikiChars [start] <= ' ' ) && ( wikiChars [start] != '\n' ) )
                start++; // trim whitespace from the start

            if ( ( start >= wikiLength ) || ( wikiChars [start] == '\n' ) )
            { // skip last empty column
                start++; // eat '\n'

                break;
            }

            sb.append( th ? "<th" : "<td" );

            if ( colspan > 1 )
            {
                sb.append( " colspan=\"" + colspan + "\"" );
            }

            sb.append( '>' );

            try
            {
                parseItemThrow( start, null, ContextType.TABLE_CELL );
            }
            catch( EndOfSubContextException e )
            { // end of cell
                start = e.position;

                if ( start >= wikiLength )
                {
                    endOfRow = true;
                }
                else
                    if ( wikiChars [start] == '\n' )
                    {
                        start++; // eat '\n'
                        endOfRow = true;
                    }
            }
            catch( EndOfContextException e )
            {
                start = e.position;
                endOfRow = true;
            }

            sb.append( th ? "</th>" : "</td>" );
        }
        while ( !endOfRow /* && start<wikiLength && wikiChars[start]!='\n' */);

        sb.append( "</tr>\n" );

        return start;
    }

    /**
     * Same as parseItem(); blank line adds &lt;br/&gt;&lt;br/&gt;
     *
     * @param start
     */
    private int parseListItem( int start )
    {
        while ( ( start < wikiLength ) && ( wikiChars [start] <= ' ' ) && ( wikiChars [start] != '\n' ) )
            start++; // skip spaces

        int end = parseItem( start, null, ContextType.LIST_ITEM );

        if ( ( ( listLevels [listLevel] == '>' ) || ( listLevels [listLevel] == ':' ) ) && ( wikiText.substring( start, end ).trim( ).length( ) == 0 ) )
        { // empty line within blockquote/div

            if ( !blockquoteBR )
            {
                sb.append( "<br/><br/>" );
                blockquoteBR = true;
            }
        }
        else
        {
            blockquoteBR = false;
        }

        return end;
    }

    /**
     * @param p
     *            points to first slash in suspected URI (scheme://etc)
     * @param start
     *            points to beginning of parsed item
     * @param end
     *            points to end of parsed item
     *
     * @return array of two integer offsets [begin_uri, end_uri] if matched, null otherwise
     */
    private int [ ] checkURI( int p, int start, int end )
    {
        if ( ( p > start ) && ( wikiChars [p - 1] == ':' ) )
        { // "://" found

            int pb = p - 1;

            while ( ( pb > start ) && isLatinLetterOrDigit( wikiChars [pb - 1] ) )
                pb--;

            int pe = p + 2;

            while ( ( pe < end ) && isUrlChar( wikiChars [pe] ) )
                pe++;

            URI uri = null;

            do
            {
                while ( ( pe > ( p + 2 ) ) && ( ",.;:?!%)".indexOf( wikiChars [pe - 1] ) >= 0 ) )
                    pe--; // don't want these chars at the end of URI

                try
                { // verify URL syntax
                    uri = new URI( wikiText.substring( pb, pe ) );
                }
                catch( URISyntaxException e )
                {
                    pe--; // try chopping from the end
                }
            }
            while ( ( uri == null ) && ( pe > ( p + 2 ) ) );

            if ( ( uri != null ) && uri.isAbsolute( ) && !uri.isOpaque( ) )
            {
                int [ ] offs = {
                        pb, pe
                };

                return offs;
            }
        }

        return null;
    }

    private int parseItem( int start, String delimiter, ContextType context )
    {
        try
        {
            return parseItemThrow( start, delimiter, context );
        }
        catch( EndOfContextException e )
        {
            return e.position;
        }
    }

    private int parseItemThrow( int start, String delimiter, ContextType context ) throws EndOfContextException
    {
        StringBuilder tb = new StringBuilder( );

        boolean specialCaseDelimiterHandling = "//".equals( delimiter );
        int p = start;
        int end = wikiLength;

        try
        {
            nextChar: while ( true )
            {
                if ( p >= end )
                {
                    throw new EndOfContextException( end ); // break;
                }

                if ( ( delimiter != null ) && wikiText.startsWith( delimiter, p ) )
                {
                    if ( !specialCaseDelimiterHandling || ( checkURI( p, start, end ) == null ) )
                    {
                        p += delimiter.length( );

                        return p;
                    }
                }

                char c = wikiChars [p];
                boolean atLineStart = false;

                // context-defined break test
                if ( c == '\n' )
                {
                    if ( ( context == ContextType.HEADER ) || ( context == ContextType.TABLE_CELL ) )
                    {
                        p++;
                        throw new EndOfContextException( p );
                    }

                    if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '\n' ) )
                    { // blank line delimits everything
                        p++; // eat one '\n' and leave another one unparsed so parseBlock() can close all lists
                        throw new EndOfContextException( p );
                    }

                    for ( p++; ( p < end ) && ( wikiChars [p] <= ' ' ) && ( wikiChars [p] != '\n' ); p++ )
                        ; // skip whitespace

                    if ( p >= end )
                    {
                        throw new EndOfContextException( p ); // end of text reached
                    }

                    c = wikiChars [p];
                    atLineStart = true;

                    if ( ( c == '-' ) && wikiText.startsWith( "----", p ) )
                    { // check for ---- <hr>

                        int pp;

                        for ( pp = p + 4; ( pp < end ) && ( ( wikiChars [pp] == ' ' ) || ( wikiChars [pp] == '\t' ) ); pp++ )
                            ; // skip spaces

                        if ( ( pp == end ) || ( wikiChars [pp] == '\n' ) )
                        {
                            throw new EndOfContextException( p ); // yes, it's <hr>
                        }
                    }

                    if ( LIST_CHARS.indexOf( c ) >= 0 )
                    { // start of list item?

                        if ( FORMAT_CHARS.indexOf( c ) < 0 )
                        {
                            throw new EndOfContextException( p );
                        }

                        // here we have a list char, which also happen to be a format char
                        if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] != c ) )
                        {
                            throw new EndOfContextException( p ); // format chars go in pairs
                        }

                        if ( /* context==ContextType.LIST_ITEM */
                        ( listLevel >= 0 ) && ( c == listLevels [0] ) )
                        {
                            // c matches current list's first level, so it must be new list item
                            throw new EndOfContextException( p );
                        }

                        // otherwise it must be just formatting sequence => no break of context
                    }
                    else
                        if ( c == '=' )
                        { // header
                            throw new EndOfContextException( p );
                        }
                        else
                            if ( c == '|' )
                            { // table or mediawiki-table
                                throw new EndOfContextException( p );
                            }
                            else
                                if ( c == '{' )
                                { // mediawiki-table?

                                    if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '|' ) )
                                    {
                                        int pp;

                                        for ( pp = p + 2; ( pp < end ) && ( ( wikiChars [pp] == ' ' ) || ( wikiChars [pp] == '\t' ) ); pp++ )
                                            ; // skip spaces

                                        if ( ( pp == end ) || ( wikiChars [pp] == '\n' ) )
                                        {
                                            throw new EndOfContextException( p ); // yes, it's start of a table
                                        }
                                    }
                                }

                    // if none matched add '\n' to text buffer
                    tb.append( '\n' );

                    // p and c already shifted past the '\n' and whitespace after, so go on
                }
                else
                    if ( c == '|' )
                    {
                        if ( context == ContextType.TABLE_CELL )
                        {
                            p++;
                            throw new EndOfSubContextException( p );
                        }
                    }

                int formatType;

                if ( c == '{' )
                {
                    if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '{' ) )
                    {
                        if ( ( ( p + 2 ) < end ) && ( wikiChars [p + 2] == '{' ) )
                        { // inline or block <nowiki>
                            appendText( tb.toString( ) );
                            tb.delete( 0, tb.length( ) ); // flush text buffer

                            int startNowiki = p + 3;
                            int endNowiki = findEndOfNowiki( startNowiki );
                            p = endNowiki + 3;

                            if ( wikiText.lastIndexOf( '\n', endNowiki ) >= startNowiki )
                            { // block <pre>

                                if ( wikiChars [startNowiki] == '\n' )
                                {
                                    startNowiki++; // skip the very first '\n'
                                }

                                if ( wikiChars [endNowiki - 1] == '\n' )
                                {
                                    endNowiki--; // omit the very last '\n'
                                }

                                if ( context == ContextType.PARAGRAPH )
                                {
                                    sb.append( "</p>" ); // break the paragraph because XHTML does not allow <pre> children of <p>
                                }

                                // sb.append( "<pre>" );
                                appendNowiki( wikiText.substring( startNowiki, endNowiki ) );
                                // sb.append( "</pre>\n" );

                                if ( context == ContextType.PARAGRAPH )
                                {
                                    sb.append( "<p>" ); // continue the paragraph
                                                        // if (context==ContextType.NOWIKI_BLOCK) return p; // in this context return immediately after nowiki
                                }
                            }
                            else
                            { // inline <nowiki>
                                appendNowiki( wikiText.substring( startNowiki, endNowiki ) );
                            }

                            continue;
                        }
                        else
                            if ( ( p + 2 ) < end )
                            { // {{image}}

                                int endImg = wikiText.indexOf( "}}", p + 2 );

                                if ( ( endImg >= 0 ) && ( endImg < end ) )
                                {
                                    appendText( tb.toString( ) );
                                    tb.delete( 0, tb.length( ) ); // flush text buffer
                                    appendImage( wikiText.substring( p + 2, endImg ) );
                                    p = endImg + 2;

                                    continue;
                                }
                            }
                    }
                }
                else
                    if ( c == '[' )
                    {
                        if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '[' ) )
                        { // [[link]]

                            int endLink = wikiText.indexOf( "]]", p + 2 );

                            if ( ( endLink >= 0 ) && ( endLink < end ) )
                            {
                                appendText( tb.toString( ) );
                                tb.delete( 0, tb.length( ) ); // flush text buffer
                                appendLink( wikiText.substring( p + 2, endLink ) );
                                p = endLink + 2;

                                continue;
                            }
                        }
                    }
                    else
                        if ( c == '\\' )
                        {
                            if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '\\' ) )
                            { // \\ = <br/>
                                appendText( tb.toString( ) );
                                tb.delete( 0, tb.length( ) ); // flush text buffer
                                sb.append( "<br/>" );
                                p += 2;

                                continue;
                            }
                        }
                        else
                            if ( c == '<' )
                            {
                                if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == '<' ) )
                                {
                                    if ( ( ( p + 2 ) < end ) && ( wikiChars [p + 2] == '<' ) )
                                    { // <<<macro>>>

                                        int endMacro = wikiText.indexOf( ">>>", p + 3 );

                                        if ( ( endMacro >= 0 ) && ( endMacro < end ) )
                                        {
                                            appendText( tb.toString( ) );
                                            tb.delete( 0, tb.length( ) ); // flush text buffer
                                            appendMacro( wikiText.substring( p + 3, endMacro ) );
                                            p = endMacro + 3;

                                            continue;
                                        }
                                    }
                                }
                            }
                            else
                                if ( ( formatType = FORMAT_CHARS.indexOf( c ) ) >= 0 )
                                {
                                    if ( ( ( p + 1 ) < end ) && ( wikiChars [p + 1] == c ) )
                                    {
                                        appendText( tb.toString( ) );
                                        tb.delete( 0, tb.length( ) ); // flush text buffer

                                        if ( c == '/' )
                                        { // special case for "//" - check if it is part of URL (scheme://etc)

                                            int [ ] uriOffs = checkURI( p, start, end );

                                            if ( uriOffs != null )
                                            {
                                                int pb = uriOffs [0];
                                                int pe = uriOffs [1];

                                                if ( ( pb > start ) && ( wikiChars [pb - 1] == '~' ) )
                                                {
                                                    sb.delete( sb.length( ) - ( p - pb + 1 ), sb.length( ) ); // roll back URL + tilde
                                                    sb.append( escapeHTML( wikiText.substring( pb, pe ) ) );
                                                }
                                                else
                                                {
                                                    sb.delete( sb.length( ) - ( p - pb ), sb.length( ) ); // roll back URL
                                                    appendLink( wikiText.substring( pb, pe ) );
                                                }

                                                p = pe;

                                                continue;
                                            }
                                        }

                                        sb.append( FORMAT_TAG_OPEN [formatType] );

                                        try
                                        {
                                            p = parseItemThrow( p + 2, FORMAT_DELIM [formatType], context );
                                        }
                                        finally
                                        {
                                            sb.append( FORMAT_TAG_CLOSE [formatType] );
                                        }

                                        continue;
                                    }
                                }
                                else
                                    if ( c == '~' )
                                    { // escape
                                      // most start line escapes are dealt with in parseBlock()

                                        if ( atLineStart )
                                        {
                                            // same as block-level escaping: '*' '-' '#' '>' ':' '|' '='
                                            if ( ( p + 1 ) < end )
                                            {
                                                char nc = wikiChars [p + 1];

                                                if ( ( nc == '>' ) || ( nc == ':' ) || ( nc == '-' ) || ( nc == '|' ) || ( nc == '=' ) || ( nc == '!' ) )
                                                { // can't be inline markup
                                                    tb.append( nc );
                                                    p += 2; // skip '~' and nc

                                                    continue nextChar;
                                                }
                                                else
                                                    if ( ( nc == '*' ) || ( nc == '#' ) )
                                                    { // might be inline markup so need to double check

                                                        char nnc = ( ( p + 2 ) < end ) ? wikiChars [p + 2] : 0;

                                                        if ( nnc != nc )
                                                        {
                                                            tb.append( nc );
                                                            p += 2; // skip '~' and nc

                                                            continue nextChar;
                                                        }

                                                        // otherwise escaping will be done at line level
                                                    }
                                                    else
                                                        if ( nc == '{' )
                                                        { // might be inline {{{ markup so need to double check

                                                            char nnc = ( ( p + 2 ) < end ) ? wikiChars [p + 2] : 0;

                                                            if ( nnc == '|' )
                                                            { // mediawiki-table?
                                                                tb.append( nc );
                                                                tb.append( nnc );
                                                                p += 3; // skip '~', nc and nnc

                                                                continue nextChar;
                                                            }

                                                            // otherwise escaping will be done as usual at line level
                                                        }
                                            }
                                        }

                                        for ( String e : ESCAPED_INLINE_SEQUENCES )
                                        {
                                            if ( wikiText.startsWith( e, p + 1 ) )
                                            {
                                                tb.append( e );
                                                p += ( 1 + e.length( ) );

                                                continue nextChar;
                                            }
                                        }
                                    }
                                    else
                                        if ( c == '-' )
                                        { // ' -- ' => &ndash;

                                            if ( ( ( p + 2 ) < end ) && ( wikiChars [p + 1] == '-' ) && ( wikiChars [p + 2] == ' ' ) && ( p > start )
                                                    && ( wikiChars [p - 1] == ' ' ) )
                                            {
                                                // appendText(tb.toString()); tb.delete(0, tb.length()); // flush text buffer
                                                // sb.append("&ndash; ");
                                                tb.append( "&ndash; " ); // &ndash; = "\u2013 "
                                                p += 3;

                                                continue;
                                            }
                                        }

                tb.append( c );
                p++;
            }
        }
        finally
        {
            appendText( tb.toString( ) );
            tb.delete( 0, tb.length( ) ); // flush text buffer
        }
    }

    protected void appendMacro( String text )
    {
        if ( "TOC".equals( text ) )
        {
            sb.append( "!!!TOC!!!" ); // put TOC placeholder for replacing it later with real TOC
        }
        else
        {
            sb.append( "&lt;&lt;&lt;Macro:" );
            sb.append( escapeHTML( unescapeHTML( text ) ) );
            sb.append( "&gt;&gt;&gt;" );
        }
    }

    protected void appendLink( String text )
    {
        String [ ] link = split( text, '|' );
        URI uri = null;

        try
        { // validate URI
            uri = new URI( link [0].trim( ) );
        }
        catch( URISyntaxException e )
        {
        }

        if ( ( uri != null ) && uri.isAbsolute( ) && !uri.isOpaque( ) )
        {
            sb.append( "<a href=\"" + escapeHTML( uri.toString( ) ) + "\" rel=\"nofollow\">" );
            sb.append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link [1].trim( ) ) ) ? link [1] : link [0] ) ) );
            sb.append( "</a>" );
        }
        else
        {
            sb.append( "<a href=\"#\" title=\"Internal link\">" );
            sb.append( escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link [1].trim( ) ) ) ? link [1] : link [0] ) ) );
            sb.append( "</a>" );
        }
    }

    protected void appendImage( String text )
    {
        String [ ] link = split( text, '|' );
        URI uri = null;

        try
        { // validate URI
            uri = new URI( link [0].trim( ) );
        }
        catch( URISyntaxException e )
        {
        }

        if ( ( uri != null ) && uri.isAbsolute( ) && !uri.isOpaque( ) )
        {
            String alt = escapeHTML( unescapeHTML( ( ( link.length >= 2 ) && !isEmpty( link [1].trim( ) ) ) ? link [1] : link [0] ) );
            sb.append( "<img src=\"" + escapeHTML( uri.toString( ) ) + "\" alt=\"" + alt + "\" title=\"" + alt + "\" />" );
        }
        else
        {
            sb.append( "&lt;&lt;&lt;Internal image(?): " );
            sb.append( escapeHTML( unescapeHTML( text ) ) );
            sb.append( "&gt;&gt;&gt;" );
        }
    }

    protected void appendText( String text )
    {
        sb.append( escapeHTML( unescapeHTML( text ) ) );
    }

    protected String generateTOCAnchorId( int hLevel, String text )
    {
        int i = 0;
        String id = ( ( HEADING_ID_PREFIX != null ) ? HEADING_ID_PREFIX : ( "H" + hLevel + "_" ) )
                + translit( text.replaceAll( "<.+?>", "" ) ).trim( ).replaceAll( "\\s+", "_" ).replaceAll( "[^a-zA-Z0-9_-]", "" );

        while ( tocAnchorIds.contains( id ) )
        { // avoid duplicates
            i++;
            id = text + "_" + i;
        }

        tocAnchorIds.add( id );

        return id;
    }

    protected void appendTOCItem( int level, String anchorId, String text )
    {
        if ( level > tocLevel )
        {
            while ( level > tocLevel )
            {
                toc.append( "<ul><li>" );
                tocLevel++;
            }
        }
        else
        {
            while ( level < tocLevel )
            {
                toc.append( "</li></ul>" );
                tocLevel--;
            }

            toc.append( "</li>\n<li>" );
        }

        toc.append( "<a href='#page_url#" + anchorId + "'>" + text + "</a>" );
    }

    protected void completeTOC( )
    {
        while ( 0 < tocLevel )
        {
            toc.append( "</li></ul>" );
            tocLevel--;
        }

        int idx;
        String tocDiv = "<div class=\"" + _strTocClass + "\">" + toc.toString( ) + "</div>";

        while ( ( idx = sb.indexOf( "!!!TOC!!!" ) ) >= 0 )
        {
            sb.replace( idx, idx + 9, tocDiv );
        }
    }

    protected void appendNowiki( String text )
    {
        sb.append( escapeHTML( replaceString( replaceString( text, "~{{{", "{{{" ), "~}}}", "}}}" ) ) );
    }

    private static class EndOfContextException extends Exception
    {
        private static final long serialVersionUID = 1L;
        int position;

        public EndOfContextException( int position )
        {
            super( );
            this.position = position;
        }
    }

    private static class EndOfSubContextException extends EndOfContextException
    {
        private static final long serialVersionUID = 1L;

        public EndOfSubContextException( int position )
        {
            super( position );
        }
    }

    private static enum ContextType
    {
        PARAGRAPH, LIST_ITEM, TABLE_CELL, HEADER, NOWIKI_BLOCK;
    }
}
