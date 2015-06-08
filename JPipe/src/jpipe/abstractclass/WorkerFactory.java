/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpipe.abstractclass;

import jpipe.interfaceclass.IWorkerFactory;

/**
 *
 * @author yl9
 * @param <E>
 */
public abstract class WorkerFactory<E> implements IWorkerFactory{
    public abstract E create();
}
