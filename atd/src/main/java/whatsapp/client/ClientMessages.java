package whatsapp.client;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ClientMessages {

// user-related
static public class ClientConnectMessage implements Serializable{
    public final String username;
    public ClientConnectMessage(String username) {
        this.username = username;
    }
  }
  static public class ClientDisconnectMessage implements Serializable{
    public ClientDisconnectMessage() {}
  }

  static public class ClientSendText implements Serializable{
    String target_name;
    String text;
    public ClientSendText(String target_name, String text) {
      this.target_name = target_name;
      this.text = text;
    }
  }
  static public class ClientSendFile implements Serializable{
    String target_name;
    byte[] file;
    public ClientSendFile(String target_name, byte[] file){
      this.target_name = target_name;
      this.file = file;
    }
  }


  static public class UserTextMessage implements Serializable {
    String source;
    String message;
    public UserTextMessage(String source, String message) {
        this.source = source;
        this.message = message;
    }
    public String getMessage() {
        return (getTime() + "[user][" + source + "]" + message);
    }
  }

  static public class UserFileMessage implements Serializable {
    String source;
    byte[] file;
    public UserFileMessage(String source, byte[] file) {
        this.source = source;
        this.file = file;
    }
  }

  static public class UserLogMessage implements Serializable {
    String message;
    public UserLogMessage(String message) {
        this.message = message;
    }
    public String getMessage() { return this.message; }
  }

  static public class UpdateTargetAboutInvite implements Serializable{
    String group_name;
    String target_name;
    public UpdateTargetAboutInvite(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }

  // Group-related
  static public class ClientGroupCreate implements Serializable{
    public final String group_name;
    public ClientGroupCreate(String group_name) {
        this.group_name = group_name;
    }
  }
  
  static public class ClientGroupLeave implements Serializable{
    public final String group_name;
    public ClientGroupLeave(String group_name) {
        this.group_name = group_name;
    }
  }

  static public class ClientGroupText implements Serializable{
    String group_name;
    String text;
    public ClientGroupText(String group_name, String text){
      this.group_name = group_name;
      this.text = text;
    }
  }

  static public class ClientGroupFile implements Serializable{
    String group_name;
    byte[] file;
    public ClientGroupFile(String group_name, byte[] file){
      this.group_name = group_name;
      this.file = file;
    }
  }

  static public class ClientGroupInvite implements Serializable{
    String group_name;
    String target_name;
    public ClientGroupInvite(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }

  static public class ClientInviteAccepted implements Serializable{}
  static public class ClientInviteDeclined implements Serializable{}

  static public class ClientRemoveUser implements Serializable{
    String group_name;
    String target_name;
    public ClientRemoveUser(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }

  static public class ClientGroupUserMute implements Serializable{
    String group_name;
    String target_name;
    int mute_time;
    public ClientGroupUserMute(String group_name, String target_name, int mute_time){
      this.group_name = group_name;
      this.target_name = target_name;
      this.mute_time = mute_time;
    }
  }

  static public class ClientGroupUserUnmute implements Serializable{
    String group_name;
    String target_name;
    public ClientGroupUserUnmute(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }
  
  static public class ClientGroupAddCoAdmin implements Serializable{
    String group_name;
    String target_name;
    public ClientGroupAddCoAdmin(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }

  static public class ClientGroupRemCoAdmin implements Serializable{
    String group_name;
    String target_name;
    public ClientGroupRemCoAdmin(String group_name, String target_name){
      this.group_name = group_name;
      this.target_name = target_name;
    }
  }

  // User-invite-related
  static public class UserInviteRequest implements Serializable {
    String group_name;
    public UserInviteRequest(String group_name){
      this.group_name = group_name;
    }
  }
  static public class UserInviteAccept implements Serializable {
    String group_name;
    String accepter_name;
    public UserInviteAccept(String group_name, String accepter_name){
      this.group_name = group_name;
      this.accepter_name = accepter_name;
    }
  }

  static public String getTime(){
    LocalDateTime now = LocalDateTime.now();
    return ("["+now.getHour()+":"+now.getMinute()+"]");
  }

}