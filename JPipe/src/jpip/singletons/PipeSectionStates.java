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
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author yl9
 */
public class PipeSectionStates {

    private static final PipeSectionStates instance = new PipeSectionStates();
    public static Integer PAUSING = 2001;
    public static Integer RESTING = 2002;
    public static Integer WORKING = 2005;
    public static Integer INITIAL = 2006;
    private static final HashMap<Integer, String> states = new HashMap<>();
    private static boolean isInitialised = false;

    private PipeSectionStates() {
    }

    public static PipeSectionStates getInstance() {

        return instance;
    }

    public static void addUserState(Integer state, String stateName) {
        states.put(state, stateName);
    }

    public static String state(Integer state) {

        if (!isInitialised) {
            states.put(WORKING, "WORKING");
            states.put(RESTING, "RESTING");
            states.put(PAUSING, "PAUSING");
            states.put(INITIAL, "INITIAL");
            isInitialised = true;
        }
        String result = states.get(state);
        if (result != null) {
            return result;
        }
        return "UNKOWN_STATE";
    }

}
