package com.lxm.framework.enigma.store;

import java.util.*;

/** 仅供存储事务内部使用；Redis 保存的是整条记录的认证密文。 */
public final class SessionState {
    public String sid;
    public Map<String,String> owner;
    public String currentKid;
    public boolean revoked;
    public long retainUntil;
    public Map<String,KeyVersion> keys=new HashMap<>();
    public static final class KeyVersion {
        public String kid;
        public String requestEncryption;
        public String responseEncryption;
        public String requestSigning;
        public String responseSigning;
        public long createdAt;
        public long expiresAt;
        public long acceptUntil;
        public String refreshedTo;
        public long retryUntil;
        public int requestUses;
        public int responseUses;
        public Map<String,Long> nonces=new HashMap<>();
        public Set<String> requestIvs=new HashSet<>();
        public Set<String> responseIvs=new HashSet<>();
    }
}
