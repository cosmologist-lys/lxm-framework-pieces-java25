/**
 * @Author: Lys
 * @Date 2023/5/12
 * @Describe
 *
 *
 *             模拟实现rabbit-mq那样的消费者-生产者模式
 *             但没有真正的可靠队列
 *             使用需谨慎
 *
 *             而且，publish出去的消息如果一直没有被消费，会被堆积
 **/
package com.lxm.framework.redis.impl.bus;
