package com.bugtracker.server.service.i;

import com.bugtracker.server.entity.Membership;
import com.bugtracker.server.entity.User;
import com.bugtracker.server.entity.enums.Role;

import java.util.List;

public interface IMemberService {
    List<Membership> getMembers(Long actorId, Long projectId);

    User findCandidate(Long actorId, Long projectId, String username);

    Membership addMember(Long actorId, Long projectId, String username, Role role);

    Membership changeRole(Long actorId, Long projectId, Long userId, Role role);

    void removeMember(Long actorId, Long projectId, Long userId);
}
