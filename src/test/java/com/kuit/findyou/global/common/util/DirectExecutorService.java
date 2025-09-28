package com.kuit.findyou.global.common.util;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * 테스트 전용 ExecutorService
 * 모든 작업을 현재 스레드에서 즉시 실행
 */
public class DirectExecutorService extends AbstractExecutorService {

    private volatile boolean shutdown = false;

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) {
        return true; // 이미 동기 실행이라 기다릴 게 없음
    }

    @Override
    public void execute(Runnable command) {
        command.run(); // 현재 스레드에서 즉시 실행
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public Future<?> submit(Runnable task) {
        try {
            task.run();
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            CompletableFuture<Void> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        try {
            task.run();
            return CompletableFuture.completedFuture(result);
        } catch (Exception e) {
            CompletableFuture<T> failed = new CompletableFuture<>();
            failed.completeExceptionally(e);
            return failed;
        }
    }
}