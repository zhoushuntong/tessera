package com.github.tessera.transaction.model;

import com.github.tessera.enclave.model.MessageHash;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * The JPA entity that contains the transaction information
 * A simple key/value pair
 *
 * The key is the transaction hash, and the value an a fully encoded {@link EncodedPayloadWithRecipients}
 */
@Entity
@Table(name = "ENCRYPTED_TRANSACTION")
public class EncryptedTransaction implements Serializable {

    @Id
    @Column(name = "ID")
    @SequenceGenerator(name = "ENC_TX_SEQ", sequenceName = "ENC_TX_SEQ")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ENC_TX_SEQ")
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(
            name = "hashBytes",
            column = @Column(name = "HASH", nullable = false, unique = true, updatable = false)
        )
    })
    private MessageHash hash;

    @Lob
    @Column(name = "ENCODED_PAYLOAD", nullable = false)
    private byte[] encodedPayload;

    public EncryptedTransaction(final MessageHash hash, final byte[] encodedPayload) {
        this.hash = hash;
        this.encodedPayload = encodedPayload;
    }

    public EncryptedTransaction() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public MessageHash getHash() {
        return this.hash;
    }

    public void setHash(final MessageHash hash) {
        this.hash = hash;
    }

    public byte[] getEncodedPayload() {
        return this.encodedPayload;
    }

    public void setEncodedPayload(final byte[] encodedPayload) {
        this.encodedPayload = encodedPayload;
    }

    @Override
    public int hashCode() {
        return 47 * 3 + Objects.hashCode(this.id);
    }

    @Override
    public boolean equals(final Object obj) {

        return (obj instanceof EncryptedTransaction) &&
            Objects.equals(this.id, ((EncryptedTransaction) obj).id);
    }


}