package com.lxm.framework.web;
import com.lxm.framework.web.http.HttpClient;
import com.lxm.framework.common.AppException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
class HttpClientTest {
 @Test void encodingCacheCredentialsFailuresAndBodyUseRealHttp() throws Exception {
  var server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);var calls=new AtomicInteger();
  server.createContext("/",exchange->{
   calls.incrementAndGet();String result=exchange.getRequestURI().getRawQuery()+"|"+exchange.getRequestHeaders().getFirst("Authorization")+"|"+new String(exchange.getRequestBody().readAllBytes(),StandardCharsets.UTF_8);
   byte[] bytes=result.getBytes(StandardCharsets.UTF_8);exchange.sendResponseHeaders(exchange.getRequestURI().getPath().equals("/fail")?503:200,bytes.length);try(var out=exchange.getResponseBody()){out.write(bytes);}
  });server.start();String url="http://127.0.0.1:"+server.getAddress().getPort();
  try {
   HttpClient.useCache(true);
   var first=HttpClient.get(url+"/?existing=1",Map.of("q","中文 +&"),String.class);
   assertTrue(first.contains("existing=1&q=%E4%B8%AD%E6%96%87+%2B%26"));
   assertEquals(first,HttpClient.get(url+"/?existing=1",Map.of("q","中文 +&"),String.class));assertEquals(1,calls.get());
   for(String token:List.of("a","b")) assertTrue(HttpClient.execute(url+"/",HttpMethod.GET,null,null,null,Map.of("Authorization",token),null,String.class,null).contains("|"+token+"|"));
   assertEquals(3,calls.get());
   assertTrue(HttpClient.post(url+"/",Map.of("name","中文"),null,String.class).contains("中文"));
   assertThrows(AppException.class,()->HttpClient.get(url+"/fail",null,String.class));
   assertThrows(IllegalArgumentException.class,()->HttpClient.get(url+"/",null,Integer.class));
  } finally {HttpClient.useCache(false);server.stop(0);}
 }
}
