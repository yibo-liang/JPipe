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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import jpipe.abstractclass.buffer.Buffer;

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
public class LUBuffer<E> extends Buffer {

    private final Queue<E> queue;
    private int maxsize = 0;
    private int count = 0;

    private HashMap<Class, Integer> pollerRecord = new HashMap<>();
    private HashMap<Class, Integer> pusherRecord = new HashMap<>();

    public LUBuffer() {
        this.maxsize = 0;
        queue = new LinkedList<>();
    }

    public LUBuffer(int maxsize) {
        this.maxsize = maxsize;
        queue = new LinkedList<>();
    }

    private int itemCount = 0;

    public int getItemCount() {
        return itemCount;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public synchronized boolean push(Object pusher, Object obj) {
        register(pusher, Buffer.PRODUCER);

        if (maxsize > 0) {
            if (queue.size() < maxsize) {
                queue.add((E) obj);

            } else {
                return false;
            }
        } else {
            queue.add((E) obj);

        }
        recordPushing(pusher);
        count++;
        itemCount++;
        try {
            this.notifyConsumer();
        } catch (Exception ex) {

        }
        return true;
    }

    public String getPollingRecordToString() {
        String temp2 = "{";
        for (Iterator<Map.Entry<Class, Integer>> it = pollerRecord.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class, Integer> pair = it.next();
            temp2 += " \"" + pair.getKey().getSimpleName() + "\" : " + pair.getValue() + "";
            //  System.out.println(pair.getKey() + "," + pair.getValue());
            if (it.hasNext()) {
                temp2 += ",";
            }
        }
        temp2 += " }";
        return temp2;
    }

    public String getPushingRecordToString() {
        String temp2 = "{";
        for (Iterator<Map.Entry<Class, Integer>> it = pusherRecord.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Class, Integer> pair = it.next();
            temp2 += " \"" + pair.getKey().getSimpleName() + "\" : " + pair.getValue() + "";
            //  System.out.println(pair.getKey() + "," + pair.getValue());
            if (it.hasNext()) {
                temp2 += ",";
            }
        }
        temp2 += " }";
        return temp2;
    }

    public HashMap getPollingRecord() {
        return this.pollerRecord;
    }

    private synchronized void recordPolling(Object poller) {
        Integer p = this.pollerRecord.get(poller.getClass());
        if (p == null) {
            this.pollerRecord.put(poller.getClass(), 1);
        } else {
            this.pollerRecord.put(poller.getClass(), p + 1);
        }
    }

    public HashMap getPushingRecord() {
        return this.pusherRecord;
    }

    private synchronized void recordPushing(Object pusher) {
        Integer p = this.pusherRecord.get(pusher.getClass());
        if (p == null) {
            this.pusherRecord.put(pusher.getClass(), 1);
        } else {
            this.pusherRecord.put(pusher.getClass(), p + 1);
        }
    }

    @Override
    public synchronized E poll(Object poller) {
        register(poller, Buffer.CONSUMER);

        if (count > 0) {
            count--;
            try {
                this.notifyProduer();
            } catch (Exception ex) {

            }
            recordPolling(poller);
            return queue.poll();
        } else {
            return null;
        }
    }

    @Override
    public synchronized E peek(Object peeker) {
        register(peeker, Buffer.CONSUMER);
        return queue.peek();
    }

    @Override
    public synchronized void clear() {
        count = 0;
        queue.clear();
        try {
            this.notifyProduer();
        } catch (Exception ex) {

        }
    }

    @Override
    public int getSize() {
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
