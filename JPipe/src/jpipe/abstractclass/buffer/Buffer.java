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
package jpipe.abstractclass.buffer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jpipe.abstractclass.Immutable;
import jpipe.abstractclass.worker.Worker;
import jpipe.interfaceclass.IBuffer;
import jpipe.interfaceclass.IPipeSectionLazy;
import jpipe.interfaceclass.IWorker;

/**
 * This is a buffer used for task parallelism
 *
 * @author Yibo
 * @param <E>
 */
public abstract class Buffer<E> extends Immutable implements IBuffer {

    public static final int PRODUCER = 1;
    public static final int CONSUMER = 2;

    private int producerCount = 0;
    private int consumerCount = 0;

    private final HashMap<IWorker, Integer> producers = new HashMap();
    private final HashMap<IWorker, Integer> consumers = new HashMap();

    private int relationCount = 0;
    
 
    public Buffer() {
        super();
    }

    public int register(Object callerKey, int identity) {
        if (identity == PRODUCER) {
            producers.put((IWorker) callerKey, producerCount);
            producerCount++;
        } else if (identity == CONSUMER) {
            consumers.put((IWorker) callerKey, consumerCount);
            consumerCount++;
        } else {
            throw new UnsupportedOperationException("Unknown Relation to the buffer.");
        }
        relationCount++;
        return relationCount;
    }

    public abstract void clear();

    public abstract int getSize();

    public abstract boolean setSize(int maxsize);
//

    public boolean isNotifierConsumer(IWorker notifier) {
        return consumers.get(notifier) != null;
    }
//Notify all consumers

    public void notifyConsumer() {

        Iterator it = consumers.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Worker, Integer> pair = (Entry) it.next();
            ((IPipeSectionLazy) pair.getKey().getWrapPipeSection()).getNotifiedByOther();
            //System.out.println("notifying! consumers");
            it.remove();
        }

    }

    public boolean isNotifierProducer(IWorker notifier) {
        return producers.get(notifier) != null;
    }
//notify all producers
    public void notifyProduer() {

        Iterator it = producers.entrySet().iterator();
        while (it.hasNext()) {
            Entry<Worker, Integer> pair = (Entry) it.next();
            ((IPipeSectionLazy) pair.getKey().getWrapPipeSection()).getNotifiedByOther();
            //System.out.println("notifying! producer");
            it.remove();
        }

    }

}
