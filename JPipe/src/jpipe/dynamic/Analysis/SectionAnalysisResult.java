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

/**
 *
 * @author yl9
 */
public class SectionAnalysisResult {

    private double BlockThroughput;
    private double AverageLatency;
    private long MaximumLatency;
    private long MinimumLatency;
    private int workdoneAmount;
    private int workfailAmount;
    private long blockRunningTime;
    
    private double LifetimeAverageConsecutiveSuccCount;
    private double currentConsecutiveSeccCount;
    
    private long maxSuccCount;

    private int pipeSectionState;
    private int workerState;

    public int getPipeSectionState() {
        return pipeSectionState;
    }

    public void setPipeSectionState(int pipeSectionState) {
        this.pipeSectionState = pipeSectionState;
    }

    public int getWorkerState() {
        return workerState;
    }

    public void setWorkerState(int workerState) {
        this.workerState = workerState;
    }
    
    
    public double getLifetimeAverageConsecutiveSuccCount() {
        return LifetimeAverageConsecutiveSuccCount;
    }

    public void setLifetimeAverageConsecutiveSuccCount(double averageSuccCount) {
        this.LifetimeAverageConsecutiveSuccCount = averageSuccCount;
    }

    public long getMaxConsecutiveSuccCount() {
        return maxSuccCount;
    }

    public void setMaxConsecutiveSuccCount(long maxSuccCount) {
        this.maxSuccCount = maxSuccCount;
    }
    
    
    
    public int getWorkfailAmount() {
        return workfailAmount;
    }

    public void setWorkfailAmount(int workfailAmount) {
        this.workfailAmount = workfailAmount;
    }

    public long getBlockRunningTime() {
        return blockRunningTime;
    }

    public void setBlockRunningTime(long blockRunningTime) {
        this.blockRunningTime = blockRunningTime;
    }

    public int getWorkdoneAmount() {
        return workdoneAmount;
    }

    public void setWorkdoneAmount(int workdoneAmount) {
        this.workdoneAmount = workdoneAmount;
    }

    public double getBlockThroughput() {
        return BlockThroughput;
    }

    public void setBlockThroughput(double BlockThroughput) {
        this.BlockThroughput = BlockThroughput;
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

    public double getCurrentConsecutiveSeccCount() {
        return currentConsecutiveSeccCount;
    }

    public void setCurrentConsecutiveSeccCount(double currentConsecutiveSeccCount) {
        this.currentConsecutiveSeccCount = currentConsecutiveSeccCount;
    }

    
    
    @Override
    public String toString() {
        return "========================================\n"
                + "BlockThroughput:" + this.BlockThroughput + "\n"
                + "AverageLatency:" + this.AverageLatency + "\n"
                + "MaximumLatency:" + this.MaximumLatency + "\n"
                + "MinimumLatency:" + this.MinimumLatency + "\n"
                + "workdoneAmount:" + this.workdoneAmount + "\n"
                + "workfailAmount:" + this.workfailAmount + "\n"
                + "blockRunningTime:" + this.blockRunningTime + "\n"
                + "======================================\n";
    }

}
