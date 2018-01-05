package org.ps.domain;


import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 平台核心类
 */
@Data
@RequiredArgsConstructor
public class Task implements Serializable, Comparable<Task> {

    /**
     * id,uuid
     */
    @NotNull
    private String id;
    /**
     * 创建时间
     */
    @NotNull
    private long createTime;

    /**
     * 调度类
     */
    @NotNull
    private String scheduler;

    /**
     * 监控类
     */
    @NotNull
    private String monitor;

    /**
     * 执行类
     */
    @NotNull
    private String executor;

    /**
     * 附加参数
     */
    private String paramString;
    /**
     * 优先级
     */
    @NotNull
    private int sort;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 任务是否存在异常
     */
    private boolean error = false;

    /**
     * 异常信息
     */
    private String errorMsg;

    /**
     * 进度信息
     */
    private float process;

    /**
     * 是否暂停
     */
    private boolean paused = false;

    /**
     * 字符串类型创建日期，用于页面展示
     */
    private String createTimeStr;


    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(Task o) {
        return (int) ((this.sort - o.getSort()) == 0 ? (this.createTime - o.getCreateTime()) : this.sort - o.getSort());
    }
}
