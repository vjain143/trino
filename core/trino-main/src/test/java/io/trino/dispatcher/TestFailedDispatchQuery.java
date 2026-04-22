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
package io.trino.dispatcher;

import com.google.common.util.concurrent.SettableFuture;
import io.trino.Session;
import io.trino.execution.QueryState;
import io.trino.spi.NodeVersion;
import io.trino.spi.QueryId;
import io.trino.spi.security.AccessDeniedException;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Optional;

import static com.google.common.util.concurrent.Futures.getDone;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static io.trino.testing.TestingSession.testSessionBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class TestFailedDispatchQuery
{
    private static final URI QUERY_URI = URI.create("fake://query");
    private static final NodeVersion NODE_VERSION = new NodeVersion("test-version");

    @Test
    void testApprovalRequiredState()
    {
        FailedDispatchQuery query = newFailedDispatchQuery(new AccessDeniedException("Approval Required"));

        assertThat(query.getState()).isEqualTo(QueryState.APPROVAL_IN_FLIGHT);
        assertThat(query.getBasicQueryInfo().getState()).isEqualTo(QueryState.APPROVAL_IN_FLIGHT);
        assertThat(listenerState(query)).isEqualTo(QueryState.APPROVAL_IN_FLIGHT);
    }

    @Test
    void testRegularAccessDeniedState()
    {
        FailedDispatchQuery query = newFailedDispatchQuery(new AccessDeniedException("Cannot execute query"));

        assertThat(query.getState()).isEqualTo(QueryState.FAILED);
        assertThat(query.getBasicQueryInfo().getState()).isEqualTo(QueryState.FAILED);
        assertThat(listenerState(query)).isEqualTo(QueryState.FAILED);
    }

    private static FailedDispatchQuery newFailedDispatchQuery(Throwable cause)
    {
        Session session = testSessionBuilder()
                .setQueryId(new QueryId("20260422_000000_00000_test1"))
                .build();

        return new FailedDispatchQuery(
                session,
                "SELECT 1",
                Optional.empty(),
                QUERY_URI,
                Optional.empty(),
                cause,
                directExecutor(),
                NODE_VERSION);
    }

    private static QueryState listenerState(FailedDispatchQuery query)
    {
        SettableFuture<QueryState> stateFuture = SettableFuture.create();
        query.addStateChangeListener(stateFuture::set);
        return getDone(stateFuture);
    }
}
