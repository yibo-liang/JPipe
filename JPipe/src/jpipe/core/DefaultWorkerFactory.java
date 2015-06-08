/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpipe.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpipe.abstractclass.WorkerFactory;
import jpipe.interfaceclass.IWorker;

/**
 * This is a default worker factory that will create new worker instance for a 
 * parallel pipes section when a ParallelPipe object is in dynamic mode that adjusts
 * throughput of its pipes. 
 *
 * @author yl9
 * @param <E>
 */
public class DefaultWorkerFactory<E extends IWorker> extends WorkerFactory {

    private final Class workerclass;
    private Class[] paramClasses;
    private Object[] params;

    public void setParamClasses(Class[] paramClasses) {
        this.paramClasses = paramClasses;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public DefaultWorkerFactory(Class Workerclass) {
        this.workerclass = Workerclass;
        this.paramClasses = null;
    }

    public DefaultWorkerFactory(Class Workerclass, Class[] paramsClasses) {
        this.workerclass = Workerclass;
        this.paramClasses = paramsClasses;
    }

    @Override
    public E create() {
        E result = null;
        try {
            Constructor ctr;
            if (paramClasses == null) {
                ctr = workerclass.getConstructor();
                result = (E) ctr.newInstance();
            } else {
                ctr = workerclass.getConstructor(paramClasses);
                result = (E) ctr.newInstance(params);
            }
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(DefaultWorkerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

}
