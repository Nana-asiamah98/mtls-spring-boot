package com.nastudio.clientMTLS.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "tbl_mtls")
public class ClientMTLS {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String clientName;

    @Column
    private String clientCode;

    @Column
    private String dopplerName;

    @Column(name = "certificate_option")
    private String certificateOption;

    @Column
    private String pemParaphrase;

    @Column(name = "callback_url")
    private String callBackURL;

    @Column(name = "isMtlsEnabled")
    private Boolean isMtlsEnabled;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @UpdateTimestamp
    private ZonedDateTime updatedAt;
}
