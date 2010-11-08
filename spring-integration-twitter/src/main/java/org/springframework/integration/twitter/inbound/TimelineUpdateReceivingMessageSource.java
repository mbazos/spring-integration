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
package org.springframework.integration.twitter.inbound;

import java.util.List;

import org.springframework.integration.MessagingException;
import org.springframework.integration.twitter.core.Tweet;
import org.springframework.integration.twitter.core.TwitterOperations;

import twitter4j.Paging;


/**
 * This {@link org.springframework.integration.core.MessageSource} lets Spring Integration consume a given account's timeline
 * as messages. It has support for dynamic throttling of API requests.
 *
 * @author Josh Long
 * @author Oleg Zhurakousky
 * @since 2.0
 */
public class TimelineUpdateReceivingMessageSource extends AbstractTwitterMessageSource<Tweet> {
	
	public TimelineUpdateReceivingMessageSource(TwitterOperations twitter){
		super(twitter);
	}
	@Override
	 public String getComponentType() {
		return "twitter:inbound-update-channel-adapter";  
	}

	@Override
	Runnable getApiCallback() {
		Runnable apiCallback = new Runnable() {	
			public void run() {
				try {
					long sinceId = getMarkerId();
					if (tweets.size() <= prefetchThreshold){
						List<Tweet> tweets = !hasMarkedStatus() 
								? twitter.getFriendsTimeline()  
								: twitter.getFriendsTimeline(sinceId);
						forwardAll(tweets);
					}	
				} catch (Exception e) {
					if (e instanceof RuntimeException){
						throw (RuntimeException)e;
					}
					else {
						throw new MessagingException("Failed to poll for Twitter mentions updates", e);
					}
				}
			}
		};
		return apiCallback;
	}
}