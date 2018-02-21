There are 4 types of messages that certain process can receive:
1. online report
2. unicast
3. casual multicast
4. total multicast

The format of the received utf string will be:
1. 'o ' + senderPid
2. 'u ' + senderPid + ' ' + msg
3. 'c ' + senderPid + ' ' + V + ' ' +  msg
4. if it is message content:
      'tm' + senderPid + ' ' + msg
   if it is message order:
      'to' + senderPid + ' ' + S
