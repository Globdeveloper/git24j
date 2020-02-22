package com.github.git24j.core;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

public class Signature extends CAutoReleasable {
    protected Signature(boolean isWeak, long rawPtr) {
        super(isWeak, rawPtr);
    }

    @Override
    protected void freeOnce(long cPtr) {
        // FIXME: not implemented yet
    }

    private String name = "";
    private String email = "";
    private OffsetDateTime when = OffsetDateTime.now();

    public Signature(String name, String email) {
        super(true, 0);
        this.name = name;
        this.email = email;
    }

    public Signature(String name, String email, long whenEpocSec, int offsetMin) {
        super(true, 0);
        this.name = name;
        this.email = email;
        this.when =
                Instant.ofEpochSecond(whenEpocSec)
                        .atOffset(ZoneOffset.ofHoursMinutes(0, offsetMin));
    }

    Signature() {
        super(true, 0);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public OffsetDateTime getWhen() {
        // when.getOffset().getTotalSeconds()
        return when;
    }

    /** @return get epoc second of {@code when} */
    public long getWhenEpocSecond() {
        return when.toEpochSecond();
    }

    /** @return get offset of when in minutes. */
    public int getWhenOffsetMinutes() {
        return when.getOffset().getTotalSeconds() / 60;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // TODO: figure out how to deal with sign
    public void setWhen(long whenEpocSec, int offsetMinutes, char sign) {
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetMinutes * 60);
        this.when = Instant.ofEpochSecond(whenEpocSec).atOffset(offset);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Signature signature = (Signature) o;
        return Objects.equals(name, signature.name)
                && Objects.equals(email, signature.email)
                && Objects.equals(when, signature.when);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, when);
    }
}
