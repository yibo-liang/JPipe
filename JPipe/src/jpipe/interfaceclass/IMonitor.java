/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpipe.interfaceclass;

/**
 *
 * @author yl9
 */
public interface IMonitor<E> {
    public abstract boolean InspectIn(E obj);
    public abstract boolean InspectOut(E obj);
    
}
