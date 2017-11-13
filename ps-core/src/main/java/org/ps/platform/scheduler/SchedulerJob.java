package org.ps.platform.scheduler;

import com.dangdang.ddframe.job.api.ShardingContext;
import com.dangdang.ddframe.job.api.simple.SimpleJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 平台下用来执行分片策略的调度器
 */
public class SchedulerJob implements SimpleJob{

    private Logger logger = LoggerFactory.getLogger(SchedulerJob.class);
    /**
     * 执行作业.
     *
     * @param shardingContext 分片上下文
     */
    @Override
    public void execute(ShardingContext shardingContext) {
        logger.debug("SchedulerJob is running.");
    }
}
