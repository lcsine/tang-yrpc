package com.tang.discovery.impl;

import com.tang.Constant;
import com.tang.ServiceConfig;
import com.tang.discovery.AbstractRegistry;
import com.tang.utils.NetUtils;
import com.tang.utils.zookeeper.ZookeeperNode;
import com.tang.utils.zookeeper.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    //维护了一个zk实例
    private ZooKeeper zooKeeper;
    public ZookeeperRegistry() {
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String connectString,int timeOut) {
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeOut);
    }


    @Override
    public void register(ServiceConfig<?> service,int port) {
        //服务名称的节点
        String parentNode = Constant.BASE_PROVIDERS_PATH+"/"+service.getInterface().getName();
        //这个节点应该是一个持久节点
        if (ZookeeperUtils.exists(zooKeeper,parentNode,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(parentNode,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode, null, CreateMode.PERSISTENT);
        }
        //创建本机的临时节点，ip:port,
        //服务提供方的端口一般自己设定，我们还需要一个获取ip的方法
        //ip我们通常是需要一个局域网ip，不是127.0.0.1，也不是ipv6
        //192.168.3.7
        //todo:后续处理端口问题
        String node = parentNode + "/" + NetUtils.getIp()+":"+port;
        if (ZookeeperUtils.exists(zooKeeper,node,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(node,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode, null, CreateMode.EPHEMERAL);
        }
        if (log.isDebugEnabled()) {
            log.debug("服务{}，已经被注册", service.getInterface().getName());
        }
    }
}
