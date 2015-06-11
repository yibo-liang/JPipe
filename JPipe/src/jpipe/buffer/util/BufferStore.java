/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpipe.buffer.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.interfaceclass.IBuffer;
import jpipe.interfaceclass.IWorker;

/**
 *
 * @author yl9
 */
public class BufferStore {

    private final HashMap<String, IBuffer> buffers = new HashMap<>();

    public BufferStore() {

    }

    public Buffer use(String name) {
        return (Buffer) buffers.get(name);
    }

    public void put(String name, Buffer buffer) {
        buffers.put(name, buffer);

    }

    public void delete(String name) {
        buffers.remove(name);
    }

    public void clear() {
        buffers.clear();
    }

    public void printBufferState(){
        
        for (Iterator<Entry<String, IBuffer>> it = buffers.entrySet().iterator(); it.hasNext();) {
            Entry<String, IBuffer> pair = it.next();
            Buffer b = ((Buffer) pair.getValue());
            System.out.println(pair.getKey()+":"+b.getCount()+"/"+b.getSize());
            
        }
    }
    /*
    public void notifyPipeSections(IWorker notifier) {

        for (Iterator<Entry<String, IBuffer>> it = buffers.entrySet().iterator(); it.hasNext();) {
            Entry<String, IBuffer> pair = it.next();
            Buffer b = ((Buffer) pair.getValue());
            if (b.isNotifierConsumer((IWorker) notifier)) {
                b.notifyProduer();
            }
            
            if (b.isNotifierProducer((IWorker) notifier)) {
                b.notifyConsumer();
            }
            it.remove();
        }
    }
    */
}
