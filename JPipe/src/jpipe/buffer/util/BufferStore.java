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

    public String BufferStates() {
        String result = "{[";
        for (Iterator<Entry<String, IBuffer>> it = buffers.entrySet().iterator(); it.hasNext();) {
            try {
                Entry<String, IBuffer> pair = it.next();
                Buffer b = ((Buffer) pair.getValue());
                result += "{ \"name\" : \"" + pair.getKey() + "\" ,"
                        + " \"info\" : { \"itemNumber\" :" + b.getCount() + ", \"maximum\" : " + b.getSize() + " }";
                if (it.hasNext()) {
                    result += ",\n";
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        result += "]}";
        return result;
    }
}
