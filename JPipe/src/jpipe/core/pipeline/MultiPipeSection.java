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
package jpipe.core.pipeline;

import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import jpipe.abstractclass.PipeSection;
import jpipe.dynamic.Analysis.SectionAnalyser;
import jpipe.dynamic.Analysis.SectionAnalysisResult;
import jpipe.dynamic.Analysis.MultiPipeSectionAnalysisResult;
import jpipe.abstractclass.worker.Worker;
import jpipe.abstractclass.worker.WorkerFactory;
import jpipe.buffer.util.BufferStore;
import jpipe.interfaceclass.IPipeSectionLazy;


/**
 *
 * @author Yibo
 * @param <E>
 */
public class MultiPipeSection<E extends PipeSection> implements IPipeSectionLazy {

    private final HashMap<Integer, SectionAnalyser> analysers;
    private final HashMap<Integer, SinglePipeSection> pipesections;
    private final DefaultWorkerFactory<Worker> workerFactory;
    private boolean isStarted = false;
//each parallel has a threadpool
    ThreadPoolExecutor threadPoolExecutor;

    private int corePoolSize = 500;
    private int maxPoolSize = 500;
    private long keepAliveTime = 5000;
    private final int threadNumber_initial;
    private int threadNumber_total = 0;

    BufferStore bs;

    public BufferStore getBs() {
        return bs;
    }

    public void setBs(BufferStore bs) {
        this.bs = bs;
    }

    public synchronized int getThreadNumber_resting() {
        int result = 0;
        for (int i = 1; i <= threadNumber_total; i++) {
            if (pipesections.get(i).isLazyResting()) {
                result++;
            }
        }
        return result;
    }

    public synchronized int getThreadNumber_pausing() {
        int result = 0;
        for (int i = 1; i <= threadNumber_total; i++) {
            if (pipesections.get(i).isPausing()) {
                result++;
            }
        }
        return result;
    }

