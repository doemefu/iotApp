package ch.furchert.iotapp.model;

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

    public UserStatus() {

    }

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