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
import jpipe.abstractclass.buffer.Buffer;
import jpipe.buffer.util.BufferStore;
import jpipe.interfaceclass.IPipeSection;
import jpipe.interfaceclass.IWorkerLazy;

/**
 * An abstract class for Worker, for the sake of immutability
 *
 * @author yl9
 */
public abstract class Worker implements IWorkerLazy {

    public static int FAIL = 0;
    public static int SUCCESS = 1;
    public static int NO_INPUT = 10;

    private final int hashCache;

    private BufferStore bufferStore = null;

    private IPipeSection wrapPipeSection;
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

    private int laziness = 0;

    public void getNotified() {
        this.laziness = laziness;
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
        while (!bf.push(this, item)) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
                }
               // System.out.println("blocked push item=" + item);
            }
        };
    }

    public Worker() {
        super();
        this.hashCache = (new Object()).hashCode();
    }

    @Override
    public int hashCode() {
        return hashCache;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Worker other = (Worker) obj;
        return this.hashCode() == other.hashCode();
    }
}
