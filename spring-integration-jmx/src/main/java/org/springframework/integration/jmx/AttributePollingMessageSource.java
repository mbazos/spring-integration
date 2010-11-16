/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.jmx;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.endpoint.AbstractMessageSource;
import org.springframework.jmx.support.ObjectNameManager;
import org.springframework.util.Assert;

/**
 * A {@link MessageSource} implementation that retrieves the current
 * value of a JMX attribute each time {@link #receive()} is invoked.
 * 
 * @author Mark Fisher
 * @since 2.0
 */
public class AttributePollingMessageSource extends AbstractMessageSource<Object> {

	private volatile ObjectName objectName;

	private volatile String attributeName;

	private volatile MBeanServer server;


	/**
	 * Provide the MBeanServer where the JMX MBean has been registered.
	 */
	public void setServer(MBeanServer server) {
		this.server = server;
	}

	/**
	 * Specify the String value of the JMX MBean's {@link ObjectName}.  
	 */
	public void setObjectName(String objectName) {
		try {
			this.objectName = ObjectNameManager.getInstance(objectName);
		}
		catch (MalformedObjectNameException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Specify the name of the attribute to be retrieved.
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	/**
	 * Retrieves the JMX attribute value.
	 */
	@Override
	protected Object doReceive() {
		Assert.notNull(this.server, "MBeanServer is required");
		Assert.notNull(this.objectName, "object name is required");
		Assert.notNull(this.attributeName, "attribute name is required");
		try {
			return this.server.getAttribute(this.objectName, this.attributeName);
		}
		catch (Exception e) {
			throw new MessagingException("failed to retrieve JMX attribute '"
					+ this.attributeName + "' on MBean [" + this.objectName + "]", e);
		}
	}

}
