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
package jpipe.buffer.util;

import java.io.Serializable;
import jpipe.interfaceclass.IWorker;

/**
 * An Object that stores a secondaryMessage with a serialisable object. This is
 * used for socket buffer, when a push is sent from client buffer to server
 * buffer, along with the pushed item;
 *
 *
 *
 * @author yl9
 * @param <E>
 */
public class SocketMessage<E extends Serializable> implements Serializable {

    private static final long serialVersionUID = 1239124812993103311L;
    E obj;
    String secondaryMessage;
    String primaryMessage;

    public String getPrimaryMessage() {
        return primaryMessage;
    }

    public void setPrimaryMessage(String primaryMessage) {
        this.primaryMessage = primaryMessage;
    }

    public E getObj() {
        return obj;
    }

    public void setObj(E obj) {
        this.obj = obj;
    }

    public String getsecondaryMessage() {
        return secondaryMessage;
    }

    public void setsecondaryMessage(String command) {
        this.secondaryMessage = command;
    }

    public SocketMessage(String primaryMessage, String secondaryMesage, Serializable messageObject) {
        this.secondaryMessage = secondaryMesage;
        this.obj = (E) messageObject;
        this.primaryMessage = primaryMessage;
    }

}
