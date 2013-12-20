/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner;

import com.facebook.presto.spi.ColumnHandle;
import com.google.common.base.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class TestingColumnHandle
        implements ColumnHandle
{
    private final Symbol symbol;

    public TestingColumnHandle(Symbol symbol)
    {
        this.symbol = checkNotNull(symbol, "symbol is null");
    }

    public Symbol getSymbol()
    {
        return symbol;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(symbol);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TestingColumnHandle other = (TestingColumnHandle) obj;
        return Objects.equal(this.symbol, other.symbol);
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("symbol", symbol)
                .toString();
    }
}