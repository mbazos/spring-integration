/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.integration.config.xml;

import org.w3c.dom.Element;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.ConsumerEndpointFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.xml.DomUtils;

/**
 * Base class parser for elements that create Message Endpoints.
 * 
 * @author Mark Fisher
 */
public abstract class AbstractConsumerEndpointParser extends AbstractBeanDefinitionParser {

	protected static final String REF_ATTRIBUTE = "ref";

	protected static final String METHOD_ATTRIBUTE = "method";


	@Override
	protected boolean shouldGenerateId() {
		return false;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}

	/**
	 * Parse the MessageHandler.
	 */
	protected abstract BeanDefinitionBuilder parseHandler(Element element, ParserContext parserContext);

	protected String getInputChannelAttributeName() {
		return "input-channel";
	}

	@Override
	protected final AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder handlerBuilder = this.parseHandler(element, parserContext);
		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(handlerBuilder, element, "output-channel");
		AbstractBeanDefinition handlerBeanDefinition = handlerBuilder.getBeanDefinition();
		if (!element.hasAttribute(this.getInputChannelAttributeName())) {
			return handlerBeanDefinition;
		}
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(ConsumerEndpointFactoryBean.class);
		String handlerBeanName = BeanDefinitionReaderUtils.registerWithGeneratedName(handlerBeanDefinition, parserContext.getRegistry());
		builder.addConstructorArgReference(handlerBeanName);
		String inputChannelAttributeName = this.getInputChannelAttributeName();
		String inputChannelName = element.getAttribute(inputChannelAttributeName);
		Assert.hasText(inputChannelName, "the '" + inputChannelAttributeName + "' attribute is required");
		if (!parserContext.getRegistry().containsBeanDefinition(inputChannelName)) {
			BeanDefinitionBuilder channelDef = BeanDefinitionBuilder.genericBeanDefinition(DirectChannel.class);
			BeanDefinitionHolder holder = new BeanDefinitionHolder(channelDef.getBeanDefinition(), inputChannelName);
			BeanDefinitionReaderUtils.registerBeanDefinition(holder, parserContext.getRegistry());
		}
		builder.addPropertyValue("inputChannelName", inputChannelName);
		Element pollerElement = DomUtils.getChildElementByTagName(element, "poller");
		if (pollerElement != null) {
			IntegrationNamespaceUtils.configurePollerMetadata(pollerElement, builder, parserContext);
		}
		IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, element, "auto-startup");
		return builder.getBeanDefinition();
	}

}
