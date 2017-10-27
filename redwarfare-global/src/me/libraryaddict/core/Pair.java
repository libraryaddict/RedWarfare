
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.libraryaddict.core;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.google.gson.Gson;

public class Pair<L, R> implements Map.Entry<L, R>, Serializable
{

    /** Serialization version */
    private static final long serialVersionUID = 4954918890077093841L;

    public static Pair fromString(String string)
    {
        return new Gson().fromJson(string, Pair.class);
    }

    public static <L, R> Pair<L, R> of(final L left, final R right)
    {
        return new Pair<L, R>(left, right);
    }

    /** Left object */
    private L left;

    /** Right object */
    private R right;

    /**
     * Create a new pair instance of two nulls.
     */
    public Pair()
    {
    }

    /**
     * Create a new pair instance.
     *
     * @param left
     *            the left value, may be null
     * @param right
     *            the right value, may be null
     */
    public Pair(final L left, final R right)
    {
        this.left = left;
        this.right = right;
    }

    public Pair<L, R> clone()
    {
        return Pair.of(left, right);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (obj instanceof Map.Entry<?, ?>)
        {
            final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;

            return Objects.equals(getKey(), other.getKey()) && Objects.equals(getValue(), other.getValue());
        }

        return false;
    }

    public final L getKey()
    {
        return left;
    }

    public R getValue()
    {
        return right;
    }

    public int hashCode()
    {
        return (getKey() == null ? 0 : getKey().hashCode()) ^ (getValue() == null ? 0 : getValue().hashCode());
    }

    public L setKey(final L key)
    {
        final L result = getKey();
        left = key;

        return result;
    }

    public R setValue(final R value)
    {
        final R result = getValue();
        right = value;

        return result;
    }

    public String toString()
    {
        return new Gson().toJson(this);
    }

    public String toString(final String format)
    {
        return String.format(format, getKey(), getValue());
    }

}
