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

import jpip.singletons.WorkerStates;
import jpip.singletons.PipeSectionStates;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.interfaceclass.IBuffer;

/**
 *
 * @author Yibo
 */
public class MultiPipeSectionAnalysisResult {

    private double SectionThroughput;
    private double AverageLatency;
    private long MaximumLatency;
    private long MinimumLatency;
    private int workdoneAmount;
    private int workfailAmount;
    private long sectionRunningTime;
    private long maxCountinuousSuccess;
    private double LifetimeAverageContinuousSuccess;
    private double currentConsecutiveSeccCount;

    private HashMap<Integer, Integer> PipsStates = new HashMap<>();

    private HashMap<Integer, Integer> WorkersStates = new HashMap<>();

    public void addPipeState(Integer state) {
        Integer i = PipsStates.get(state);
        if (i == null) {
            i = Integer.valueOf(0);
        }
        PipsStates.put(state, Integer.valueOf(i + 1));
    }

    public void addWorkerState(Integer state) {

        Integer i = WorkersStates.get(state);
        if (i == null) {
            i = Integer.valueOf(0);
        }
        WorkersStates.put(state, Integer.valueOf(i + 1));
    }

    @Override
    public String toString() {

        String temp = "{ \"PipeSection_States\" : [";
        for (Iterator<Map.Entry<Integer, Integer>> it = PipsStates.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> pair = it.next();
            temp += "{ \"" + PipeSectionStates.state(pair.getKey()) + " \" : \"" + pair.getValue() + "\" }";
            if (it.hasNext()) {
                temp += ",";
            }
        }
        temp += "]}";
        String temp2 = "{ \"Worker_States\" : [";
        for (Iterator<Map.Entry<Integer, Integer>> it = WorkersStates.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Integer, Integer> pair = it.next();
            temp2 += "{ \"" + WorkerStates.state(pair.getKey()) + "\" : \"" + pair.getValue() + "\" }";
            if (it.hasNext()) {
                temp2 += ",";
            }
        }
        temp2 += "]}";

        String result = "Section Throughput: " + this.SectionThroughput + "\n";
        result += temp + "\n";
        result += temp2 + "\n";
        result += "Section Average Latency: " + this.AverageLatency + "\n";
        result += "Section Maximum Latency: " + this.MaximumLatency + "\n";
        result += "Section Minimum Latency: " + this.MinimumLatency + "\n";
        int total = (this.workdoneAmount + this.workfailAmount);
        float rate = total > 0 ? (float) this.workdoneAmount / (float) total : 0;
        result += "Section work done:" + this.workdoneAmount + "/" + total + " (" + rate * 100 + "%)\n";
        result += "Section max Countinuous Success: " + this.maxCountinuousSuccess + "\n";
        result += "Section Lifetime average Continuous Success: " + this.LifetimeAverageContinuousSuccess + "\n";
        result += "Section Current average Continuous Success: " + this.currentConsecutiveSeccCount + "\n";
        result += "Section section Running Time: " + this.sectionRunningTime + "\n";

        return result;
    }

    public long getMaxCountinuousSuccess() {
        return maxCountinuousSuccess;
    }

    public void setMaxCountinuousSuccess(long maxCountinuousSuccess) {
        this.maxCountinuousSuccess = maxCountinuousSuccess;
    }

    public double getLifetimeAverageContinuousSuccess() {
        return LifetimeAverageContinuousSuccess;
    }

    public void setLifetimeAverageContinuousSuccess(double LifetimeAverageContinuousSuccess) {
        this.LifetimeAverageContinuousSuccess = LifetimeAverageContinuousSuccess;
    }

    public double getCurrentConsecutiveSeccCount() {
        return currentConsecutiveSeccCount;
    }

    public void setCurrentConsecutiveSeccCount(double currentConsecutiveSeccCount) {
        this.currentConsecutiveSeccCount = currentConsecutiveSeccCount;
    }

    public long getSectionRunningTime() {
        return sectionRunningTime;
    }

    public void setSectionRunningTime(long sectionRunningTime) {
        this.sectionRunningTime = sectionRunningTime;
    }

    public double getSectionThroughput() {
        return SectionThroughput;
    }

    public void setSectionThroughput(double SectionThroughput) {
        this.SectionThroughput = SectionThroughput;
    }

    public double getAverageLatency() {
        return AverageLatency;
    }

    public void setAverageLatency(double AverageLatency) {
        this.AverageLatency = AverageLatency;
    }

    public long getMaximumLatency() {
        return MaximumLatency;
    }

    public void setMaximumLatency(long MaximumLatency) {
        this.MaximumLatency = MaximumLatency;
    }

    public long getMinimumLatency() {
        return MinimumLatency;
    }

    public void setMinimumLatency(long MinimumLatency) {
        this.MinimumLatency = MinimumLatency;
    }

    public int getWorkdoneAmount() {
        return workdoneAmount;
    }

    public void setWorkdoneAmount(int workdoneAmount) {
        this.workdoneAmount = workdoneAmount;
    }

    public int getWorkfailAmount() {
        return workfailAmount;
    }

    public void setWorkfailAmount(int workfailAmount) {
        this.workfailAmount = workfailAmount;
    }

}
