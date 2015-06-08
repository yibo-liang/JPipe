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

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.dynamic.Analysis.SectionAnalyser;
import jpipe.interfaceclass.IBUffer;
import jpipe.interfaceclass.IWorker;

/**
 *
 * @author Yibo
 */
public class PipeSection implements Runnable {

    private IBUffer[] buffers;
    private ConcurrentPipes manager;
    private SectionAnalyser analyser;

    private IWorker worker;

    private boolean Running = true;
    private boolean Pausing = false;

    private long worktimer;

    public void setAnalyser(SectionAnalyser analyser) {
        this.analyser = analyser;
    }

    public boolean isRunning() {
        return Running;
    }

    public void setRunning(boolean isRunning) {
        this.Running = isRunning;
    }

    public synchronized void setWorker(IWorker worker) {
        this.worker = worker;
    }

    public PipeSection(IWorker worker, IBUffer[] buffers) {
        this.buffers = buffers;
        this.worker = worker;
    }

    public void pause() {
        this.Pausing = true;
    }

    public void resume() {
        synchronized (this) {
            this.Pausing = false;
            notifyAll();
        }
    }

    @Override
    public void run() {

        if (analyser != null) {
            analyser.BlockStart();
        }
        while (manager == null || Running) {
            synchronized (this) {
                while (Pausing) {
                    try {

                        wait();

                    } catch (InterruptedException ex) {
                        Logger.getLogger(PipeSection.class.getName()).log(Level.SEVERE, null, ex);
                        Pausing = false;// if pausing failed, the process would not pass to prevent deadlock
                    }

                }
            }
            WorkStart();
            //System.out.println(this.worker);
            boolean result = this.worker.work(this.buffers);
            if (result == true) {
                WorkFinish();
            } else {
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
