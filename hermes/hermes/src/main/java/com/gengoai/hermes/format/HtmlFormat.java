package com.gengoai.hermes.format;

import com.gengoai.hermes.Document;
import com.gengoai.hermes.corpus.DocumentCollection;
import com.gengoai.io.resource.Resource;
import org.jsoup.Jsoup;
import org.jsoup.internal.StringUtil;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.kohsuke.MetaInfServices;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;
import java.util.stream.Stream;

public class HtmlFormat extends WholeFileTextFormat implements OneDocPerFileFormat, Serializable {
    private static final long serialVersionUID = 1234567L;
    private final DocFormatParameters parameters;

    public HtmlFormat(DocFormatParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public DocFormatParameters getParameters() {
        return parameters;
    }

    @Override
    public void write(DocumentCollection documentCollection, Resource outputResource) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(Document document, Resource outputResource) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Stream<Document> readSingleFile(String content) {
        return Stream.of(parameters.getDocumentFactory().create(
                new HtmlToPlainText().getPlainText(Jsoup.parse(content).body())));
    }

    @MetaInfServices
    public static class Provider implements DocFormatProvider {

        @Override
        public DocFormat create(DocFormatParameters parameters) {
            return new HtmlFormat(parameters);
        }

        @Override
        public String getName() {
            return "HTML";
        }

        @Override
        public boolean isWriteable() {
            return true;
        }
    }


    /**
     * HTML to plain-text. This example program demonstrates the use of jsoup to convert HTML input to lightly-formatted
     * plain-text. That is divergent from the general goal of jsoup's .text() methods, which is to get clean data from a
     * scrape.
     * <p>
     * Note that this is a fairly simplistic formatter -- for real world use you'll want to embrace and extend.
     * </p>
     * <p>
     * To invoke from the command line, assuming you've downloaded the jsoup jar to your current directory:</p>
     * <p><code>java -cp jsoup.jar org.jsoup.examples.HtmlToPlainText url [selector]</code></p>
     * where <i>url</i> is the URL to fetch, and <i>selector</i> is an optional CSS selector.
     *
     * @author Jonathan Hedley, jonathan@hedley.net
     */
    public class HtmlToPlainText {
        private final Set<String> BLOCK_LEVEL_TAGS = Set.of("table",
                                                            "div",
                                                            "p",
                                                            "h1",
                                                            "h2",
                                                            "h3",
                                                            "h4",
                                                            "h5",
                                                            "h6",
                                                            "ol",
                                                            "ul",
                                                            "address",
                                                            "blockquote",
                                                            "center",
                                                            "dir",
                                                            "dl",
                                                            "filedest",
                                                            "form",
                                                            "isindex",
                                                            "menu",
                                                            "noframes",
                                                            "noscript",
                                                            "pre",
                                                            "dt",
                                                            "frameset");

        /**
         * Format an Element to plain-text
         *
         * @param element the root element to format
         * @return formatted text
         */
        public String getPlainText(Element element) {
            FormattingVisitor formatter = new FormattingVisitor();
            NodeTraversor.traverse(formatter, element); // walk the DOM, and call .head() and .tail() for each node
            return formatter.toString();
        }

        // the formatting rules, implemented in a breadth-first DOM traverse
        private class FormattingVisitor implements NodeVisitor {
            private StringBuilder accum = new StringBuilder(); // holds the accumulated text

            public void head(Node node, int depth) {
                String name = node.nodeName().toLowerCase();
                if (node instanceof TextNode) {
                    append(((TextNode) node).text()); // TextNodes carry all user-readable text in the DOM.
                } else if (name.equals("li")) {
                    append("\n * ");
                } else if (name.equals("dt")) {
                    append("  ");
                } else if (BLOCK_LEVEL_TAGS.contains(name)) {
                    append("\n");
                }
            }

            public void tail(Node node, int depth) {
                if (BLOCK_LEVEL_TAGS.contains(node.nodeName().toLowerCase())) {
                    append("\n");
                }
            }

            // appends text to the string builder with a simple word wrap method
            private void append(String text) {
                if (text.equals(" ") &&
                        (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
                    return; // don't accumulate long runs of empty spaces
                }
                accum.append(text);
            }

            @Override
            public String toString() {
                return accum.toString();
            }
        }
    }

}//END OF HtmlFormat
