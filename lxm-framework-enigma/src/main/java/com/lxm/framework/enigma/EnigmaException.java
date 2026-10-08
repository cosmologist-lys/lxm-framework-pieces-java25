package com.lxm.framework.enigma;

/** 可显示的协议错误；不包含密码异常、密钥或请求内容。 */
public final class EnigmaException extends RuntimeException {
    private final int code;
    private final int status;
    public EnigmaException(int code, int status, String message) { super(message); this.code=code; this.status=status; }
    public int code() { return code; }
    public int status() { return status; }
    public static EnigmaException protocol() { return new EnigmaException(6200,400,"加密协议不正确"); }
    public static EnigmaException key() { return new EnigmaException(6201,401,"请重新建立加密会话"); }
    public static EnigmaException expired() { return new EnigmaException(6202,401,"加密密钥已过期"); }
    public static EnigmaException integrity() { return new EnigmaException(6203,400,"消息认证失败"); }
    public static EnigmaException replay() { return new EnigmaException(6204,409,"请求已处理，请勿重复发送"); }
    public static EnigmaException capacity() { return new EnigmaException(6205,429,"加密会话已达到容量限制"); }
    public static EnigmaException identity() { return new EnigmaException(6206,403,"加密会话与当前登录身份不符"); }
    public static EnigmaException storage() { return new EnigmaException(6207,503,"加密会话服务暂时不可用"); }
}
