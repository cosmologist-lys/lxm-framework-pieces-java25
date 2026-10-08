package com.lxm.framework.enigma.store;

import java.util.function.Function;

/** action 可能被乐观事务重试；只能修改传入的副本，不能执行外部业务或不可重试副作用。 */
public interface SessionStore {
    void create(SessionState state);
    default <T> T transact(String sid,Function<SessionState,T> action) { return transact(sid,false,action); }
    <T> T transact(String sid,boolean allowRevokedOwner,Function<SessionState,T> action);
    void revokeOwner(java.util.Map<String,String> owner,long retainUntil);
}
