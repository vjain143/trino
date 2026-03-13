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
package io.trino.plugin.eventlistener.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.airlift.json.ObjectMapperProvider;
import io.airlift.units.DataSize;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Utility class to handle JSON serialization of query events with field exclusion and truncation.
 * <p>
 * Features:
 * - Exclude fields completely (replaced with null in output)
 * - Truncate specific fields to a maximum size limit
 */
public class QueryEventFieldFilter
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapperProvider().get();

    private final Set<String> excludedFields;
    private final Set<String> truncatedFields;
    private final long maxFieldSizeBytes;
    private final long truncationSizeLimitBytes;

    public QueryEventFieldFilter(
            Set<String> excludedFields,
            DataSize maxFieldSize,
            Set<String> truncatedFields,
            DataSize truncationSizeLimit)
    {
        this.excludedFields = requireNonNull(excludedFields, "excludedFields is null");
        this.maxFieldSizeBytes = requireNonNull(maxFieldSize, "maxFieldSize is null").toBytes();
        this.truncatedFields = requireNonNull(truncatedFields, "truncatedFields is null");
        this.truncationSizeLimitBytes = requireNonNull(truncationSizeLimit, "truncationSizeLimit is null").toBytes();
    }

    /**
     * Apply field filtering (truncation and exclusion) to a JSON string.
     * Optimized to avoid creating multiple intermediate String objects.
     */
    public String applyFiltering(String json)
    {
        if (json.isEmpty() || (excludedFields.isEmpty() && truncatedFields.isEmpty())) {
            return json;
        }

        JsonNode root;
        try {
            root = OBJECT_MAPPER.readTree(json);
        }
        catch (JsonProcessingException e) {
            return json;
        }

        filterNode(root);

        try {
            return OBJECT_MAPPER.writeValueAsString(root);
        }
        catch (JsonProcessingException e) {
            return json;
        }
    }

    private void filterNode(JsonNode node)
    {
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode childNode : arrayNode) {
                filterNode(childNode);
            }
            return;
        }

        if (!(node instanceof ObjectNode objectNode)) {
            return;
        }

        long effectiveLimit = Math.min(maxFieldSizeBytes, truncationSizeLimitBytes);
        for (Map.Entry<String, JsonNode> field : objectNode.properties()) {
            String fieldName = field.getKey();
            JsonNode value = field.getValue();

            if (excludedFields.contains(fieldName)) {
                objectNode.putNull(fieldName);
                continue;
            }

            if (truncatedFields.contains(fieldName) && value.isTextual()) {
                String fieldValue = value.asText();
                if (fieldValue.getBytes(StandardCharsets.UTF_8).length > effectiveLimit) {
                    objectNode.put(fieldName, truncateString(fieldValue, effectiveLimit));
                }
            }

            filterNode(value);
        }
    }

    /**
     * Truncate a string to accommodate max bytes while handling UTF-8 properly.
     */
    public static String truncateString(String value, long maxBytes)
    {
        if (value == null || maxBytes <= 0) {
            return value;
        }

        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return value;
        }

        // Truncate string to fit within maxBytes
        String truncated = new String(bytes, 0, (int) Math.min(maxBytes, bytes.length), StandardCharsets.UTF_8);

        // Remove any incomplete characters at the end
        while (truncated.getBytes(StandardCharsets.UTF_8).length > maxBytes && truncated.length() > 0) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }

        return truncated + "...[TRUNCATED]";
    }
}
