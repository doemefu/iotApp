package ch.furchert.iotapp.model;

import java.util.Date;
import jakarta.persistence.*;

@Entity
@Table(name = "status")
public class UserStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EUserStatus name;

    //private Date created;
    //private Date updated;

    public UserStatus() {

    }

    //@PrePersist
    //protected void onCreate() {
    //    this.created = new Date();
    //    this.updated = new Date();
    //}

    //@PreUpdate
    //protected void onUpdate() {
    //    this.updated = new Date();
    //}

    public UserStatus(EUserStatus name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EUserStatus getName() {
        return name;
    }

    public void setName(EUserStatus name) {
        this.name = name;
    }
}