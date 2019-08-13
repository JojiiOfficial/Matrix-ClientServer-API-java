package de.jojii.matrixclientserver.Callbacks;

import de.jojii.matrixclientserver.Bot.Member;

import java.io.IOException;
import java.util.List;

public interface MemberCallback {
    void onResponse(List<Member> roomMember) throws IOException;
}
