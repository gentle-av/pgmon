package avr.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class Json5Parser {

    private final ObjectMapper mapper;

    public Json5Parser() {
        this.mapper = new ObjectMapper();
    }

    public <T> T parse(InputStream input, Class<T> clazz) throws Exception {
        String content = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        String cleaned = stripJson5(content);
        return mapper.readValue(cleaned, clazz);
    }

    private String stripJson5(String json5) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        int i = 0;
        while (i < json5.length()) {
            char c = json5.charAt(i);
            char next = i + 1 < json5.length() ? json5.charAt(i + 1) : 0;
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i += 2;
                } else {
                    i++;
                }
                continue;
            }
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                    result.append(c);
                }
                i++;
                continue;
            }
            if (!inString && c == '/' && next == '/') {
                inLineComment = true;
                i += 2;
                continue;
            }
            if (!inString && c == '/' && next == '*') {
                inBlockComment = true;
                i += 2;
                continue;
            }
            if (c == '"' && !inLineComment && !inBlockComment) {
                inString = !inString;
                result.append(c);
                i++;
                continue;
            }
            if (!inLineComment && !inBlockComment) {
                result.append(c);
            }
            i++;
        }
        return result.toString();
    }
}
