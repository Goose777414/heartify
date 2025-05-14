package com.example.heartify.repository;


import com.example.heartify.model.Invitation;
import com.example.heartify.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends CrudRepository<Invitation, Long> {

    boolean existsBySenderAndReceiverAndAccepted(User sender, User receiver, boolean accepted);


    List<Invitation> findByReceiver(User receiver);

    Optional<Invitation> findBySenderAndReceiver(User sender, User receiver);
}