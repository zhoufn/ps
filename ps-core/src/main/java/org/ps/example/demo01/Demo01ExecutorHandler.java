package org.ps.example.demo01;

import com.dangdang.ddframe.job.api.ShardingContext;
import org.ps.platform.core.Task;
import org.ps.platform.core.annotation.IExecutor;
import org.ps.platform.core.handler.ExecutorHandler;
import org.springframework.stereotype.Component;

@Component
@IExecutor(name = "demoExecutor")
public class Demo01ExecutorHandler extends ExecutorHandler {

    @Override
    public void execute(ShardingContext shardingContext, Task runnigTask) {
        System.out.println("*******************************");
    }
}
