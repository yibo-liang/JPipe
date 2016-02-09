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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.buffer.util.SocketMessage;

/**
 *
 * @author yl9
 * @param <E>
 */
public class SocketBuffer<E extends Serializable> extends Buffer {

    public static int SERVER = 1000;
    public static int CLIENT = 2000;

    private final int socketType;

    private Thread connector;
    public static int DEFAULT_PORT = 60001;

    private String server;
    private final int port;

    private int pushMessageinQUeue = 0;
    private int serverCount = 0;
    private int clientCount = 0;
    private int size = 20;

    private synchronized void syncCount(int n) {
        serverCount = n;

    }

    //BufferQueue for client buffer
    private final Queue<SocketMessage<E>> MessageQueue = new LinkedBlockingDeque<>();
    //Queue for server buffer
    private final Queue<E> StorageQueue = new LinkedList();

    /*
     private synchronized void clientSocketPush(E Object) {
     localCount++;
     StorageQueue.add(Object);
     }

     private synchronized E clientSocketPoll() {
     localCount--;
     return StorageQueue.poll();

     }
     */
    private synchronized E clientSocketPeek() {
        return StorageQueue.peek();
    }

    private void setSocket() {
        //ConnectionThread = new SocketConnection(socketType, this);
        //ConnectionThread.start();
    }

    /**
     * Create a Server side SocketBuffer with default port
     *
     */
    public SocketBuffer() {
        this.socketType = SERVER;
        this.port = DEFAULT_PORT;
        setSocket();
    }

    /**
     * Create a Client side SocketBuffer
     *
     * @param server
     * @param port
     */
    public SocketBuffer(String server, int port) {
        this.socketType = CLIENT;

        this.server = server;
        this.port = port;
        setSocket();
    }

    /**
     * Create a SocketBuffer server with specified port.
     *
     * @param port
     * @throws java.io.IOException
     */
    public SocketBuffer(int port) {
        this.socketType = SERVER;
        this.port = port;
        setSocket();
    }

    @Override
    public synchronized boolean push(Object callerKey, Object obj) {
        if (callerKey != null) {
            register(callerKey, PRODUCER);
        }
        if (this.serverCount + pushMessageinQUeue < size || size == 0) {

            if (socketType == CLIENT) {
                MessageQueue.add(new SocketMessage<>("PUSH", null, (E) obj));
                //ConnectionThread.noticeClientConnetor();
                pushMessageinQUeue++;
                return true;
            } else {
                StorageQueue.add((E) obj);
                serverCount++;
                notifyConsumer();
                //System.out.println("Pushed " + obj.toString());
                return true;
            }

        }
        return false;
    }

    @Override
    public synchronized E poll(Object callerKey) {
        if (callerKey != null) {
            register(callerKey, CONSUMER);
        }
        if (socketType == SERVER) {
            if (serverCount > 0) {
                serverCount--;
                notifyProduer();
                return StorageQueue.poll();
            } else {
                return null;
            }
        } else if (socketType == CLIENT) {
            if (clientCount > 0) {
                clientCount--;
                return StorageQueue.poll();
            } else {
                MessageQueue.add(new SocketMessage<>("POLL", null, null));
                //ConnectionThread.noticeClientConnetor();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public synchronized E peek(Object callerKey) {
        if (callerKey != null) {
            register(callerKey, CONSUMER);
        }
        System.out.println("peek, count=" + serverCount);
        if (socketType == SERVER) {
            if (this.serverCount > 0) {
                return StorageQueue.peek();
            } else {
                return null;
            }
        } else if (socketType == CLIENT) {
            if (clientCount > 0) {
                return StorageQueue.peek();
            } else {
                MessageQueue.add(new SocketMessage<>("PEEK", null, null));
                //ConnectionThread.noticeClientConnetor();
                return null;
            }
        } else {
            return null;
        }

    }

    @Override
    public synchronized void clear() {
        if (socketType == SERVER) {
            StorageQueue.clear();
            serverCount = 0;
            this.notifyProduer();
        }
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public synchronized boolean setSize(int maxsize) {
        if (socketType == SERVER) {
            if (serverCount < maxsize) {
                this.size = maxsize;
                this.notifyProduer();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public int getCount() {
        return socketType == SERVER ? serverCount : clientCount;
    }

}
