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
import jpip.singletons.PipeSectionStates;
import jpipe.dynamic.Analysis.SectionAnalyser;
import jpip.singletons.WorkerStates;
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

    //state for analysis purpose
    private int state = PipeSectionStates.INITIAL;

    public long getWorktimer() {
        return worktimer;
    }

    public void setWorktimer(long worktimer) {
        this.worktimer = worktimer;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

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
        this.analyser.setPipeSection(this);
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

    @Override
    public void pause() {
        //System.out.println("Being paused by someone");

        this.Pausing = true;

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
        this.state = PipeSectionStates.WORKING;
        this.getWorker().setState(WorkerStates.WORKING);
    }

    public void WorkFinish(int workResult) {
        if (workResult == Worker.SUCCESS) {
            if (analyser != null) {
                long latency = System.nanoTime() - worktimer;
                analyser.workdone(latency);

            }
            return;
        } else if (workResult == Worker.FAIL) {
            if (analyser != null) {
                analyser.workfail();

            }
            return;
        }

        synchronized (this) {
            if (workResult == Worker.NO_INPUT) {
                if (Lazy) {

                    try {
                        this.LazyResting = true;
                        this.state = PipeSectionStates.RESTING;
                        wait();
                        this.LazyResting = false;
                    } catch (InterruptedException ex) {
                        Logger.getLogger(PipeSection.class.getName()).log(Level.SEVERE, null, ex);
                        this.resume();
                    }

                }
            }
            while (Pausing) {

                try {
                    if (Pausing) {
                        this.state = PipeSectionStates.PAUSING;
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
