package dev.jackraidenph.nliis.backend.utility;

import org.springframework.ai.reader.pdf.config.ParagraphManager.Paragraph;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StringUtilities {

    private static final String MATCH_POSSESSIVE_SINGULAR_FORM_FANCY_APOSTROPHE = "(\\p{L}+)[’‘](\\p{L}+)";
    private static final String MATCH_POSSESSIVE_PLURAL_FORM_FANCY_APOSTROPHE = "(s)[’‘]([^\\p{L}])";
    private static final String MATCH_POSSESSIVE_FORM_FANCY_APOSTROPHE =
            MATCH_POSSESSIVE_SINGULAR_FORM_FANCY_APOSTROPHE + "|" + MATCH_POSSESSIVE_PLURAL_FORM_FANCY_APOSTROPHE;

    private static final String CAPTURE_EITHER_APOSTROPHE_PAIR = "$1$3'$2$4";

    private static final String MATCH_FANCY_CHARACTERS = "([^\\p{L}'\\-\\s\\d_])";
    private static final String MATCH_FANCY_CHARACTERS_BEFORE_NORMAL_APOSTROPHE = "([^\\p{L}\\d_]')";
    private static final String MATCH_REMAINING_FANCY =
            MATCH_FANCY_CHARACTERS + "|" + MATCH_FANCY_CHARACTERS_BEFORE_NORMAL_APOSTROPHE;

    private static final String MATCH_TWO_OR_MORE_WHITESPACES = "\\s{2,}";

    //ABBREVIATIONS
    private static final String MPFFA = MATCH_POSSESSIVE_FORM_FANCY_APOSTROPHE;

    //PUBLIC PATTERNS
    public static final String ONE_OR_MORE_SPACES = "\\s+";
    public static final String MATCH_NEW_LINE = "\\s*\r?\n";

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy-HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    /**
     * @param string The string to be sanitized
     * @return sanitized string (Fancy characters removed, fancy apostrophes replaced with regular ones, dashes preserved)
     */
    public static String sanitize(String string) {
        return string
                .toLowerCase()
                .replaceAll(MPFFA, CAPTURE_EITHER_APOSTROPHE_PAIR)
                .replaceAll(MATCH_NEW_LINE, " ")
                .replaceAll(MATCH_REMAINING_FANCY, " ")
                .replaceAll(MATCH_TWO_OR_MORE_WHITESPACES, " ")
                .strip();
    }

    public static String normalizeForDocument(String text) {
        return text
                .replaceAll("[“”«»]", "\"")
                .replaceAll(MPFFA, CAPTURE_EITHER_APOSTROPHE_PAIR)
                .replaceAll(MATCH_TWO_OR_MORE_WHITESPACES, " ")
                .strip();
    }

    public static List<Paragraph> splitIntoParagraphs(String text) {
        String[] splitText = text.split("\r?\n\\s+");
        if (splitText.length == 0) {
            return List.of();
        }
        List<Paragraph> paragraphs = new ArrayList<>();
        for (String content : splitText) {
            int start = text.indexOf(content);
            int stop = start + content.length();
            paragraphs.add(new Paragraph(content, start, stop));
        }
        return paragraphs;
    }

    public record Paragraph(String text, int start, int stop) {

    }

}
