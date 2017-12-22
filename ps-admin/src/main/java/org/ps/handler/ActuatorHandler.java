package org.ps.handler;

import com.alibaba.fastjson.JSON;
import org.ps.domain.Constant;
import org.ps.domain.ServerStatus;
import org.ps.domain.UrlData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 外部接口访问类
 */
@Component
public class ActuatorHandler {


    @Autowired
    public ZookeeperHandler zookeeperHandler;


    public ServerStatus getServerStatus() throws Exception{
        Map<String, String> serverMap = zookeeperHandler.getClient().getChildrenWithData("/" + Constant.NODE_ENVIRONMENT);
        List<String> scheduleServers = null, executorServers = null, monitorServers = null;
        try{
            scheduleServers = zookeeperHandler.getClient().getChildren("/" + Constant.NODE_SCHEDULER + "/" + Constant.NODE_INSTANCES);
        }catch(Exception e){
            scheduleServers = new ArrayList<>();
        }
        try{
            executorServers = zookeeperHandler.getClient().getChildren("/" + Constant.NODE_EXECUTOR + "/" + Constant.NODE_INSTANCES);
        }catch (Exception e){
            executorServers = new ArrayList<>();
        }
        try{
            monitorServers = zookeeperHandler.getClient().getChildren("/" + Constant.NODE_MONITOR + "/" + Constant.NODE_INSTANCES);
        }catch(Exception e){
            monitorServers = new ArrayList<>();
        }
        ServerStatus serverStatus = new ServerStatus();
        double totalScheduleOL = 0, totalExecutorOL = 0, totalMonitorOL = 0;
        Map<String, UrlData> urlDataMap = new HashMap<>();
        for(String url : serverMap.keySet()){
            String dataStr = serverMap.get(url);
            String ip = url.split(Constant.PATH_SEPARATOR)[0];
            UrlData urlData = new UrlData();
            String realUrl = "http://" + url.replace(Constant.PATH_SEPARATOR, ":") + "/" + Constant.URL_PATH_METRICS;
            urlData.setUrl(realUrl);
            urlData.setData(dataStr);
            urlDataMap.put(ip, urlData);
            String serverRole = JSON.parseObject(dataStr).getObject(Constant.SERVER_ROLE_KEY, String.class);
            if(serverRole.equals(Constant.NODE_SCHEDULER)){
                serverStatus.setScheduleTotalCount(serverStatus.getScheduleTotalCount() + 1);
            }else if(serverRole.equals(Constant.NODE_EXECUTOR)){
                serverStatus.setExecutorTotalCount(serverStatus.getExecutorTotalCount() + 1);
            }else if(serverRole.equals(Constant.NODE_MONITOR)){
                serverStatus.setMonitorTotalCount(serverStatus.getMonitorTotalCount() + 1);
            }
        }
        for(String scheduleServer : scheduleServers){
            String ip = scheduleServer.split(Constant.PATH_SEPARATOR)[0];
            serverStatus.setScheduleLiveCount(serverStatus.getScheduleLiveCount() + 1);
            String realUrl = urlDataMap.get(ip).getUrl();
            totalScheduleOL += calculateOverLoad(connectURL(realUrl));
        }
        for(String executorServer : executorServers){
            String ip = executorServer.split(Constant.PATH_SEPARATOR)[0];
            serverStatus.setExecutorLiveCount(serverStatus.getExecutorLiveCount() + 1);
            String realUrl = urlDataMap.get(ip).getUrl();
            totalExecutorOL += calculateOverLoad(connectURL(realUrl));
        }
        for(String monitorServer : monitorServers){
            String ip = monitorServer.split(Constant.PATH_SEPARATOR)[0];
            serverStatus.setMonitorLiveCount(serverStatus.getMonitorLiveCount() + 1);
            String realUrl = urlDataMap.get(ip).getUrl();
            totalMonitorOL += calculateOverLoad(connectURL(realUrl));
        }
        if(serverStatus.getScheduleLiveCount() != 0){
            String n = DoubleToStringWithFormat(totalScheduleOL / serverStatus.getScheduleLiveCount() * 100);
            serverStatus.setScheduleOL(n);
        }else{
            serverStatus.setScheduleOL("0");
        }
        if(serverStatus.getExecutorLiveCount() != 0){
            String n = DoubleToStringWithFormat(totalExecutorOL / serverStatus.getExecutorLiveCount() * 100);
            serverStatus.setExecutorOL(n);
        }else{
            serverStatus.setExecutorOL("0");
        }
        if(serverStatus.getMonitorLiveCount() != 0){
            String n = DoubleToStringWithFormat(totalMonitorOL / serverStatus.getMonitorLiveCount() * 100);
            serverStatus.setMonitorOL(n);
        }else{
            serverStatus.setMonitorOL("0");
        }
        return serverStatus;
    }

    private double calculateOverLoad(String response){
        double memFree = getMetricFromResponse(response, "mem.free", Double.class);
        double memTotal = getMetricFromResponse(response, "mem", Double.class);
        if(memTotal == 0){
            return 100;
        }
        return (memTotal - memFree) / memTotal;
    }
    private <T> T getMetricFromResponse(String response, String key, Class<T> clazz){
        return JSON.parseObject(response).getObject(key, clazz);
    }
    private String connectURL(String source) throws Exception{
        URL url = new URL(source);
        URLConnection connection = url.openConnection();
        connection.connect();
        BufferedReader reader;
        String response = "";
        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while((line = reader.readLine()) != null){
            response += line;
        }
        return response;
    }
    private static String DoubleToStringWithFormat(Number n){
        return String.format("%.0f", n);
    }
    public static void main(String args[]) throws Exception{
        //String res = connectURL("http://www.dd.com");
        //System.out.println(res);
    }
}
