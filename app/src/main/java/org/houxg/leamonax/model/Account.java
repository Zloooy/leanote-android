package org.houxg.leamonax.model;

import com.google.gson.annotations.SerializedName;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.houxg.leamonax.database.AppDataBase;

import java.util.List;

@Table(name = "Account", database = AppDataBase.class)
public class Account extends BaseModel {

    public static final int EDITOR_RICH_TEXT = 0;
    public static final int EDITOR_MARKDOWN = 1;

    @SerializedName("Ok")
    boolean isOk;
    @SerializedName("Msg")
    String msg;

    @Column(name = "id")
    @PrimaryKey(autoincrement = true)
    long localUserId;
    @Column(name = "userId")
    @SerializedName("UserId")
    String userId = "";
    @Column(name = "userName")
    @SerializedName("Username")
    String userName = "";
    @Column(name = "email")
    @SerializedName("Email")
    String email = "";
    @Column(name = "verified")
    @SerializedName("Verified")
    boolean verified;
    @Column(name = "avatar")
    @SerializedName("Avatar")
    String avatar = "";
    @Column(name = "token")
    @SerializedName("Token")
    String accessToken = "";
    @Column(name = "defaultEditor")
    int defaultEditor = EDITOR_MARKDOWN;
    @Column(name = "host")
    @SerializedName("Host")
    String host = "";
    @Column(name = "noteUsn")
    int noteUsn;
    @Column(name = "notebookUsn")
    int notebookUsn;
    @Column(name = "lastUseTime")
    long lastUseTime;

    @Deprecated
    @Column(name = "lastUsn")
    @SerializedName("LastSyncUsn")
    int lastSyncUsn;

    public Account() {
    }

    public boolean isOk() {
        return isOk;
    }

    public long getLocalUserId() {
        return localUserId;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @Deprecated
    public int getLastSyncUsn() {
        return lastSyncUsn;
    }

    public int getDefaultEditor() {
        return defaultEditor;
    }

    public String getHost() {
        return host;
    }

    public String getMsg() {
        return msg;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Deprecated
    public void setLastUsn(int lastUsn) {
        this.lastSyncUsn = lastUsn;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setDefaultEditor(int defaultEditor) {
        this.defaultEditor = defaultEditor;
    }

    public int getNoteUsn() {
        return noteUsn;
    }

    public int getNotebookUsn() {
        return notebookUsn;
    }

    public void setNoteUsn(int noteUsn) {
        this.noteUsn = noteUsn;
    }

    public void setNotebookUsn(int notebookUsn) {
        this.notebookUsn = notebookUsn;
    }

    public void updateLastUseTime() {
        lastUseTime = System.currentTimeMillis() / 1000;
    }

    public long getLastUseTime() {
        return lastUseTime;
    }

    public static Account getAccount(String email, String host) {
        return SQLite.select()
                .from(Account.class)
                .where(Account_Table.email.eq(email))
                .and(Account_Table.host.eq(host))
                .querySingle();
    }

    public static Account getAccountWithToken() {
        return SQLite.select()
                .from(Account.class)
                .where(Account_Table.token.notEq(""))
                .orderBy(Account_Table.lastUseTime, false)
                .querySingle();
    }

    public static List<Account> getAccountListWithToken() {
        return SQLite.select()
                .from(Account.class)
                .where(Account_Table.token.notEq(""))
                .orderBy(Account_Table.lastUseTime, false)
                .queryList();
    }

    public static Account getAccountById(long id) {
        return new Select()
                .from(Account.class)
                .where(Account_Table.id.eq(id))
                .querySingle();
    }

    @Override
    public String toString() {
        return "Account{" +
                "isOk=" + isOk +
                ", msg='" + msg + '\'' +
                ", localUserId=" + localUserId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + verified +
                ", avatar='" + avatar + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", defaultEditor=" + defaultEditor +
                ", lastSyncUsn=" + lastSyncUsn +
                ", host='" + host + '\'' +
                '}';
    }
}
