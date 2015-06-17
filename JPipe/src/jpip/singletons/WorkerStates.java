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
package jpip.singletons;

import java.util.HashMap;

/**
 *
 * @author yl9
 */
public class WorkerStates {

    private static WorkerStates instance = null;
    public static Integer BLOCKED_POLLING = 1001;
    public static Integer BLOCKED_PUSHING = 1002;
    public static Integer POST_SUCCESS = 1003;
    public static Integer POST_FAIL = 1004;
    public static Integer WORKING = 1005;
    public static Integer INITIAL = 1006;
    public static Integer WAITING_SUBTASK = 1007;

    private static final HashMap<Integer, String> states = new HashMap<>();

    private static boolean isInitialised = false;

    private WorkerStates() {

    }

    public static WorkerStates getInstance() {
        if (instance == null) {
            instance = new WorkerStates();
        }
        return instance;
    }
    
    public static void addUserState(Integer state, String stateName){
        states.put(state, stateName);
    }
    
    public static String state(Integer state) {
        if (!isInitialised) {
            states.put(WORKING, "WORKING");
            states.put(BLOCKED_PUSHING, "BLOCKED_PUSHING");
            states.put(BLOCKED_POLLING, "BLOCKED_POLLING");
            states.put(POST_SUCCESS, "POST_SUCCESS");
            states.put(POST_FAIL, "POST_FAIL");
            states.put(WAITING_SUBTASK, "WAITING_SUBTASK");
            states.put(INITIAL, "INITIAL");
            isInitialised=true;
        }
        String result = states.get(state);
        if (result != null) {
            return result;
        }
        return "UNKOWN_STATE";
    }

}
