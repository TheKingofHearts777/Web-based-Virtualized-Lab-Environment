package com.csproj.Cyberlab.API.user.labInstance.vmInstance;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "")
@Data
public class VncConnectionResponse {

    @NonNull
    private String vncWebSocketConnection; // @TODO: this will more than likely need to be URI encoded.

    @NonNull
    private String pveTicketCookie; // pve ticket with 'PVEAuthCookie=' prepended

    @NonNull
    private String vncPort;

}
