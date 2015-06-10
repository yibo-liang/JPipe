/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpipe.buffer.util;

import java.util.HashMap;
import jpipe.abstractclass.TPBuffer;
import jpipe.interfaceclass.IBUffer;

/**
 *
 * @author yl9
 */
public class TPBufferStore{

    private static final TPBufferStore instance = null;
    private static final HashMap<String, TPBuffer> buffers = new HashMap<>();

    private TPBufferStore() {

    }

    public static TPBufferStore getInstance() {
        if (instance == null) {
            return new TPBufferStore();
        } else {
            return instance;
        }
    }

    public static TPBuffer use(String name) {
        return (TPBuffer)buffers.get(name);
    }

    public static void put(String name, TPBuffer buffer) {
        buffers.put(name, buffer);

    }

    public static void delete(String name) {
        buffers.remove(name);
    }
    
    public static void clear(){
        buffers.clear();
    }
}
