import fetch from 'isomorphic-fetch';

import buildApiAction from './base';

export const FetchAction = buildApiAction('FETCH_WEBHOOKS', '/webhooks');