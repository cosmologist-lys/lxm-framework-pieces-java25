package com.lxm.framework.enigma.mvc;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.protocol.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** 缓存已由应用 Mapper 和 JsonResultAdvice 完成的 JSON，最后仅保护一次。 */
public final class EnigmaResponseFilter extends OncePerRequestFilter {
    private final EnigmaProtocol protocol;
    private final EnigmaProperties properties;
    public EnigmaResponseFilter(EnigmaProtocol protocol,EnigmaProperties properties) { this.protocol=protocol;this.properties=properties; }
    @Override
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
        var buffered=new BufferedResponse(request,response,properties.getMaxBodyBytes());
        try { chain.doFilter(request,buffered); }
        catch (Exception failure) {
            if (request.getAttribute(EnigmaInterceptor.POLICY)==null) {
                if (failure instanceof IOException io) throw io;
                if (failure instanceof ServletException servlet) throw servlet;
                throw new ServletException(failure);
            }
            buffered.resetBuffer();buffered.setStatus(500);
            buffered.buffer.writeBytes(error(6200,"响应无法按加密协议处理"));
        }
        if (request.getAttribute(EnigmaInterceptor.POLICY)==null) return;
        buffered.finish();
        var context=(ProtectedContext)request.getAttribute(EnigmaInterceptor.CONTEXT);
        byte[] bytes=buffered.buffer.toByteArray();
        if (context!=null && context.authenticated) {
            try {
                var protectedResponse=protocol.protect(context,response.getStatus(),bytes);
                bytes=protectedResponse.body();protectedResponse.headers().forEach(response::setHeader);
            } catch (EnigmaException failure) {
                response.setStatus(failure.status());
                var protectedFailure=protocol.protect(context,failure.status(),error(failure.code(),failure.getMessage()));
                bytes=protectedFailure.body();protectedFailure.headers().forEach(response::setHeader);
            }
        } else {
            // 尚未取得可信密码上下文的握手/认证错误只属于 HTTPS 错误，客户端不能当成已认证业务结果。
            response.setHeader("X-Enigma-Mode","NONE");response.setHeader("X-Enigma-Unexecuted","true");
        }
        response.setHeader("Cache-Control","no-store");response.setHeader("Content-Encoding",null);
        response.setContentType("application/json;charset=UTF-8");response.setContentLength(bytes.length);response.getOutputStream().write(bytes);
    }
    private static byte[] error(int code,String message) {
        var value=ProtocolJson.MAPPER.createObjectNode();value.put("code",code);value.put("message",message);value.putNull("data");
        return ProtocolJson.MAPPER.writeValueAsBytes(value);
    }
    private static final class BufferedResponse extends HttpServletResponseWrapper {
        private final HttpServletRequest request;
        private final int maximum;
        private final ByteArrayOutputStream buffer=new ByteArrayOutputStream();
        private PrintWriter writer;
        private ServletOutputStream stream;
        BufferedResponse(HttpServletRequest request,HttpServletResponse response,int maximum) { super(response);this.request=request;this.maximum=maximum; }
        private boolean protectedResponse() { return request.getAttribute(EnigmaInterceptor.POLICY)!=null; }
        @Override public ServletOutputStream getOutputStream() throws IOException {
            if (writer!=null) throw new IllegalStateException("Writer already obtained");return output();
        }
        private ServletOutputStream output() throws IOException {
            if (stream==null) {
                var direct=super.getOutputStream();
                stream=new ServletOutputStream() {
                    public boolean isReady() { return true; }
                    public void setWriteListener(WriteListener listener) { throw new IllegalStateException("Async protected output is unsupported"); }
                    public void write(int value) throws IOException {
                        if (!protectedResponse()) direct.write(value);
                        else { if (buffer.size()>=maximum) throw new IOException("Protected response exceeds configured limit");buffer.write(value); }
                    }
                    public void write(byte[] value,int offset,int length) throws IOException {
                        if (!protectedResponse()) direct.write(value,offset,length);
                        else { if (length>maximum-buffer.size()) throw new IOException("Protected response exceeds configured limit");buffer.write(value,offset,length); }
                    }
                    public void flush() throws IOException { if (!protectedResponse()) direct.flush(); }
                };
            }
            return stream;
        }
        @Override public PrintWriter getWriter() throws IOException {
            if (stream!=null && writer==null) throw new IllegalStateException("Output stream already obtained");
            if (writer==null) writer=new PrintWriter(new OutputStreamWriter(output(),StandardCharsets.UTF_8));return writer;
        }
        @Override public void flushBuffer() throws IOException { if (writer!=null) writer.flush();if (!protectedResponse()) super.flushBuffer(); }
        @Override public void resetBuffer() { if (protectedResponse()) buffer.reset();else super.resetBuffer(); }
        void finish() { if (writer!=null) writer.flush(); }
    }
}
