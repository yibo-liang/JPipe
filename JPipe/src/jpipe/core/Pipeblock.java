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
 * THE SOFTWARE.3
 */
package jpipe.core;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.dynamic.Analysis.BlockAnalyser;
import jpipe.interfaceclass.BufferInterface;
import jpipe.interfaceclass.WorkerInterface;

/**
 *
 * @author Yibo
 */
public class Pipeblock implements Runnable {

    private ArrayList<BufferInterface> buffers;
    private SectionManager manager;
    private BlockAnalyser analyser;

    private WorkerInterface worker;

    private boolean Pausing = false;
    private boolean Running = true;

    private long worktimer;

    public void setAnalyser(BlockAnalyser analyser) {
        this.analyser = analyser;
    }

    public boolean isPausing() {
        return Pausing;
    }

    public void setPausing(boolean isPausing) {
        this.Pausing = isPausing;
    }

    public boolean isRunning() {
        return Running;
    }

    public void setRunning(boolean isRunning) {
        this.Running = isRunning;
    }

    public synchronized void setWorker(WorkerInterface worker) {
        this.worker = worker;
    }

    public Pipeblock(WorkerInterface worker, ArrayList<BufferInterface> buffers) {
        this.buffers = buffers;
        this.worker = worker;
    }

    @Override
    public void run() {

        synchronized (this) {
            if (analyser != null) {
                analyser.BlockStart();
            }
            while (manager == null || Running) {
                // handle pause
                // pause can only happen before a work, not while it is working
                if (Pausing) {
                    try {
                        wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Pipeblock.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                WorkStart();
                //System.out.println(this.worker);
                int result = this.worker.work(this.buffers);
                if (result == 1) {
                    WorkFinish();
                }else
                    WorkFail();
            }
        }

    }

    private void WorkStart() {
        worktimer = System.nanoTime();
    }

    private void WorkFinish() {
        long latency = System.nanoTime() - worktimer;
        if (analyser != null) {
            analyser.workdone(latency);
        }
    }

    private void WorkFail() {
        if (analyser != null) {
            analyser.workfail();
        }
    }
}
