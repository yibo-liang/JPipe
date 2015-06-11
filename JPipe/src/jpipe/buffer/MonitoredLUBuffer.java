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
package jpipe.buffer;

import java.util.Deque;
import java.util.LinkedList;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.interfaceclass.IMonitor;

/**
 * This class is a locked implementation of buffer queue It has a maximum size
 * It would return null while polling, if nothing is in the buffer It would
 * return false if a push action failed Maximum size is by default 10 A
 * constructor with integer parameter can change it
 *
 *
 * @author Yibo
 * @param <E>
 */
public class MonitoredLUBuffer<E> extends LUBuffer<E> {

    private final Deque<E> queue;
    private int maxsize = 0;
    private int count = 0;
    private IMonitor monitor;

    public MonitoredLUBuffer(IMonitor monitor) {
        this.maxsize = 0;
        this.monitor = monitor;
        queue = new LinkedList<>();
    }

    public MonitoredLUBuffer(int maxsize, IMonitor monitor) {
        this.maxsize = maxsize;
        this.monitor = monitor;
        queue = new LinkedList<>();
    }

    @Override
    public synchronized boolean push(Object pusher, Object obj) {

        register(pusher, Buffer.PRODUCER);
        if (maxsize > 0) {
            if (queue.size() < maxsize && monitor.InspectIn(obj)) {
                queue.add((E) obj);

            } else {
                return false;
            }
        } else {
            if (monitor.InspectIn(obj)) {
                queue.add((E) obj);
            }
        }
        count++;
        return true;
    }

    @Override
    public synchronized E poll(Object poller) {
        register(poller, Buffer.CONSUMER);
        if (monitor.InspectOut(queue.poll())) {
            count--;
            return queue.poll();

        } else {
            return null;
        }

    }

    @Override
    public synchronized E peek(Object peeker) {
        register(peeker, Buffer.CONSUMER);
        if (monitor.InspectOut(queue.poll())) {
            return queue.peek();
        } else {
            queue.addLast(queue.peek());
            return null;
        }
    }

    @Override
    public synchronized void clear() {
        count = 0;
        queue.clear();
    }

    @Override
    public synchronized int getSize() {
        return this.maxsize;
    }

    @Override
    public synchronized boolean setSize(int maxsize) {
        //only change when the new size is larger than 
        //the count of the element in the queue
        if (maxsize >= count) {
            this.maxsize = maxsize;
            return true;
        }
        return false;
    }

}
