/*
 * The MIT License
 *
 * Copyright 2016 yl9.
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
package com.liangyibo.jpipe;

/**
 *
 * @author yl9
 */
public class DummyPlan {
    /*
    The plan of this branch is to build a Java Pipeline implementation that is able to do the following work:
    1. can do straight stream pipeline
    1.1Synchronised String Streaming
        Interface Work class
        JPipe.reg("work1", SomeWorker1.class);
        JPipe.pipeline("stdin | work1 | stdout");
    or  
        InputWork<String> stdin=JPipe.StdIn;
        Pipeline p=JPipe.createPipe()
             .append(stdin)
             .append(work1)
             .append(stdout)
             .build();
        p.pipeline();
    or 
        InputWork<String> stdin=JPipe.StdIn;
        Pipeline p=JPipe.createPipe()
             .append(stdin)
             .append("work1")
             .append(stdout)
             .build();
        p.pipeline();
    or
        InputWork<String> stdin=JPipe.StdIn;
        Pipeline p=JPipe.createPipe()
             .append(stdin)
             .append(SomeWorker1.class)
             .append(stdout)
             .build();
        p.pipeline();
    
    1.2 Object Streaming
        Worker<Obj1, Obj2> work1=new someWorker1();
        Worker<Obj2, Obj3> work2=new someWorker2();
        Worker<Obj3, Obj4> work3=new someWorker2();
        Pipeline p=JPipe.createPipe()
             .append(work1)
             .append(work2)
             .append(work3)
             .build();
        JPipe.pipeline(p);
    
    2. allow buffering for asynchroinsed pipeline
        JPipe.reg("work1", SomeWorker1.class);
        JPipe.asyncPipelining("stdin > buf1 | work1 | stdout");
    or
        AsyncPipeline p=JPipe.createAsyncPipe()
            .append(JPipe.StdIn)
            .addBuffer(String.class)
            .append(work1)
            .append(JPipe.StdOut)
            .build();
    2.1 allow buffering with specified size
    
        JPipe.reg("work1", SomeWorker1.class);
        JPipe.asyncPipelining("stdin > buf1(10) | work1 | stdout");
    or 
        AsyncPipeline p=JPipe.createAsycPipe()
            .append(JPipe.StdIn)
            .addBuffer(String.class, 10)
            .append(SomeWorker1.class)
            .append(JPipe.StdOut)
            .build();
    
    3 allow not only Task Parallelism, but data parallelism
        3.1 Regular pipe with multiple worker in same pipe section
            JPipe.reg("work1", SomeWorker1.class);
            JPipe.asyncPipelining("stdin > buf1(10) | work1*10 | stdout");
        or
            AsyncPipeline p=JPipe.createPipe()
                .append(JPipe.StdIn)
                .addBuffer(String.class, 10)
                .append(SomeWorker1.class,10)
                .append(JPipe.StdOut)
                .build();
    
        3.2 Task Parallelism, construct complex pipeline
            such as 
                Worker A,B,C,D,E
                A | B 
                B | C
                B | D
                C | E
                D | A
                where the flow of the pipeline is not mono-dirictional, and may be conditional
            3.2.1 Non-conditional, multi-dirictional
            e.g.
            A | B | C
            C | A or D (Round Robin, sequentially)
            D | E
            JPipe.reg("A",WorkerA.class);
            JPipe.reg("B",WorkerB.class);
            JPipe.reg("C",WorkerC.class);
            JPipe.reg("D",WorkerD.class);
            JPipe.reg("E",WorkerE.class);
            JPipe.asyncPipelining("A | B | C;" +
                                  "C | A,D ;" +
                                  "D | E");
            Or
            AsyncPipeline p1=JPipe.createPipe()
                .append(WorkerA.class)
                .append(WorkerB.class)
                .append(WorkerC.class);
            AsyncPipeline p2=JPipe.createPipe()
                .append(WorkerC.class)
                .append({WorkerA.class, WorkerD.class});
            AsyncPipeline p3=JPipe.createPipe()
                .append(WorderD.class)
                .append(WorkerE.class);
            JPipe.buildFrom({p1,p2,p3});
            // In above case, although A and C appear in both p1 and p2, 
            the worker thread of A and C should be created only once.
            
            Another 
            e.g.
            A | B*10 | C,D
            JPipe.reg("A",WorkerA.class);
            JPipe.reg("B",WorkerB.class);
            JPipe.reg("C",WorkerC.class);
            JPipe.reg("D",WorkerD.class);
            JPipe.asyncPipelining("A | B*10 | A,D");
            or
            AsyncPipeline p1=JPipe.createPipe()
                .append(WorkerA.class)
                .append(WorkerB.class,10)
                .append({WorkerA.class, WorkerD.class});
            
        3.2.2 Conditional flow direction
        e.g.1
            A | B*10 | C if cond1
            A | B*10 | D if cond2
            A | B*10 | E otherwise
            JPipe.asyncPipelining( "A | B*10;"+
                                   "B ? cond1 | C"+
                                   "B ? cond2 | D"+
                                   "B ? else | E");
        
    4. Remote pipeline, which uses socket streaming, either in byte or object
        e.g
        On Machine A
        stdin | uppercase | Machine B:8080
        On Machine B
        from tcp port 8080 | stdout
        
        On Machine A
        JPipe.reg("uppercase", toUpperWorker.class);
        JPipe.reg("B", new Host("B",8080));
        JPipe.pipeline("stdin | uppercase | B");
        On Machine B
        JPipe.pipeline("8080 | stdout");
    
    */
}
