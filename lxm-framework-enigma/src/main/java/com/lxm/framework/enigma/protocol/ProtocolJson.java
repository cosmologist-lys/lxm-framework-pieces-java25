package com.lxm.framework.enigma.protocol;

import com.lxm.framework.enigma.EnigmaException;
import org.erdtman.jcs.JsonCanonicalizer;
import tools.jackson.core.*;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.*;
import tools.jackson.databind.json.JsonMapper;
import java.nio.charset.*;
import java.nio.ByteBuffer;
import java.io.IOException;

public final class ProtocolJson {
    public static final JsonMapper MAPPER=JsonMapper.builder(JsonFactory.builder()
        .enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION)
        .streamReadConstraints(StreamReadConstraints.builder().maxNestingDepth(64).maxStringLength(10_485_760).maxNumberLength(128).build()).build()).enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();
    private ProtocolJson() { }
    public static String utf8(byte[] bytes) {
        try { return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(bytes)).toString(); }
        catch (CharacterCodingException invalid) { throw EnigmaException.protocol(); }
    }
    public static JsonNode read(byte[] bytes) {
        try { var value=MAPPER.readTree(utf8(bytes));validate(value);return value; }
        catch (JacksonException invalid) { throw EnigmaException.protocol(); }
    }
    public static byte[] canonical(Object value) {
        var node=value instanceof JsonNode json ? json : MAPPER.valueToTree(value);
        validate(node);
        try { return new JsonCanonicalizer(MAPPER.writeValueAsString(node)).getEncodedUTF8(); }
        catch (IOException invalid) { throw EnigmaException.protocol(); }
    }
    private static void validate(JsonNode node) {
        if (node==null) throw EnigmaException.protocol();
        if (node.isString()) unicode(node.stringValue());
        if (node.isNumber()) {
            double value=node.doubleValue();
            if (!Double.isFinite(value) || (Math.rint(value)==value && Math.abs(value)>9_007_199_254_740_991d)) throw EnigmaException.protocol();
        }
        if (node.isObject()) for (var property:node.properties()) { unicode(property.getKey());validate(property.getValue()); }
        if (node.isArray()) for (var item:node) validate(item);
    }
    public static void unicode(String text) {
        for (int i=0;i<text.length();i++) {
            char ch=text.charAt(i);
            if (Character.isHighSurrogate(ch)) {
                if (++i>=text.length() || !Character.isLowSurrogate(text.charAt(i))) throw EnigmaException.protocol();
            } else if (Character.isLowSurrogate(ch)) throw EnigmaException.protocol();
        }
    }
}
