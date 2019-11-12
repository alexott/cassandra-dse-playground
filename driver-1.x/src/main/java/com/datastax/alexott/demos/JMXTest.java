package com.datastax.alexott.demos;

import java.util.Set;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.cassandra.metrics.CassandraMetricsRegistry;

public class JMXTest {

	public static void main(String[] args) throws Exception {
		JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://[127.0.0.1]:7199/jmxrmi");
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		Set<ObjectInstance> objs = mbsc.queryMBeans(ObjectName
				.getInstance("org.apache.cassandra.metrics:type=ClientRequest,scope=Read-ALL,name=TotalLatency"), null);
		for (ObjectInstance obj : objs) {
			Object proxy = JMX.newMBeanProxy(mbsc, obj.getObjectName(), CassandraMetricsRegistry.JmxCounterMBean.class);
			if (proxy instanceof CassandraMetricsRegistry.JmxCounterMBean) {
				System.out.println("TotalLatency = " + ((CassandraMetricsRegistry.JmxCounterMBean) proxy).getCount());
			}
		}
		jmxc.close();
	}

}

/*
 * Set<ObjectName> names = mbsc.queryNames(null, null); for (ObjectName name :
 * names) { System.out.println("\tObjectName = " + name); }
 */
