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
package jpipe.util;

/**
 *
 * @author yl9
 */
public class Triplet<A, B, C> {

    private final A first;
    private final B second;
    private final C third;

    public Triplet(A first, B second, C third) {
        super();
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public int hashCode() {
        int hashFirst = first != null ? first.hashCode() : 0;
        int hashSecond = second != null ? second.hashCode() : 0;
        int hashThird = third != null ? third.hashCode() : 0;
        return combineHash(hashFirst, combineHash(hashSecond, hashThird));
    }

    private int combineHash(int hash1, int hash2) {
        return hash1 * 31 + hash2;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Triplet) {
            Triplet otherPair = (Triplet) other;
            return ((this.first == otherPair.first
                    || (this.first != null && otherPair.first != null
                    && this.first.equals(otherPair.first)))
                    && (this.second == otherPair.second
                    || (this.second != null && otherPair.second != null
                    && this.second.equals(otherPair.second))));
        }

        return false;
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    public C getThird() {
        return third;
    }

}
