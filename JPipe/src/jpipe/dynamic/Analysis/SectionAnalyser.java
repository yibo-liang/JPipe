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
package jpipe.dynamic.Analysis;

import jpipe.abstractclass.PipeSection;

/**
 *
 * @author Yibo
 */
public class SectionAnalyser {

    private int workdoneAmount;
    private int workfailAmount;

    private long maxLatency;
    private long minLatency;
    private long totalWorkingLatency;
    private double meanLatency;
    private long blocktime;
    private long blockStartTime;
    private int ConsecutiveSuccCount = 0;
    private int maxSuccCount = 0;
    private int totalSuccCount = 0;
    private int succCountReset = 1;

    private PipeSection pipeSection; 

    public void setPipeSection(PipeSection pipeSection) {
        this.pipeSection = pipeSection;
    }
    
    public SectionAnalyser() {
        
    }

    public void BlockStart() {
        blockStartTime = System.nanoTime();
    }

    public long getBlockRunningTime() {
        return System.nanoTime() - blockStartTime;
    }

    public void BlockFinish() {
        blocktime = System.nanoTime() - blockStartTime;
        
    }

    public void workdone(long latency) {
        //System.out.println("Workdone!" + latency);
        totalWorkingLatency += latency;
        workdoneAmount++;
        ConsecutiveSuccCount++;
        if (latency > maxLatency) {
            //System.out.println("new max!");
            maxLatency = latency;
        }
        if (latency < minLatency) {
            minLatency = latency;
        }
        meanLatency = totalWorkingLatency / (double) workdoneAmount;

    }

    public void workfail() {
        workfailAmount++;
        if (ConsecutiveSuccCount > 0) {
            if (ConsecutiveSuccCount > maxSuccCount) {
                maxSuccCount = ConsecutiveSuccCount;
            }
            totalSuccCount += ConsecutiveSuccCount;
            succCountReset++;
            ConsecutiveSuccCount = 0;
        }
    }

    public SectionAnalysisResult analyseNano() {
        long timespent = this.getBlockRunningTime();
        SectionAnalysisResult result = new SectionAnalysisResult();
        result.setAverageLatency(meanLatency);
        result.setMaximumLatency(maxLatency);
        result.setMinimumLatency(minLatency);
        result.setBlockThroughput(workdoneAmount / (double) timespent);
        result.setWorkdoneAmount(workdoneAmount);
        result.setBlockRunningTime(getBlockRunningTime());
        result.setMaxConsecutiveSuccCount(maxSuccCount);
        result.setLifetimeAverageConsecutiveSuccCount((double) totalSuccCount / (double) succCountReset);
        result.setPipeSectionState(this.pipeSection.getState());
        result.setWorkerState(this.pipeSection.getWorker().getState());
        
        //SoftReset();
        return result;

    }

    public SectionAnalysisResult analyseMs() {
        long timespent = this.getBlockRunningTime();
        SectionAnalysisResult result = new SectionAnalysisResult();
        result.setAverageLatency(meanLatency / (Math.pow(10, 6)));
        result.setMaximumLatency((long) (maxLatency / (Math.pow(10, 6))));
        result.setMinimumLatency((long) (minLatency / (Math.pow(10, 6))));
        result.setBlockThroughput(workdoneAmount / (timespent / (Math.pow(10, 9))));
        result.setWorkdoneAmount(workdoneAmount);
        result.setWorkfailAmount(workfailAmount);
        result.setBlockRunningTime((long) ((getBlockRunningTime()) / (Math.pow(10, 6))));

        result.setMaxConsecutiveSuccCount(maxSuccCount);
        result.setLifetimeAverageConsecutiveSuccCount((double) totalSuccCount / (double) succCountReset);
        result.setCurrentConsecutiveSeccCount((double) ConsecutiveSuccCount);
        
        result.setPipeSectionState(this.pipeSection.getState());
        result.setWorkerState(this.pipeSection.getWorker().getState());
        //SoftReset();
        return result;

    }
}
