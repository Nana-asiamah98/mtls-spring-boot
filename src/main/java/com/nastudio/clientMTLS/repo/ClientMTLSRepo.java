package com.nastudio.clientMTLS.repo;

import com.nastudio.clientMTLS.model.ClientMTLS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientMTLSRepo extends JpaRepository<ClientMTLS , Integer> {

    Optional<ClientMTLS> findDistinctByClientCode(String clientCode);
}
