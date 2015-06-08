/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpip.singletons;

/**
 *
 * @author yl9
 */
public class JPipeGlobal {

    private static JPipeGlobal instance = new JPipeGlobal();

    private JPipeGlobal() {

    }

    public static JPipeGlobal getInstance() {
        return instance;
    }

    public static final int SUCCESSFUL = 1;
    public static final int FAIL = 0;
    
    
}
