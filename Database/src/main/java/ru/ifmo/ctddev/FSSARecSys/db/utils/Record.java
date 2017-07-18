package ru.ifmo.ctddev.FSSARecSys.db.utils;

import java.io.Serializable;


public class Record implements Serializable {
    public void saveOrUpdate() {
        HibernateUtil.runQueryVoid(s -> s.saveOrUpdate(this));
    }
}
