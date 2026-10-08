package com.lxm.framework.enigma.store;

import com.lxm.framework.enigma.*;
import com.lxm.framework.enigma.protocol.ProtocolJson;
import java.time.Clock;
import java.util.*;
import java.util.function.Function;

/** 有限容量的单实例开发实现；有效的重放记录不会被容量淘汰。 */
public final class MemorySessionStore implements SessionStore {
    private final Map<String,SessionState> sessions=new HashMap<>();
    private final Map<String,Integer> rate=new HashMap<>();
    private final Map<String,Long> revokedOwners=new HashMap<>();
    private long minute=-1;
    private final EnigmaProperties properties;
    private final Clock clock;
    public MemorySessionStore(EnigmaProperties properties,Clock clock) { this.properties=properties;this.clock=clock; }
    private SessionState copy(SessionState value) { return ProtocolJson.MAPPER.readValue(ProtocolJson.MAPPER.writeValueAsBytes(value),SessionState.class); }
    public synchronized void create(SessionState value) {
        long now=clock.millis();revokedOwners.entrySet().removeIf(entry->entry.getValue()<=now);sessions.values().removeIf(state -> state.retainUntil<=now);
        if (sessions.size()>=properties.getMaxSessions()) throw EnigmaException.capacity();
        if (minute!=now/60_000) { minute=now/60_000;rate.clear(); }
        String owner=EnigmaCryptoOwner.binding(value.owner);
        if (revokedOwners.containsKey(owner)) throw EnigmaException.identity();
        if (rate.getOrDefault(owner,0)>=properties.getSessionsPerMinute()) throw EnigmaException.capacity();
        if (rate.size()>=properties.getMaxSessions() && !rate.containsKey(owner)) throw EnigmaException.capacity();
        if (sessions.containsKey(value.sid)) throw EnigmaException.capacity();
        rate.merge(owner,1,Integer::sum);sessions.put(value.sid,copy(value));
    }
    public synchronized <T> T transact(String sid,boolean allowRevokedOwner,Function<SessionState,T> action) {
        var state=sessions.get(sid);
        if (state==null || state.retainUntil<=clock.millis()) { sessions.remove(sid);throw EnigmaException.key(); }
        if (!allowRevokedOwner && revokedOwners.getOrDefault(EnigmaCryptoOwner.binding(state.owner),0L)>clock.millis()) throw EnigmaException.identity();
        var candidate=copy(state);T result=action.apply(candidate);sessions.put(sid,candidate);return result;
    }
    public synchronized void revokeOwner(Map<String,String> owner,long retainUntil) {
        revokedOwners.entrySet().removeIf(entry->entry.getValue()<=clock.millis());
        String key=EnigmaCryptoOwner.binding(owner);
        if (revokedOwners.size()>=properties.getMaxSessions() && !revokedOwners.containsKey(key)) throw EnigmaException.storage();
        revokedOwners.put(key,retainUntil);
    }
}
