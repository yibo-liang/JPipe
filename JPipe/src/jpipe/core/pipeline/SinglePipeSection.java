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
 * THE SOFTWARE.3
 */
package jpipe.core.pipeline;

import jpipe.abstractclass.PipeSection;
import jpipe.abstractclass.worker.Worker;
import jpipe.interfaceclass.IPipeSection;

/**
 *
 * @author Yibo
 */
public class SinglePipeSection extends PipeSection
        implements Runnable {

    public SinglePipeSection(Worker worker) {
        this.setWorker(worker);
    }

    @Override
    public void run() {

        if (analyser != null) {
            analyser.BlockStart();
        }
        while (isRunning()) {
            //the worker may be awoken by follower consumer when resting a pushing job to output buffer
            // the laziness is set to 0 for pushing this for once, then the worker goes back lazy
            //set the laziness back, "People don't change!" -- by House
            this.setLaziness(9000);
            WorkStart();
            
            int result = this.getWorker().work();
            WorkFinish(result);
            
        }

    }

    @Override
    public void getNotifiedByOther() {
        this.resume();
        this.getWorker().getNotified();
        
    }

}
