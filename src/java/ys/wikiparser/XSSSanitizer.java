package ys.wikiparser;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

public class XSSSanitizer {

    // Create a PolicyFactory to define what HTML tags and attributes are allowed
    private static final PolicyFactory POLICY_FACTORY = new HtmlPolicyBuilder()
            .allowElements("a", "b", "i", "u", "p", "br", "div", "span", "ul", "ol", "li", "strong", "em", "strike", "sub", "sup", "pre", "code", "blockquote", "hr", "h1", "h2", "h3", "h4", "h5", "h6", "table", "thead", "tbody", "tfoot", "tr", "th", "td", "caption", "img")
            .allowAttributes("href").onElements("a")
            .allowAttributes("style").onElements("div", "span", "p")
            .allowAttributes("class").globally()
            .toFactory();

    public static String sanitize(String input) {
        // Ensure the input is a string
        if (input == null) {
            return null;
        }

        // Use the POLICY_FACTORY to sanitize the input
        return POLICY_FACTORY.sanitize(input);
    }

}
