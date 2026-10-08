package com.lxm.framework.enigma;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.*;

@Getter @Setter
@ConfigurationProperties("lfp.enigma")
public class EnigmaProperties {
    private boolean enabled;
    private String store;
    private long keyTtlMillis=900_000;
    private long refreshAfterMillis=720_000;
    private long overlapMillis=120_000;
    private long clockSkewMillis=60_000;
    private long nonceTtlMillis=180_000;
    private long responseGraceMillis=300_000;
    private int maxBodyBytes=1_048_576;
    private int maxEnvelopeBytes=1_572_864;
    private int maxSessions=1000;
    private int maxNoncesPerKey=4096;
    private int maxUsesPerKey=4096;
    private int sessionsPerMinute=5;
    private boolean allowInsecureLocalhost;
    private List<String> allowedOrigins=new ArrayList<>();
    private String publicPathPrefix="";
    private String redisUri="redis://127.0.0.1:6379/0";
    private String redisNamespace="lfp:enigma:v1:";
    private String activeWrappingKid;
    private Map<String,String> wrappingKeys=new HashMap<>();
    public void validate() {
        if (keyTtlMillis<=0 || keyTtlMillis>3_600_000 || refreshAfterMillis<=0 || refreshAfterMillis>=keyTtlMillis ||
            overlapMillis<0 || overlapMillis>120_000 || clockSkewMillis<=0 || clockSkewMillis>300_000 ||
            nonceTtlMillis<2*clockSkewMillis+30_000 || responseGraceMillis<overlapMillis ||
            maxBodyBytes<=0 || maxBodyBytes>10_485_760 || maxEnvelopeBytes<((maxBodyBytes+16L)*4+2)/3+2048 || maxEnvelopeBytes>16_777_216 ||
            maxSessions<=0 || maxNoncesPerKey<=0 || maxUsesPerKey<=0 || maxUsesPerKey>(1<<20) || sessionsPerMinute<=0) {
            throw new IllegalArgumentException("Invalid Enigma lifecycle or capacity configuration");
        }
        if (!publicPathPrefix.isEmpty() && (!publicPathPrefix.startsWith("/") || publicPathPrefix.endsWith("/"))) throw new IllegalArgumentException("Invalid public path prefix");
    }
}
