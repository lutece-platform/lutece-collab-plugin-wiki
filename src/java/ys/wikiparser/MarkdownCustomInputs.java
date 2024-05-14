package ys.wikiparser;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.util.html.HtmlTemplate;
import  ys.wikiparser.XSSSanitizer;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import  java.io.File;
import org.apache.commons.io.FileUtils;
import java.nio.charset.StandardCharsets;
public class MarkdownCustomInputs
{

    public static final String PROPERTY_CUSTOM_TEMPLATE_PATH = "wiki.custom.template.path";
    public static final String TEMPLATE_SUFFIX = ".html";
    private static final String PROPERTY_PATH_TEMPLATE = "path.templates";

    public static final String VAR_OPEN = "${";
    public static final String VAR_CLOSE = "}";

    /**
     * Extract the parameters from the input text
     * @param inputText
     * @return the parameters
     */
    public static Map<String, String> extractParams( String inputText)
    {
        Map<String, String> paramMap = new java.util.HashMap<>( );
                // Define the regular expression pattern to match key={{value}} pairs
        String regex = "(\\w+)=\\{\\{([^}]*)\\}\\}";

        // Compile the pattern
        Pattern pattern = Pattern.compile( regex );

        // Create a matcher for the input string
        Matcher matcher = pattern.matcher( inputText );

        // Create a HashMap to store the parameters and their arguments

        // Find all matches and extract the parameters and their arguments
        while ( matcher.find( ) )
        {
            // Get the parameter name (group 1) and the argument (group 2)
            String param = matcher.group( 1 );
            String arg = matcher.group( 2 );
            if(arg == null){
                arg = "";
            }
            paramMap.put( param.toString(), arg.toString() );
        }
        return paramMap;
    }
    /**
     * Fill the template with the model
     * @param template
     * @param model
     * @return the filled template
     */
   public static String fillTemplate (String template, Map<String, String> model){
       HtmlTemplate htmlTemplate = new HtmlTemplate( template );
        for ( String key : model.keySet( ) )
        {
            htmlTemplate.substitute( VAR_OPEN + key + VAR_CLOSE, model.get( key ));
        }
       String sanitizedHtml = XSSSanitizer.sanitize( htmlTemplate.getHtml( ) );
       return sanitizedHtml;
    }

    /**
     *   if there is a template matching the custom input name in the custom template path, render it
     * @param inputText
     * @param customInputName
     * @return the rendered html
     */
    public static String renderCustomInHtml( String inputText, String customInputName )
    {
       String html = "";
        try
        {
            Map<String, String> model = extractParams( inputText );
            String templatePath =  AppPathService.getPath( PROPERTY_PATH_TEMPLATE ) + AppPropertiesService.getProperty( PROPERTY_CUSTOM_TEMPLATE_PATH ) + File.separator + customInputName + TEMPLATE_SUFFIX;
            File file = new File( templatePath );
            html = FileUtils.readFileToString( file, StandardCharsets.UTF_8 );
            html = fillTemplate( html, model );
        }
      catch( Exception e){
            String errorMessage = "Issue for custom input: " + customInputName;
            AppLogService.error( errorMessage );
            AppLogService.error(e.getMessage());
            html = "<div style='color:red;font-weight:bold'>"+ errorMessage + "</div>";
      }

        return html;
    }
}
