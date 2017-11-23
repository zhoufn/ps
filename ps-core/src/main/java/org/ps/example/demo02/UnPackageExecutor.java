package org.ps.example.demo02;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.ps.example.demo02.domain.Param;
import org.ps.example.demo02.handler.UnCompressHandler;
import org.ps.platform.core.ShardTask;
import org.ps.platform.core.Task;
import org.ps.platform.core.annotation.IExecutor;
import org.ps.platform.core.handler.ExecutorHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

/**
 * 解压执行器
 */
@Component
@IExecutor(name = "unPackageExecutor")
public class UnPackageExecutor extends ExecutorHandler {

    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    private Connection connection;

    @Autowired
    private UnCompressHandler unCompressHandler;

    private void initConnection() throws Exception {
        if (this.connection == null) {
            this.connection = this.dataSource.getConnection();
        }
    }

    @Override
    public void execute(ShardingContext shardingContext, Task runnigTask){
        ShardTask shardTask = this.getOneWaitingShardTask(runnigTask, shardingContext.getShardingItem());
        if(shardTask == null){
            return;
        }
        System.out.println("*************扫描到ShardTask：" + JSON.toJSONString(shardTask) + "*******************");
        //设置任务开始时间
        this.updateShardTaskTime(shardTask,1);
        //待解压文件
        String srcFilePath = shardTask.getParamString();
        Param param = JSON.parseObject(runnigTask.getParamString(), Param.class);
        //解压到的路径
        String destDir = param.getDestFilePath();
        try {
            this.unCompressHandler.unCompress(srcFilePath,destDir);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            //设置任务完成时间
            this.updateShardTaskTime(shardTask,2);
        }
    }


    /**
     * 更新ShardTask时间
     * @param shardTask
     * @param type  1:更新启动时间，2：更新完成时间
     */
    private void updateShardTaskTime(ShardTask shardTask,int type){
        try{
            this.initConnection();
            String sql = null;
            if(type == 1){
                sql = "update job_shard_task t set t.beginTime=now() where t.id=?";
            }else if(type == 2){
                sql = "update job_shard_task t set t.endTime=now() where t.id=?";
            }
            PreparedStatement pstmt = this.connection.prepareStatement(sql);
            pstmt.setString(1,shardTask.getId());
            pstmt.execute();
            pstmt.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取一个待执行的ShardTask
     *
     * @param runningTask
     * @param shardNumber
     * @return
     */
    private ShardTask getOneWaitingShardTask(Task runningTask, int shardNumber) {
        ShardTask shardTask = null;
        try {
            this.initConnection();
            PreparedStatement pstmt = connection.prepareStatement("select id,paramString,createTime from job_shard_task t where t.endTime is null and t.parentId=? and t.shardNumber=? order by t.createTime asc limit 0,1");
            pstmt.setString(1, runningTask.getId());
            pstmt.setInt(2, shardNumber);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                shardTask = new ShardTask();
                shardTask.setId(rs.getString("id"));
                shardTask.setParentId(runningTask.getId());
                shardTask.setShardNumber(shardNumber);
                shardTask.setParamString(rs.getString("paramString"));
                shardTask.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
                break;
            }
            rs.close();
            pstmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shardTask;
    }
}
