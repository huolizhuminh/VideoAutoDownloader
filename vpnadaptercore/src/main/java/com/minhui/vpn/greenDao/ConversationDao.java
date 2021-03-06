package com.minhui.vpn.greenDao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.minhui.vpn.nat.Conversation;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "CONVERSATION".
*/
public class ConversationDao extends AbstractDao<Conversation, Long> {

    public static final String TABLENAME = "CONVERSATION";

    /**
     * Properties of entity Conversation.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property SessionTag = new Property(1, String.class, "sessionTag", false, "SESSION_TAG");
        public final static Property Index = new Property(2, int.class, "index", false, "INDEX");
        public final static Property RequestURL = new Property(3, String.class, "requestURL", false, "REQUEST_URL");
        public final static Property Size = new Property(4, long.class, "size", false, "SIZE");
        public final static Property Time = new Property(5, long.class, "time", false, "TIME");
        public final static Property Type = new Property(6, int.class, "type", false, "TYPE");
    }


    public ConversationDao(DaoConfig config) {
        super(config);
    }
    
    public ConversationDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"CONVERSATION\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"SESSION_TAG\" TEXT," + // 1: sessionTag
                "\"INDEX\" INTEGER NOT NULL ," + // 2: index
                "\"REQUEST_URL\" TEXT," + // 3: requestURL
                "\"SIZE\" INTEGER NOT NULL ," + // 4: size
                "\"TIME\" INTEGER NOT NULL ," + // 5: time
                "\"TYPE\" INTEGER NOT NULL );"); // 6: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"CONVERSATION\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String sessionTag = entity.getSessionTag();
        if (sessionTag != null) {
            stmt.bindString(2, sessionTag);
        }
        stmt.bindLong(3, entity.getIndex());
 
        String requestURL = entity.getRequestURL();
        if (requestURL != null) {
            stmt.bindString(4, requestURL);
        }
        stmt.bindLong(5, entity.getSize());
        stmt.bindLong(6, entity.getTime());
        stmt.bindLong(7, entity.getType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Conversation entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String sessionTag = entity.getSessionTag();
        if (sessionTag != null) {
            stmt.bindString(2, sessionTag);
        }
        stmt.bindLong(3, entity.getIndex());
 
        String requestURL = entity.getRequestURL();
        if (requestURL != null) {
            stmt.bindString(4, requestURL);
        }
        stmt.bindLong(5, entity.getSize());
        stmt.bindLong(6, entity.getTime());
        stmt.bindLong(7, entity.getType());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Conversation readEntity(Cursor cursor, int offset) {
        Conversation entity = new Conversation( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // sessionTag
            cursor.getInt(offset + 2), // index
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // requestURL
            cursor.getLong(offset + 4), // size
            cursor.getLong(offset + 5), // time
            cursor.getInt(offset + 6) // type
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Conversation entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setSessionTag(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setIndex(cursor.getInt(offset + 2));
        entity.setRequestURL(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSize(cursor.getLong(offset + 4));
        entity.setTime(cursor.getLong(offset + 5));
        entity.setType(cursor.getInt(offset + 6));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Conversation entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Conversation entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Conversation entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
