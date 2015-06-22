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
package jpipe.abstractclass.worker;

import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.Immutable;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.buffer.util.BufferStore;
import jpip.singletons.WorkerStates;
import jpipe.buffer.SocketBuffer;
import jpipe.interfaceclass.IPipeSection;
import jpipe.interfaceclass.IWorkerLazy;

/**
 * An abstract class for Worker, for the sake of immutability
 *
 * @author yl9
 */
public abstract class Worker extends Immutable implements IWorkerLazy {

    public static int FAIL = 0;
    public static int SUCCESS = 1;
    public static int NO_INPUT = 10;

    private BufferStore bufferStore = null;

    private IPipeSection wrapPipeSection;

    private int State = WorkerStates.INITIAL;

    public int getState() {
        return State;
    }

    public void setState(int State) {
        this.State = State;
    }

    final Object noticeLock = new Object();

    public IPipeSection getWrapPipeSection() {
        if (this.wrapPipeSection == null) {
            return null;
        }
        return wrapPipeSection;
    }

    public void setWrapPipeSection(IPipeSection wrapPipeSection) {
        this.wrapPipeSection = wrapPipeSection;
    }

    public void getNotified() {
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public BufferStore getBufferStore() {
        return bufferStore;
    }

    @Override
    public void setBufferStore(BufferStore bufferStore) {
        this.bufferStore = bufferStore;
    }

    @SuppressWarnings("empty-statement")
    public void blockedpush(Buffer bf, Object item) {
        int interval = 0;
        if (bf instanceof SocketBuffer) {
            interval = 3000;
        }
        while (!bf.push(this, item)) {
            synchronized (this) {
                try {
                    this.State = WorkerStates.BLOCKED_PUSHING;
                    wait(interval);
                    this.State = WorkerStates.WORKING;
                } catch (InterruptedException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
                // System.out.println("blocked push item=" + item);
            }
        };
    }

    @SuppressWarnings("empty-statement")
    public Object blockedpoll(Buffer bf) {
        int interval = 0;
        if (bf instanceof SocketBuffer) {
            //System.out.println("Socket buffer detected");
            interval = 3000;
        }
        Object result =bf.poll(this);;
        while (result == null) {
            synchronized (this) {
                try {
                    this.State = WorkerStates.BLOCKED_POLLING;
                    wait(interval);
                    this.State = WorkerStates.WORKING;
                    
                    result = bf.poll(this);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        };
        return result;
    }

    public Worker() {
        super();
    }

}
