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
package jpipe.util.socket;

import java.util.LinkedList;
import java.util.Queue;
import jpipe.buffer.util.SocketMessage;

/**
 *
 * @author yl9
 */
public class AsyncSocket {

    private class MsgSender extends Thread {

    }

    private class MsgReceiver extends Thread {

    }

    private class Client extends Thread {

        public Client(String server, int port, Queue msgQueue) {

        }

    }

    private class Server extends Thread {

        public Server(int port, Queue msgQueue) {

        }

    }

    private boolean isServer;
    private String server;
    private int port;
    private Thread communicator;

    private Queue<SocketMessage> messageQueue = new LinkedList<>();

    /**
     * Construct as a server socket
     *
     * @param port
     */
    public AsyncSocket(int port) {
        this.port = port;
        isServer = true;
        initiate();
    }

    /**
     * Construct as a client socket
     *
     * @param server
     * @param port
     */
    public AsyncSocket(String server, int port) {
        this.server = server;
        this.port = port;
        isServer = false;
        initiate();
    }

    private void initiate() {
        if (isServer) {
            this.communicator = new Server(port, messageQueue);
        } else {
            this.communicator = new Client(server, port, messageQueue);
        }
        communicator.start();
    }

}
