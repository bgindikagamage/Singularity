## Singularity Webhooks

Singularity provides for integration with third-party services via webhooks for three types of objects: requests, deploys, and tasks.

Webhooks are added via the API and can be one and only one of the three types.

### Adding a Webhook

In order to create a new Webhook, post the json for the [SingularityWebhook](reference/api.md#model-SingularityWebhook) to the [webhook endpoint](reference/api.md#post-apiwebhooks).

For example, create a file webhook.json (create your own requestb.in for testing!)

```json
{
    "id": "test-webhook",
    "uri": "http://requestb.in/1lw85s51",
    "type": "REQUEST"
}
```

```sh
curl -i -X POST -H "Content-Type: application/json" -d@webhook.json \
http://singularity/singularity/api/requests
```

This webhook will start to receive events anytime a [Request](reference/api.md#model-SingularityRequest) is created, modified, or deleted.

### Webhook Types

#### Request

Request webhooks are sent every time a request is created, deleted, or updated - as well as when it's state changes (it becomes paused or enters cooldown.)

Request webhooks are in the format of [SingularityRequestHistory](reference/api.md#-singularityrequesthistory) objects. 

#### Deploy

Deploy webhooks are sent when deploys are started and finished (fail or succeed.)

Deploy webhooks are in the format of [SingularityDeployUpdate](reference/api.md#model-SingularityDeployUpdate) objects.

#### Task

Task webhooks are sent when tasks are launched by Singularity, killed with by Singularity users, and on all task updates recieved from Mesos.

Task webhooks are in the format of [SingularityTaskHistoryUpdate](reference/api.md#model-SingularityTaskHistoryUpdate) objects.

### Webhook notes

- Webhooks are only considered successful if the response code to the webhook is between 200 and 299 inclusive.
- Webhooks will be delivered every 10 seconds (checkWebhooksEveryMillis) 
- Webhooks will be deleted if they fail to deliver and there are more than 50 in the queue (maxQueuedUpdatesPerWebhook)
- Webhooks will be deleted if they fail to deliver after 7 days (deleteUndeliverableWebhooksAfterHours)
- For debugging purposes, queued webhook updates can be retrieved from the [API](reference/api.md#get-apiwebhooksrequestwebhookid)