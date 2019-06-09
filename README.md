# Assignment2 ATD192
### Implementing whatsapp clone using Akka and Java

Submission by group #93427:
- Guy Ben-aharon 204075931
- Gennady Gramovich 314336751

## <b>Actor model design</b>
### 2 Clients, 2 Groups example:

![alt text](https://i.imgur.com/cr9NHzL.png)

Our model contains 3 Main actors:
- User actor
- ManagingServer actor
- Group actor (Created by ManagingServer actor)


## Client --> User behaviour
Input received at client terminal will be parsed and matching message will be passed to user to act.
## User <--> Server behaviour
User interactes with server to connect/disconnect/get another user refrence/forward message to group.
## User <--> Server <--> Group behaviour
Each group-related message received in server will be forwarded to the matching group (after server validating message)
and be returned to the matching user/s.

# Messages
## Client -> User messages (located at client/ClientMessages.java)
### ClientConnectMessage
  Client tells source user to initiate connect to server
### ClientDisconnectMessage
  Client tells source user to initiate disconnect from server
### ClientSendText
  Client tells source user to send message to target user containing text message
### ClientSendFile
  Client tells source user to send message to target user containing file path
### ClientGroupCreate
  Client tells source user to send message to server for starting a group
### ClientGroupLeave
  Client tells source user to send message to server for leaving a group
### ClientGroupText
  Client tells source user to send message to server for broadcasting text to group
### ClientGroupFile
  Client tells source user to send message to server for broadcasting file to group
### ClientGroupInvite
  Client tells source user to send invited user invite request (UpdateTargetAboutInvite message)
### ClientInviteAccepted / ClientInviteDeclined
  Client updates invited user actor about "Yes/No" input (Invited user initates UserInviteAccept message back)
### ClientGroupUserRem
  Client tells source user to initate server request to remove target user
### ClientGroupUserMute
  Client tells source user to initate mute target user
### ClientGroupUserUnmute
  Client tells source user to initate unmute target user
### ClientGroupAddCoAdmin
  Client tells source user to initate coadmin target user
### ClientGroupRemCoAdmin
  Client tells source user to initate remove target user from being a coadmin

## User -> User messages (located at client/ClientMessages.java)
### UserTextMessage
  Sending message target formatted text message
### UserFileMessage
  Sending message target file (using file path)
### UserLogMessage
  Sending message target normal console print
### UpdateTargetAboutInvite
  Updates invited user about being invited to group (and remembers inviters ref and group name)
### UserInviteAccept
  Invited user update inviter about accepting, initating inviter to send "InviteUserApproveMessage" message to server, and sending "Welcome" message back to invited user
  
## Client < -- > Server messages (located at common folder)
### ActionSucess / ActionFailed
  Message sent from server to update client about Successful/failed action, like connection/disconnect etc.