    public synchronized int getThreadNumber_running() {
        int result = 0;
        for (int i = 1; i <= threadNumber_total; i++) {
            if (pipesections.get(i).isRunning()) {
                result++;
            }
        }
        return result;
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

    public MultiPipeSection(WorkerFactory wfactory, BufferStore bs) {
        this.pipesections = new HashMap<>();
        this.analysers = new HashMap<>();

        this.workerFactory = (DefaultWorkerFactory<Worker>) wfactory;
        this.threadNumber_initial = 5;
    }

    public MultiPipeSection(WorkerFactory wfactory, BufferStore bs, int thread_number) {
        this.pipesections = new HashMap<>();
        this.analysers = new HashMap<>();
        this.bs = bs;
        this.workerFactory = (DefaultWorkerFactory<Worker>) wfactory;
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

                AddPipeSection(i);

            }
            isStarted = true;
        }
    }

    private synchronized void AddPipeSection(int index) {
        // System.out.println("adding pip i=" + index);
        //following code creates a worker and an analyser class for each
        //pipeline block class.
        // Analysers and threads of Blocks are saved into a hashmap for future reference.
        //pipeline block is then executed in the threadpool
        SectionAnalyser a = new SectionAnalyser();
        analysers.put(index, a);

        Worker newWorker = (Worker) this.workerFactory.create();
        newWorker.setBufferStore(bs);
        SinglePipeSection pb = new SinglePipeSection(newWorker);

        pb.setAnalyser(a);
        //newWorker.setWrapPipeSection(pb);
        pb.setLaziness(3000);
        pipesections.put(index, pb);

        threadPoolExecutor.execute(pb);

        threadNumber_total++;
        //System.out.println("Added into pipsecs total=" + threadNumber_total);

    }

    public synchronized void DecreasePipesBy(int n) {
        int running = getThreadNumber_running();
        //if n is larger or equal to running thread, pause all.
        for (int i = running; i > running - n && i >= 1; i--) {
            System.out.println("pausing " + (i) + " ");
            pipesections.get(i).pause();

        }
    }

    public synchronized void IncreasePipesBy(int n) {
        for (int i = 1; i <= n; i++) {
            if (getThreadNumber_pausing() > 0) {
                System.out.println("notifying " + (getThreadNumber_running() + 1) + " ");

                pipesections.get(getThreadNumber_running() + 1).resume();
                // pipesections.get(threadNumber_running + 1).notifyAll();
                // notifyAll();

            } else {
                AddPipeSection(i + threadNumber_total);
            }
        }
    }

    public synchronized MultiPipeSectionAnalysisResult GetSectionAnalyseResult() {
        //calculate everything from analysers
        //reset all analysers
        MultiPipeSectionAnalysisResult result = new MultiPipeSectionAnalysisResult();
        long maxLatency = Long.MIN_VALUE;
        long minLatency = Long.MAX_VALUE;
        double meanLatency = 0;
        double throughput = 0;
        int workdone = 0;
        int workfail = 0;
        long SectionRunningTime = 0;

        long maxContiSucc = 0;
        double LifetimeAvrgConsecutiveSucc = 0;
        double currentAvrgConsecutiveSucc = 0;

        for (int i = 1; i <= threadNumber_total; i = i + 1) {
            SectionAnalysisResult temp = analysers.get(i).analyseMs();
            //System.out.println(temp);
            if (temp.getBlockRunningTime() > SectionRunningTime) {
                SectionRunningTime = temp.getBlockRunningTime();
            }
            if (temp.getMaximumLatency() > maxLatency) {
                maxLatency = temp.getMaximumLatency();
            }
            if (temp.getMinimumLatency() < minLatency) {
                minLatency = temp.getMinimumLatency();
            }

            if (temp.getMaxConsecutiveSuccCount() > maxContiSucc) {
                maxContiSucc = temp.getMaxConsecutiveSuccCount();
            }

            workdone += temp.getWorkdoneAmount();
            workfail += temp.getWorkfailAmount();
            SinglePipeSection pi = pipesections.get(i);
            if (pi.isRunning()) {
                //running++;
                LifetimeAvrgConsecutiveSucc += ((double) temp.getLifetimeAverageConsecutiveSuccCount() - (double) LifetimeAvrgConsecutiveSucc) / (double) i;
                meanLatency += ((double) temp.getAverageLatency() - (double) meanLatency) / (double) i;
                currentAvrgConsecutiveSucc += ((double) temp.getCurrentConsecutiveSeccCount() - currentAvrgConsecutiveSucc) / (double) i;
                throughput += temp.getBlockThroughput();
            }
            
            result.addPipeState(temp.getPipeSectionState());
            result.addWorkerState(temp.getWorkerState());

        }
       
        //avrgContiSucc = avrgContiSucc / threadNumber_total;
        result.setAverageLatency(meanLatency);

        result.setWorkdoneAmount(workdone);
        result.setWorkfailAmount(workfail);

        result.setSectionThroughput(throughput);
        result.setSectionRunningTime(SectionRunningTime);

        result.setMaximumLatency(maxLatency);
        result.setMinimumLatency(minLatency);
        
        result.setCurrentConsecutiveSeccCount(currentAvrgConsecutiveSucc);
        result.setLifetimeAverageContinuousSuccess(LifetimeAvrgConsecutiveSucc);
        result.setMaxCountinuousSuccess(maxContiSucc);

        return result;
    }

    /**
     * Pause all working pipe sections
     */
    @Override
    public synchronized void pause() {
        this.DecreasePipesBy(this.threadNumber_total - this.getThreadNumber_pausing());
    }

    /**
     * Resume all pausing pipe sections
     */
    @Override
    public synchronized void resume() {
        this.IncreasePipesBy(this.getThreadNumber_pausing());
    }

    @Override
    public void getNotifiedByOther() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
