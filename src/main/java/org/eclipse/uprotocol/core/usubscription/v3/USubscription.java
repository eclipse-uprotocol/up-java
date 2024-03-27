package org.eclipse.uprotocol.core.usubscription.v3;

import org.eclipse.uprotocol.v1.UStatus;

/* The following is the uSubscription API declared as an interface so it could be easily 
 * used  
 */
public interface USubscription {
    FetchSubscriptionsResponse fetchSubscriptions(FetchSubscriptionsRequest request);

    UStatus registerForNotifications(NotificationsRequest request);

    UStatus unregisterForNotifications(NotificationsRequest request);
}
