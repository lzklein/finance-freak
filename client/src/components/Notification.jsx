import React from 'react'
import Alert from './Alert';
import Inbox from './Inbox';

// popout window of recent alerts
// maybe 3-5 most recent alerts
// see all button at bottom nav to inbox
const Notification = () => {
  return (
    <div>
        <Alert/>
        <Inbox/>
    </div>
  )
}

export default Notification