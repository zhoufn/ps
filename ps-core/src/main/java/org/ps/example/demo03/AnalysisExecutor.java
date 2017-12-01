package org.ps.example.demo03;

import com.alibaba.fastjson.JSON;
import com.dangdang.ddframe.job.api.ShardingContext;
import org.ps.example.demo02.UnPackageExecutor;
import org.ps.example.demo03.domain.AnalysisShardTask;
import org.ps.example.demo03.domain.AnalysisShardTaskRepository;
import org.ps.example.demo03.domain.Param;
import org.ps.example.demo03.handler.AnalysisHandler;
import org.ps.platform.core.ShardTask;
import org.ps.platform.core.Task;
import org.ps.platform.core.annotation.IExecutor;
import org.ps.platform.core.handler.ExecutorHandler;
import org.ps.platform.core.repository.ShardTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;

@Component
@IExecutor(name = "analysisExecutor")
@Scope(value = "prototype")
public class AnalysisExecutor extends ExecutorHandler{

    @Autowired
    private AnalysisShardTaskRepository repository;

    @Autowired
    private AnalysisHandler analysisHandler;

    /**
     * @param shardingContext 分片上下文
     * @param runningTask     运行任务
     * @param shardTask       分片任务
     */
    @Override
    public void execute(ShardingContext shardingContext, Task runningTask, ShardTask shardTask) {
        AnalysisShardTask analysisShardTask = (AnalysisShardTask) shardTask;
        Param param = JSON.parseObject(runningTask.getParamString(),Param.class);
        String srcFile = analysisShardTask.getParamString();
        String xpathFile = param.getRolePath();
        this.analysisHandler.analysis(xpathFile,srcFile);

    }

    /**
     * 获取JPA接口
     *
     * @return
     */
    @Override
    protected ShardTaskRepository getShardTaskRepository() {
        return this.repository;
    }
}
