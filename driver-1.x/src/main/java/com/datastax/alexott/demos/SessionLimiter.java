package com.datastax.alexott.demo;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.MoreExecutors;

public class SessionLimiter {
	final Session session;
	final Semaphore semaphore;
	final int limit;
	Executor executor = MoreExecutors.directExecutor();

	static int getNumberOfRequests(final Session session) {
		PoolingOptions poolingOptions = session.getCluster().getConfiguration().getPoolingOptions();
		int requestsPerConnection = poolingOptions.getMaxRequestsPerConnection(HostDistance.LOCAL);
		int maxConnections = poolingOptions.getCoreConnectionsPerHost(HostDistance.LOCAL);

		return (int) (requestsPerConnection * maxConnections * 0.95);
	}

	public SessionLimiter(final Session session) {
		this(session, getNumberOfRequests(session));
	}

	public SessionLimiter(final Session session, int limit) {
		System.out.println("Initializing SessionLimiter with limit=" + limit);
		this.session = session;
		this.limit = limit;
		semaphore = new Semaphore(limit);
	}

	public ResultSetFuture executeAsync(Statement statement) throws InterruptedException {
		semaphore.acquire();
		ResultSetFuture future = session.executeAsync(statement);
		future.addListener(() -> semaphore.release(), executor);
		return future;
	}

	public ResultSetFuture executeAsync(String query) throws InterruptedException {
		semaphore.acquire();
		ResultSetFuture future = session.executeAsync(query);
		future.addListener(() -> semaphore.release(), executor);
		return future;
	}

	public ResultSetFuture executeAsync(String query, Object... values) throws InterruptedException {
		semaphore.acquire();
		ResultSetFuture future = session.executeAsync(query, values);
		future.addListener(() -> semaphore.release(), executor);
		return future;
	}

	public ResultSetFuture executeAsync(String query, Map<String, Object> values) throws InterruptedException {
		semaphore.acquire();
		ResultSetFuture future = session.executeAsync(query, values);
		future.addListener(() -> semaphore.release(), executor);
		return future;
	}

	public void waitForFinish() throws InterruptedException {
		while (semaphore.availablePermits() != limit) {
			Thread.sleep(200);
		}
	}
}
