package whatsapp.common;

import java.io.Serializable;

public class GetUserDestMessage implements Serializable {
    final String username;

    public GetUserDestMessage(String username) {
        this.username = username;
    }

    public String getUsername() {
        return this.username;
    }
}