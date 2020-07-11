package com.mobiotrics.contactless.smartcut;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Entity mapped to table "NOTE".
 */
@Entity(indexes = {
        @Index(value = "name, date DESC", unique = true)
})

public class Registration {

    public static final int REG_STATUS_NOT_REG = 0;
    public  static final int REG_STATUS_REG = 1;
    public  static final int REG_STATUS_VERIFIED = 2;


    @Id
    private Long id;

    @NotNull
    private String name;
    private String email;
    private java.util.Date date;
    private String verification_code;

    private int reg_status;

    @Generated(hash = 267918624)
    public Registration() {
    }

    public Registration(Long id) {
        this.id = id;
    }

    @Generated(hash = 2122139442)
    public Registration(Long id, @NotNull String name, String email, java.util.Date date,
            String verification_code, int reg_status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.date = date;
        this.verification_code = verification_code;
        this.reg_status = reg_status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    @NotNull
    public String getEmail() {
        return email;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setName(@NotNull String text) {
        this.name = text;
    }

    public java.util.Date getDate() {
        return date;
    }

    public void setDate(java.util.Date date) {
        this.date = date;
    }


    public int getStatus(){
        return this.reg_status;
    }

    public void setStatus(@NotNull int s){

        this.reg_status = s;

    }

    public String getName() {
        return this.name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getReg_status() {
        return this.reg_status;
    }

    public void setReg_status(int reg_status) {
        this.reg_status = reg_status;
    }

    public void setVericode(@NotNull String verification_code) {
        this.verification_code = verification_code;
    }

    public String getVerification_code() {
        return this.verification_code;
    }

    public void setVerification_code(String verification_code) {
        this.verification_code = verification_code;
    }
}