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
package jpipe.bufferclass;

import java.util.List;
import jpipe.abstractclass.TPBuffer;

/**
 * This class implements a Queue buffer in a lock free manner To use this buffer
 * requires that there is only one thread consuming element and another one
 * producing.
 *
 * Size of buffer cannot be changed, Size of buffer is by default 20 Due to the
 * design, the actual quantity of elements would be size-1
 *
 * @author Yibo
 * @param <E>
 */
public class QBufferLockfree<E> extends TPBuffer {

    E[] buffer;
    int buffersize = 20;
    //head is the index of element being peeked//polled
    private int head = 0;

    private int tail = 0;

    public QBufferLockfree(int size) {
        this.buffer = (E[]) new Object[size];
    }

    public QBufferLockfree() {

        this(20);

    }

    @Override
    public boolean push(Object obj) {

        int nextTail = (tail + 1) % buffersize;
        if (head != nextTail) {
            buffer[tail] = (E) obj;
            tail = nextTail;
            return true;
        }
        return false;
    }

    @Override
    public E poll() {
        int nextHead = (head + 1) % buffersize;
        if (tail != head) {
            head = nextHead;
            return buffer[head];
        }

        return null;
    }

    @Override
    public E peek() {
        if (tail != head) {
            return buffer[head];
        }
        return null;
    }

    @Override
    public void clear() {
        tail = head;
    }

    @Override
    public int getMaxsize() {
        return buffersize;
    }

    @Override
    public boolean setMaxsize(int maxsize) {
        //not allowed, lock free pattern requires no change from a third thread
        //and producer and consumer should not change it
        return false;
    }

    @Override
    public List<E> pollAll() {
        //not allowed because a single consumer should not need to poll all elements
        return null;
    }

}
