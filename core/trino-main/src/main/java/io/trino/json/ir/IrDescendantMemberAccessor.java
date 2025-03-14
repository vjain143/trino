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
package io.trino.json.ir;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.trino.spi.type.Type;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class IrDescendantMemberAccessor
        extends IrAccessor
{
    private final String key;

    @JsonCreator
    public IrDescendantMemberAccessor(@JsonProperty("base") IrPathNode base, @JsonProperty("key") String key, @JsonProperty("type") Optional<Type> type)
    {
        super(base, type);
        this.key = requireNonNull(key, "key is null");
    }

    @Override
    protected <R, C> R accept(IrJsonPathVisitor<R, C> visitor, C context)
    {
        return visitor.visitIrDescendantMemberAccessor(this, context);
    }

    @JsonProperty
    public String getKey()
    {
        return key;
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
        IrDescendantMemberAccessor other = (IrDescendantMemberAccessor) obj;
        return Objects.equals(this.base, other.base) && Objects.equals(this.key, other.key);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(base, key);
    }
}
