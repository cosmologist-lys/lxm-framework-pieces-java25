package org.example;

import com.lxm.framework.enigma.crypto.EnigmaCrypto;
import com.lxm.framework.enigma.protocol.*;
import org.junit.jupiter.api.*;
import org.springframework.context.ConfigurableApplicationContext;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class EnigmaHttpTest {
    @Test void ordinaryAsyncEndpointStillWorksWhenEnigmaIsEnabled() throws Exception {
        var response=http.send(HttpRequest.newBuilder(URI.create(origin+"/demo/async")).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,response.statusCode());assertEquals("async",ProtocolJson.read(response.body()).path("data").asString());
        assertTrue(response.headers().firstValue("X-Enigma-Mode").isEmpty());
    }
    static ConfigurableApplicationContext application;
    static String origin;
    static final HttpClient http=HttpClient.newHttpClient();
    static final EnigmaCrypto crypto=new EnigmaCrypto();
    Map<String,String> keys;String sid,kid;
    @BeforeAll static void start() {
        application=EnigmaDemoApplication.start(0);origin="http://127.0.0.1:"+application.getEnvironment().getProperty("local.server.port");
    }
    @AfterAll static void stop() { if(application!=null)application.close(); }
    @BeforeEach void handshake() throws Exception {
        var response=http.send(HttpRequest.newBuilder(URI.create(origin+"/enigma/session")).header("Authorization","Bearer browser-test").POST(HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(200,response.statusCode());assertEquals("no-store",response.headers().firstValue("Cache-Control").orElseThrow());
        var grant=ProtocolJson.read(response.body());sid=grant.path("sid").asString();kid=grant.path("kid").asString();
        keys=new HashMap<>();grant.path("keys").properties().forEach(pair->keys.put(pair.getKey(),pair.getValue().asString()));
    }
    RequestContext context(String method,String path,String mode) {
        var uri=URI.create(origin+path);
        return new RequestContext(1,mode,"request",sid,kid,method,uri.getRawPath(),RequestContext.query(uri.getRawQuery()),mode.equals("ENCRYPT")?"application/json":"",Long.toString(System.currentTimeMillis()),crypto.randomId());
    }
    HttpRequest request(RequestContext context,byte[] plaintext,String signatureOverride) {
        String target=context.path();
        if (!context.params().isEmpty()) {
            var query=new ArrayList<String>();context.params().forEach((key,list)->list.forEach(value->query.add(java.net.URLEncoder.encode(key,StandardCharsets.UTF_8)+"="+java.net.URLEncoder.encode(value,StandardCharsets.UTF_8))));
            target+="?"+String.join("&",query);
        }
        var builder=HttpRequest.newBuilder(URI.create(origin+target)).header("Authorization","Bearer browser-test");
        builder.header("X-Enigma-Version","1").header("X-Enigma-Mode",context.mode()).header("X-Enigma-Session",sid).header("X-Enigma-Kid",kid)
            .header("X-Enigma-Timestamp",context.timestamp()).header("X-Enigma-Nonce",context.nonce());
        byte[] body=new byte[0];
        if(context.mode().equals("SIGN")) builder.header("X-Enigma-Sign",signatureOverride==null?EnigmaCrypto.encode(crypto.sign(EnigmaCrypto.decode(keys.get("requestSigning"),32),ProtocolJson.canonical(context))):signatureOverride);
        else {
            String iv=EnigmaCrypto.encode(crypto.randomBytes(12));
            body=ProtocolJson.MAPPER.writeValueAsBytes(Map.of("v",1,"kid",kid,"iv",iv,"ciphertext",EnigmaCrypto.encode(crypto.encrypt(EnigmaCrypto.decode(keys.get("requestEncryption"),32),EnigmaCrypto.decode(iv,12),ProtocolJson.canonical(context),plaintext))));
            builder.header("Content-Type","application/json");
        }
        return builder.method(context.method(),HttpRequest.BodyPublishers.ofByteArray(body)).build();
    }
    tools.jackson.databind.JsonNode verified(RequestContext request,HttpResponse<byte[]> response) {
        var value=ProtocolJson.read(response.body());assertEquals("1",response.headers().firstValue("X-Enigma-Version").orElseThrow());
        assertEquals(sid,response.headers().firstValue("X-Enigma-Session").orElseThrow());assertEquals(kid,response.headers().firstValue("X-Enigma-Kid").orElseThrow());
        var authentication=new LinkedHashMap<String,Object>();authentication.put("v",1);authentication.put("request",request);authentication.put("direction","response");
        authentication.put("timestamp",response.headers().firstValue("X-Enigma-Timestamp").orElseThrow());authentication.put("nonce",response.headers().firstValue("X-Enigma-Nonce").orElseThrow());
        String mode=response.headers().firstValue("X-Enigma-Mode").orElseThrow();authentication.put("status",response.statusCode());authentication.put("code",value.path("code").asInt());
        authentication.put("message",value.path("message").asString());authentication.put("protection",mode);authentication.put("unexecuted",Boolean.parseBoolean(response.headers().firstValue("X-Enigma-Unexecuted").orElseThrow()));
        if(mode.equals("SIGN")) {authentication.put("data",value.get("data"));crypto.verify(EnigmaCrypto.decode(keys.get("responseSigning"),32),ProtocolJson.canonical(authentication),value.path("sign").asString());return value;}
        assertEquals("ENCRYPT",mode);var envelope=value.get("data");
        byte[] plain=crypto.decrypt(EnigmaCrypto.decode(keys.get("responseEncryption"),32),EnigmaCrypto.decode(envelope.path("iv").asString(),12),ProtocolJson.canonical(authentication),EnigmaCrypto.decode(envelope.path("ciphertext").asString(),-1));
        var decoded=(tools.jackson.databind.node.ObjectNode)value;decoded.set("data",ProtocolJson.read(plain));return decoded;
    }
    @Test void encryptedDtoIsValidatedThenFilteredAndResponseStatusIsBound() throws Exception {
        var context=context("POST","/demo/body","ENCRYPT");var response=http.send(request(context,"{\"name\":\"中文\",\"secret\":\"hidden\"}".getBytes(StandardCharsets.UTF_8),null),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(201,response.statusCode());var value=verified(context,response);assertEquals("中文",value.path("data").path("name").asString());assertFalse(value.path("data").has("secret"));
    }
    @Test void noBodyPutAuthenticatesDuplicateEmptyUnicodeParameters() throws Exception {
        var context=context("PUT","/demo/params?q=%E4%B8%AD%E6%96%87&q=%2B&q=&bare","SIGN");
        var response=http.send(request(context,null,null),HttpResponse.BodyHandlers.ofByteArray());assertEquals(200,response.statusCode());
        assertEquals(List.of("中文","+",""),ProtocolJson.MAPPER.convertValue(verified(context,response).path("data").path("q"),new tools.jackson.core.type.TypeReference<List<String>>(){}));
    }
    @Test void replayAndTamperCannotExecuteController() throws Exception {
        int before=EnigmaDemoApplication.calls.get();var context=context("GET","/demo/params/42?query=value","SIGN");var signed=request(context,null,null);
        var first=http.send(signed,HttpResponse.BodyHandlers.ofByteArray());assertEquals(200,first.statusCode());verified(context,first);
        var replay=http.send(signed,HttpResponse.BodyHandlers.ofByteArray());assertEquals(409,replay.statusCode());assertEquals(6204,verified(context,replay).path("code").asInt());
        assertEquals(before+1,EnigmaDemoApplication.calls.get());
        var altered=context("GET","/demo/params/42?query=other","SIGN");var invalid=http.send(request(altered,null,EnigmaCrypto.encode(new byte[32])),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(400,invalid.statusCode());assertEquals("NONE",invalid.headers().firstValue("X-Enigma-Mode").orElseThrow());assertEquals(before+1,EnigmaDemoApplication.calls.get());
    }
    @Test void dtoFailureAndBusinessErrorAreAuthenticatedWithoutInternalMessage() throws Exception {
        int before=EnigmaDemoApplication.calls.get();var context=context("POST","/demo/body","ENCRYPT");
        var invalid=http.send(request(context,"{\"name\":\"\",\"secret\":\"hidden\"}".getBytes(StandardCharsets.UTF_8),null),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(400,invalid.statusCode());assertEquals(400,verified(context,invalid).path("code").asInt());assertEquals(before,EnigmaDemoApplication.calls.get());
        var failure=context("GET","/demo/error","SIGN");var response=http.send(request(failure,null,null),HttpResponse.BodyHandlers.ofByteArray());
        assertEquals(500,response.statusCode());assertFalse(verified(failure,response).path("message").asString().contains("internal-secret"));
    }
}
