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
package io.trino.plugin.opa.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.airlift.json.JsonCodec;
import io.airlift.json.JsonCodecFactory;
import io.trino.spi.security.Identity;

import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static java.util.Objects.requireNonNull;

public record TrinoIdentity(
        String user,
        Set<String> groups,
        @JsonInclude(NON_EMPTY) Map<String, Object> jwtClaims)
{
    private static final String TOKEN_CLAIMS = "internal$token$internal.claims";
    private static final JsonCodec<Map<String, Object>> MAP_JSON_CODEC = new JsonCodecFactory().mapJsonCodec(String.class, Object.class);

    public static TrinoIdentity fromTrinoIdentity(Identity identity)
    {
        return new TrinoIdentity(
                identity.getUser(),
                identity.getGroups(),
                readClaims(identity));
    }

    public TrinoIdentity
    {
        requireNonNull(user, "user is null");
        groups = ImmutableSet.copyOf(requireNonNull(groups, "groups is null"));
        jwtClaims = ImmutableMap.copyOf(requireNonNull(jwtClaims, "jwtClaims is null"));
    }

    private static Map<String, Object> readClaims(Identity identity)
    {
        String claims = identity.getExtraCredentials().get(TOKEN_CLAIMS);
        if (claims == null) {
            return ImmutableMap.of();
        }
        try {
            return MAP_JSON_CODEC.fromJson(claims);
        }
        catch (RuntimeException e) {
            throw new IllegalArgumentException("Unable to deserialize JWT claims from identity", e);
        }
    }
}
