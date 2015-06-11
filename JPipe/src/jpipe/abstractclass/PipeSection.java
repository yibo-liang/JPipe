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
package jpipe.abstractclass;

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.worker.Worker;
import jpipe.core.pipeline.SinglePipeSection;
import jpipe.dynamic.Analysis.SectionAnalyser;
import jpipe.interfaceclass.IPipeSectionLazy;

/**
 *
 * @author Yibo Liang
 */
public abstract class PipeSection extends Immutable implements IPipeSectionLazy {

    public SectionAnalyser analyser;

    private long worktimer;

    private boolean Running = true;
    private boolean Pausing = false;

    private int laziness = 0;

    private boolean Lazy = false;
    private boolean LazyResting = false;

    private Worker worker;

    public Worker getWorker() {
        return worker;
    }

    public void setWorker(Worker worker) {
        this.worker = worker;
        this.worker.setWrapPipeSection(this);
    }

    public void setAnalyser(SectionAnalyser analyser) {
        this.analyser = analyser;
    }

    /**
     * Is the worker of this pipe section lazy
     *
     * @return
     */
    public boolean isLazy() {
        return Lazy;
    }

    /**
     * Is the worker of this pipe section lazily resting
     *
     * @return
     */
    public boolean isLazyResting() {
        return LazyResting;
    }

    /**
     * is the pipe section in a working state or ready to work
     *
     * @return
     */
    public boolean isRunning() {
        return Running;
    }

    /**
     * is this pipe section is pausing after last finished work
     *
     * @return
     */
    public boolean isPausing() {
        return Pausing;
    }

    /**
     * Terminate this pipe section thread after the current work is done.
     */
    public void terminate() {
        this.Running = false;
    }

    /**
     * The amount of laziness of this worker. The time to rest after each work.
     * and the time to push again if the previous push failed
     *
     * @param laziness
     */
    public void setLaziness(int laziness) {
        this.laziness = laziness;
        this.Lazy = this.laziness > 0;
        if (Lazy == false) {
            synchronized (this) {
                notifyAll();
            }
        }
    }

    public int getLaziness() {
        return laziness;
    }

    public void rest() {
        setLaziness(9000);
    }

    @Override
    public void pause() {
        //System.out.println("Being paused by someone");

        this.Pausing = true;
        try {
            throw new Exception("GGG");
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    @Override
    public void resume() {
        synchronized (this) {
            //resume from pausing and resting
            this.Pausing = false;
            this.LazyResting = false;
            notifyAll();
        }
    }

    public void WorkStart() {
        if (analyser != null) {
            worktimer = System.nanoTime();
        }
    }

    public void WorkFinish(int workerState) {
        if (workerState == Worker.SUCCESS) {
            if (analyser != null) {
                long latency = System.nanoTime() - worktimer;
                analyser.workdone(latency);

            }
            return;
        } else if (workerState == Worker.FAIL) {
            if (analyser != null) {
                analyser.workfail();

            }
            return;
        }

        synchronized (this) {
            if (workerState == Worker.NO_INPUT) {
                // System.out.println("No input, go Lazy if lazy");
                if (Lazy) {
                    rest();
                    try {
                        this.LazyResting = true;
                        // System.out.println("Resting !!!!!!!!!!!!!!!!!!!!!!!!!"+laziness);
                        wait();
                        this.LazyResting = false;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PipeSection.class.getName()).log(Level.SEVERE, null, ex);
                        this.resume();
                    }

                }
            }
        // System.out.println("berfore lazy");

            //System.out.println("berfore  22  lazy p="+Pausing+", l="+laziness);
            while (Pausing) {

                try {
                    if (Pausing) {
                        //System.out.println("Pausing !!!!!!!!!!!!!!!!!!!!!!!!!"+laziness);
                        wait(this.laziness);
                    }

                } catch (InterruptedException ex) {
                    Logger.getLogger(SinglePipeSection.class.getName()).log(Level.SEVERE, null, ex);
                    this.resume();// if pausing failed, the process would not pass to prevent deadlock
                }

            }

        }

    }

}
