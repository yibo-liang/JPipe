/*
 * The MIT License
 *
 * Copyright 2015 yl9.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jpipe.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.dynamic.Analysis.SectionAnalyser;
import jpipe.dynamic.Analysis.SectionAnalysisResult;
import jpipe.dynamic.Analysis.ConcurrentSectionAnalysisResult;
import jpipe.abstractclass.TPBuffer;
import jpipe.abstractclass.DefaultWorker;
import jpipe.abstractclass.WorkerFactory;
import jpipe.interfaceclass.IWorker;

/**
 *
 * @author Yibo
 * @param <E>
 */
public class ConcurrentPipes {

    private final TPBuffer[] buffers;
    private final HashMap<Integer, SectionAnalyser> analysers;
    private final HashMap<Integer, PipeSection> pipesections;
    private final WorkerFactory workerFactory;
    private boolean isStarted = false;
//each parallel has a threadpool
    ThreadPoolExecutor threadPoolExecutor;

    private int corePoolSize = 500;
    private int maxPoolSize = 500;
    private long keepAliveTime = 5000;
    private final int threadNumber_initial;
    private int threadNumber_total = 0;

    public int getThreadNumber_pause() {
        return threadNumber_pause;
    }
    private int threadNumber_pause = 0;
    private int threadNumber_running = 0;

    public int getThreadNumber_actual() {
        return threadNumber_running;
    }

    public synchronized void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public synchronized void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public synchronized void setKeepAliveTime(long keepAliveTime) {
        this.keepAliveTime = keepAliveTime;
    }

    public ConcurrentPipes(WorkerFactory wfactory, TPBuffer[] bs) {
        this.pipesections = new HashMap<>();
        this.analysers = new HashMap<>();
        this.buffers = bs;
        this.workerFactory = wfactory;
        this.threadNumber_initial = 5;
    }

    public ConcurrentPipes(WorkerFactory wfactory, TPBuffer[] bs, int thread_number) {
        this.pipesections = new HashMap<>();
        this.analysers = new HashMap<>();
        this.buffers = bs;
        this.workerFactory = wfactory;
        this.threadNumber_initial = thread_number;
    }

    public synchronized void Start() {
        if (!isStarted) {
            threadNumber_total = 0;
            threadPoolExecutor = new ThreadPoolExecutor(
                    corePoolSize,
                    maxPoolSize,
                    keepAliveTime,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>()
            );
            for (Integer i = 1; i <= threadNumber_initial; i++) {

                AddSection(i);

            }
            isStarted = true;
        }
    }

    private synchronized void AddSection(int index) {
        System.out.println("adding pip i=" + index);
        //following code creates a worker and an analyser class for each
        //pipeline block class.
        // Analysers and threads of Blocks are saved into a hashmap for future reference.
        //pipeline block is then executed in the threadpool
        SectionAnalyser a = new SectionAnalyser();
        analysers.put(index, a);
        PipeSection pb;

        pb = new PipeSection((IWorker) this.workerFactory.create(), buffers);
        pipesections.put(index, pb);

        threadPoolExecutor.execute(pb);
        threadNumber_total++;
        threadNumber_running++;

    }

    public synchronized void DecreasePipesBy(int n) {
        int running = threadNumber_running;
        //if n is larger or equal to running thread, pause all.
        for (int i = running; i > running - n && i >= 1; i--) {
            System.out.println("pausing " + (i) + " ");
            pipesections.get(i).pause();

            threadNumber_pause++;
            threadNumber_running--;
        }
    }

    public synchronized void IncreasePipesBy(int n) {
        for (int i = 1; i <= n; i++) {
            if (threadNumber_running < threadNumber_total) {
                System.out.println("notifying " + (threadNumber_running + 1) + " ");

                pipesections.get(threadNumber_running + 1).resume();
                // pipesections.get(threadNumber_running + 1).notifyAll();
                // notifyAll();

                threadNumber_pause--;
                threadNumber_running++;
            } else {
                AddSection(i + threadNumber_total);
            }
        }
    }

    public synchronized ConcurrentSectionAnalysisResult GetSectionAnalyseResult() {
        //calculate everything from analysers
        //reset all analysers
        ConcurrentSectionAnalysisResult result = new ConcurrentSectionAnalysisResult();
        long maxLatency = Long.MIN_VALUE;
        long minLatency = Long.MAX_VALUE;
        double meanLatency = 0;
        double throughput = 0;
        int workdone = 0;

        for (int i = 1; i <= threadNumber_total; i = i + 1) {
            SectionAnalysisResult temp = analysers.get(i).analyseMs();
            if (temp.getMaximumLatency() > maxLatency) {
                maxLatency = temp.getMaximumLatency();
            }
            if (temp.getMinimumLatency() < minLatency) {
                minLatency = temp.getMinimumLatency();
            }

            workdone += temp.getWorkdoneAmount();

            PipeSection pi = pipesections.get(i);
            if (pi.isRunning()) {
                //running++;
                meanLatency = meanLatency + (temp.getAverageLatency() - meanLatency) / i;
                throughput += temp.getBlockThroughput();
            }

        }

        result.setRunningThreads(threadNumber_running);
        result.setPausedThreads(threadNumber_pause);
        result.setAverageLatency(meanLatency);
        result.setWorkdoneAmount(workdone);
        result.setSectionThroughput(throughput);

        return result;
    }

}
