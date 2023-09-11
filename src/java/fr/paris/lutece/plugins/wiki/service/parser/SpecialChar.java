package fr.paris.lutece.plugins.wiki.service.parser;

public class SpecialChar {

    /**
     * Render specific entities
     *
     * @param strSource
     *            The source
     * @return The source transformed
     */
    public static String renderWiki( String strSource )
    {
        String strRender = strSource;
        strRender = strRender.replaceAll("\\[@MarkdownLanguage;", "");
        strRender = strRender.replaceAll( "\\[lt;", "<" );
        strRender = strRender.replaceAll( "\\[gt;", ">" );
        strRender = strRender.replaceAll( "\\[nbsp;", "&nbsp;" );
        strRender = strRender.replaceAll( "\\[quot;", "'" );
        strRender = strRender.replaceAll( "\\[amp;", "&" );
        strRender = strRender.replaceAll( "\\[hashmark;", "#" );
        strRender = strRender.replaceAll("\\[codeQuote;", "`");
        strRender = strRender.replaceAll("\\[simpleQuote;", "'");
        return strRender;
    }
    public static String reverseRender ( String str )
    {

        str = str.replaceAll("<", "\\[lt;");
        str = str.replaceAll(">", "\\[gt;");
        str = str.replaceAll("&nbsp;", "\\[nbsp;");
        str = str.replaceAll("'", "\\[quot;");
        str = str.replaceAll("&", "\\[amp;");
        str = str.replaceAll("#", "\\[hashmark;");
        str = str.replaceAll("`", "\\[codeQuote;");
        str = str.replaceAll("'", "\\[simpleQuote;");
        str = str.replaceAll("'", "\\[simpleQuote;");
        str = str.replaceAll("\\\\", "");

        return str;
    }


}
