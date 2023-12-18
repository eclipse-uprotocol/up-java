package org.eclipse.uprotocol.core.usubscription.v3;

import org.eclipse.uprotocol.v1.*;

public interface USubscription {
    
    // A consumer (application) calls this API to subscribe to a topic.
    // What is passed is the SubscriptionRequest message containing the topic, the
    // subscriber's name, and any Subscription Attributes. This API returns a
    // SubscriptionResponse message containing the status of the request along with
    // any event delivery configuration
    // required to consume the event. Calling this API also registers the subscriber
    // to received subscription change notifications if ever the subscription state
    // changes.
    SubscriptionResponse subscribe(SubscriptionRequest request);
  
    
    // The consumer no longer wishes to subscribe to a topic so it issues an
    // explicit unsubscribe request.
    UStatus unsubscribe(UnsubscribeRequest request);

    // Fetch a list of subscriptions
    FetchSubscriptionsResponse fetchSubscriptions(FetchSubscriptionsRequest request);


    //  API called by producers to register a topic. This API
    // informs the Subscription Service that to create the topic and it is ready to publish.
    UStatus createTopic(CreateTopicRequest request);

    // Request deprecation of a topic. Producers call this to inform the uSubscription
    // that it will no longer produce to said topic. The topic is flagged as deprcated
    // which
    UStatus deprecateTopic(DeprecateTopicRequest request);


    // Register to receive subscription change notifications that are published on the
    // 'up:/core.usubscription/3/subscriptions#Update'
    UStatus registerForNotifications(NotificationsRequest request);

    // Unregister for subscription change events
    UStatus unregisterForNotifications(NotificationsRequest request);

    // Fetch a list of subscribers that are currently subscribed to a given topic.
    FetchSubscribersResponse fetchSubscribers(FetchSubscribersRequest request);

    // Reset subscriptions to and from the uSubscription Service. 
    // This API is used between uSubscription services in order to flush and 
    // reestablish subscriptions between devices. A uSubscription service might 
    // ned to call this API if its database is flushed or corrupted (ex. factory
    // reset).
    // **__NOTE:__** This is a private API only for uSubscription services,
    // uEs can call Unsubscribe() to flush their own subscriptions.
    UStatus reset(ResetRequest request);
}
