package ru.ifmo.ctddev.FSSARecSys.db.tables;

import ru.ifmo.ctddev.FSSARecSys.db.utils.Record;

import javax.persistence.*;


@Entity
@Table(name="Version")
public class VersionRecord extends Record {
    @Id
    private int id;

    @Column(name="version", nullable=false)
    private int version;

    // setters and getters

    @SuppressWarnings("unused")
    public int getId() {
        return id;
    }

    @SuppressWarnings("unused")
    public void setId(int id) {
        this.id = id;
    }

    @SuppressWarnings("unused")
    public int getVersion() {
        return version;
    }

    @SuppressWarnings("unused")
    public void setVersion(int version) {
        this.version = version;
    }
}
