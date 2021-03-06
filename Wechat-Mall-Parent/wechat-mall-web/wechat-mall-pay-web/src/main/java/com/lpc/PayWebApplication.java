package com.lpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Lin
 * @Date 2020/2/10
 */
@SpringBootApplication
@EnableHystrix
@EnableFeignClients
@EnableAsync
@EnableScheduling
public class PayWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayWebApplication.class,args);
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadTaskPool = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        threadTaskPool.setCorePoolSize(10);
        // 设置最大线程数
        threadTaskPool.setMaxPoolSize(10);
        // 设置队列容量
        threadTaskPool.setQueueCapacity(10);
        // 设置线程活跃时间300秒
        threadTaskPool.setKeepAliveSeconds(300);
        // 设置默认线程名称
        threadTaskPool.setThreadNamePrefix("multiThread-");
        // 设置拒绝策略rejection-policy：当pool已经达到max size的时候，如何处理新任务 CALLER_RUNS：不在新线程中执行任务，而是由调用者所在的线程来执行
        threadTaskPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 等待所有任务结束后再关闭线程池
        threadTaskPool.setWaitForTasksToCompleteOnShutdown(true);
        return threadTaskPool;
    }
}
