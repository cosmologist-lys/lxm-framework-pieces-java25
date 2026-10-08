package com.lxm.framework.redis.impl.bus;

import com.lxm.framework.redis.inf.BusSubscriberSession;
import lombok.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author: Lys
 * @Date 2023/5/16
 * @Describe
 **/
public class BusMultiSub implements BusSubscriberSession.BusSubInf {

    private final List<BusSub> subscribers;

    public BusMultiSub(@NonNull List<BusSub> subscribers) {
        this.subscribers = subscribers;
    }

    @Override
    public void subscribe() {
        subscribers.forEach(BusSub::subscribe);
    }
}
