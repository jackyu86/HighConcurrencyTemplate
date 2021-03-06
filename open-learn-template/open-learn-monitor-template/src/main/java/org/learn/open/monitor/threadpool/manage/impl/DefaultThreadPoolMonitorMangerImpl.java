package org.learn.open.monitor.threadpool.manage.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.learn.open.monitor.threadpool.ThreadPoolMonitorSet;
import org.learn.open.monitor.threadpool.manage.ThreadPoolMonitorManager;
import org.learn.open.monitor.threadpool.model.ThreadPoolInformation;
import org.learn.open.monitor.threadpool.proxy.MonitorThreadPoolProxy;
import org.learn.open.utils.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: jack-yu
 * @Description:  默认线程池管理器
 */
public class DefaultThreadPoolMonitorMangerImpl implements ThreadPoolMonitorManager{
    private static final Logger LOGGER= LoggerFactory.getLogger(DefaultThreadPoolMonitorMangerImpl.class);

    @Override
    public ThreadPoolInformation getTPInformationByName(String name) {

        Object threadPoolObject= ThreadPoolMonitorSet.INSTANCE.getThreadPoolObject(name);
        if(null==threadPoolObject){
            LOGGER.warn("Not find ThreadPool by Name [{}]", name);
            return null;
        }

        return buildThreadPoolInformation(name,threadPoolObject);
    }

    @Override
    public List<ThreadPoolInformation> getAllTPInformation() {
        Map<String,Object> threadPoolMap=ThreadPoolMonitorSet.INSTANCE.getAllThreadPools();
        List<ThreadPoolInformation> threadPoolInformationList=new ArrayList<ThreadPoolInformation>();

        for(Map.Entry<String,Object> entry:threadPoolMap.entrySet()){

            ThreadPoolInformation threadPoolInformation=buildThreadPoolInformation(entry.getKey(),entry.getValue());
            threadPoolInformationList.add(threadPoolInformation);
        }
        return threadPoolInformationList;
    }

    @Override
    public ThreadPoolInformation registerThreadPoolMonitor(String threadPoolName, String beanId) {

        Object threadPoolObject=ThreadPoolMonitorSet.INSTANCE.getThreadPoolObject(threadPoolName);
        if(null!=threadPoolObject){
            //已经注册
            LOGGER.error("Register ThreadPool Error. threadPoolName[{}] has existed.",threadPoolName);
            return null;
        }

        threadPoolObject= SpringContextUtil.getBeanById(beanId);
        if(null==threadPoolObject){
            //未获得bean
            LOGGER.error("Register ThreadPool Error. threadPool bean not find by id[{}]",beanId);
            return null;
        }

        ThreadPoolMonitorSet.INSTANCE.register(threadPoolName,threadPoolObject);

        return getTPInformationByName(threadPoolName);
    }

    @Override
    public Boolean removeAllTask(String name) {
        MonitorThreadPoolProxy ntp = getMonitorThreadPoolProxy(name);
        if(ntp!=null) {
            ntp.removeTask();
            return true;
        }else{
            return false;
        }
    }

    private ThreadPoolInformation buildThreadPoolInformation(String name,Object threadPoolObject){
        MonitorThreadPoolProxy threadPoolProxy=new MonitorThreadPoolProxy(threadPoolObject);
        ThreadPoolInformation threadPoolInformation=threadPoolProxy.buildThreadPoolInformation();
        threadPoolInformation.setName(name);

        return threadPoolInformation;
    }

    private MonitorThreadPoolProxy getMonitorThreadPoolProxy(String name){
        Object threadPoolObject= ThreadPoolMonitorSet.INSTANCE.getThreadPoolObject(name);
        if(null==threadPoolObject){
            LOGGER.warn("Not find ThreadPool by Name [{}]", name);
            return null;
        }
        return new MonitorThreadPoolProxy(threadPoolObject);
    }
}
