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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import jpipe.abstractclass.buffer.Buffer;
import jpipe.buffer.util.SocketMessage;
import jpipe.interfaceclass.IWorker;

/**
 *
 * @author yl9
 * @param <E>
 */
public class SocketBuffer<E extends Serializable> extends Buffer {

    public static int SERVER = 1000;
    public static int CLIENT = 2000;

    private class SocketConnection extends Thread {

        public class ServerEchoThread extends Thread {

            protected Socket socket;

            private SocketBuffer buffer;

            public ServerEchoThread(Socket clientSocket, SocketBuffer buffer) {
                this.socket = clientSocket;
                this.buffer = buffer;
            }

            @Override
            public void run() {
                System.out.println("Run Client");
                while (!shutdown) {
                    try {
                        ObjectOutputStream outs = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream ins = new ObjectInputStream(socket.getInputStream());

                        SocketMessage<Serializable> obj = (SocketMessage) ins.readObject();
                        String message = obj.getPrimaryMessage();
                        SocketMessage resultMessage;
                        E resultObj;
                        System.out.println("Receive message=" + message + ", obj=" + obj.getObj());
                        switch (message) {
                            case "PUSH":
                                Boolean pushResult = buffer.push(null, (E) obj.getObj());

                                resultMessage = new SocketMessage<>("PUSHRESULT", String.valueOf(serverCount), pushResult);

                                outs.writeObject(resultMessage);
                                buffer.notifyConsumer();
                                break;
                            case "POLL":
                                resultObj = (E) buffer.poll(null);
                                if (resultObj != null) {
                                    resultMessage = new SocketMessage<>("POLLRESULT", String.valueOf(serverCount), resultObj);
                                } else {
                                    resultMessage = new SocketMessage<>("PULLRESULT", String.valueOf(serverCount), null);
                                }
                                outs.writeObject(resultMessage);
                                buffer.notifyProduer();
                                break;
                            case "PEEK":
                                resultObj = (E) buffer.peek(null);
                                if (resultObj != null) {
                                    resultMessage = new SocketMessage<>("PEEKRESULT", String.valueOf(serverCount), resultObj);
                                } else {
                                    resultMessage = new SocketMessage<>("PEEKRESULT", String.valueOf(serverCount), null);
                                }
                                outs.writeObject(resultMessage);
                                break;
                            //ignore unrecognized messages
                            default:

                        }

                    } catch (IOException | ClassNotFoundException e) {
                        Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, e);
                        return;
                    }
                }
            }
        }

        private ServerEchoThread eth;
        private final int type;
        private boolean shutdown = false;
        private final SocketBuffer buffer;

        public boolean isShutdown() {
            return shutdown;
        }

        public void setShutdown(boolean shutdown) {
            this.shutdown = shutdown;
        }

        public SocketConnection(int type, SocketBuffer buffer) {

            this.type = type;
            this.buffer = buffer;
        }

        private void runServer() {
            System.out.println("Run server");
            try {
                ServerSocket serverSocket;

                BufferedReader input = null;
                BufferedWriter output = null;
                boolean connecting = false;
                serverSocket = new ServerSocket(port);
                while (!shutdown) {
                    try {

                        Socket connection = serverSocket.accept();
                        eth = new ServerEchoThread(connection, buffer);
                        eth.start();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        public void noticeClientConnetor() {
            synchronized (this) {
                notifyAll();
            }
        }

        private void runClient() {

            Socket socket = null;
            while (!shutdown) {

                SocketMessage<E> cmdobj = MessageQueue.poll();

                if (cmdobj != null) {
                    boolean reset = false;
                    while (true) {
                        try {
                            if (reset) {
                                socket.close();
                                socket = null;
                            }
                            if (socket == null) {

                                socket = new Socket(server, port);
                            }
                            ObjectOutputStream ous = new ObjectOutputStream(socket.getOutputStream());
                            ObjectInputStream ins = new ObjectInputStream(socket.getInputStream());
                            //send object with message
                            System.out.println("Client sending message" + cmdobj.getPrimaryMessage());
                            ous.writeObject(cmdobj);

                            //wait for reply
                            SocketMessage result = (SocketMessage) ins.readObject();
                            System.out.println("Client receive message " + result.getPrimaryMessage() + " = " + result.getObj());
                            //deal with reply
                            if (result.getPrimaryMessage().equals("POLLRESULT")) {
                                if (result.getObj() != null) {

                                    syncCount(Integer.parseInt(result.getsecondaryMessage()));
                                    StorageQueue.add((E) result.getObj());
                                    clientCount++;
                                    buffer.notifyConsumer();
                                    break;
                                }
                            } else if (result.getPrimaryMessage().equals("PEEKRESULT")) {
                                if (result.getObj() != null) {

                                    syncCount(Integer.parseInt(result.getsecondaryMessage()));
                                    StorageQueue.add((E) result.getObj());
                                    clientCount++;
                                    buffer.notifyConsumer();
                                    break;
                                }
                                break;
                            } else if (result.getPrimaryMessage().equals("PUSHRESULT")) {

                                if (result.getObj() != null && (Boolean) result.getObj()) {
                                    syncCount(Integer.parseInt(result.getsecondaryMessage()));
                                    pushMessageinQUeue--;
                                    break;
                                }
                            } else {
                                break;
                            }
                        } catch (IOException ex) {
                            try {
                                System.out.println("Exception when Socket Client tried to connect to Socket Server!");
                                reset = true;

                                Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, ex);
                                wait(500);
                            } catch (InterruptedException ex1) {
                                Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SocketBuffer.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
            }
        }

        @Override
        public void run() {
            if (type == SERVER) {
                System.out.println("starting server");
                runServer();

            } else {
                System.out.println("starting client");
                runClient();
            }
        }

    }

    private SocketConnection ConnectionThread;
    private int socketType;

    private Thread connector;
    public static int DEFAULT_PORT = 60001;

    private String server;
    private int port;

    private int pushMessageinQUeue = 0;
    private int serverCount = 0;
    private int clientCount = 0;
    private int size = 20;

    private synchronized void syncCount(int n) {
        serverCount = n;

    }

    //BufferQueue for client buffer
    private final Queue<SocketMessage<E>> MessageQueue = new LinkedList();
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
        ConnectionThread = new SocketConnection(socketType, this);
        ConnectionThread.start();
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
        register(callerKey, PRODUCER);
        if (this.serverCount + pushMessageinQUeue < size || size == 0) {

            if (socketType == CLIENT) {
                MessageQueue.add(new SocketMessage<>("PUSH", null, (E) obj));
                ConnectionThread.noticeClientConnetor();
                pushMessageinQUeue++;
                return true;
            } else {
                StorageQueue.add((E) obj);
                serverCount++;
                notifyConsumer();
                System.out.println("Pushed " + obj.toString());
                return true;
            }

        }
        return false;
    }

    @Override
    public synchronized E poll(Object callerKey) {
        register(callerKey, CONSUMER);
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
                ConnectionThread.noticeClientConnetor();
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public synchronized E peek(Object callerKey) {
        register(callerKey, CONSUMER);
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
                ConnectionThread.noticeClientConnetor();
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
