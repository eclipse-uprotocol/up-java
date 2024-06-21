package org.eclipse.uprotocol.core.usubscription.v3;

import org.eclipse.uprotocol.v1.UStatus;
import org.eclipse.uprotocol.core.usubscription.v3.*;

/* The following is the uSubscription API declared as an interface so it could be easily 
 * used  
 */
public interface USubscription {

    
    /**
     * A consumer (application) calls this API to subscribe to a topic.
     * What is passed is the SubscriptionRequest message containing the topic, the
     * subscriber's name, and any Subscription Attributes. This API returns a
     * SubscriptionResponse message containing the status of the request along with
     * any event delivery configuration
     * required to consume the event. Calling this API also registers the subscriber
     * to received subscription change notifications if ever the subscription state
     * changes.
     * @param request the request containing the topic, subscriber name, and subscription attributes
     * @return the response containing the status of the request and any event delivery configuration
     */
    SubscriptionResponse subscribe(SubscriptionRequest request);


    // The consumer no longer wishes to subscribe to a topic so it issues an
    // explicit unsubscribe request.
    UStatus unsubscribe(UnsubscribeRequest request);


    /**
     * Fetch a list of subscribers that are currently subscribed to a given topic.
     * @param request the request containing the topic that we want the list of subscribers for
     * @return the list of subscribers for the given topic
     */
    FetchSubscribersResponse fetchSubscribers(FetchSubscribersRequest request);


    /**
     * Reset subscriptions to and from the uSubscription Service.
     * This API is used between uSubscription services in order to flush and 
     * reestablish subscriptions between devices. A uSubscription service might 
     * ned to call this API if its database is flushed or corrupted (ex. factory
     * reset).
     * **__NOTE:__** This is a private API only for uSubscription services,
     * uEs can call Unsubscribe() to flush their own subscriptions.
     * 
     * @param request the request containing the reset information
     * @return the status of the reset request
     */
    UStatus reset(ResetRequest request);


    /**
     * Fetch a list of subscriptions for a given subscriber.
     * @param request the request containing the subscriber
     * @return the list of subscriptions for the subscriber
     */
    FetchSubscriptionsResponse fetchSubscriptions(FetchSubscriptionsRequest request);


    /**
     * Register for subscription change notifications for a given topic
     * @param request the request containing the topic
     * @return the status of the request
     */
    UStatus registerForNotifications(NotificationsRequest request);

    /**
     * Unregister for subscription change notifications for a given topic
     * @param request the request containing the topic
     * @return the status from unregistering for a subscription change notification
     */
    UStatus unregisterForNotifications(NotificationsRequest request);
}
