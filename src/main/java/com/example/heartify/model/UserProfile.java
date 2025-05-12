package com.example.heartify.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Integer age;
    private String city;
    private String about;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL)
    private List<Keyword> keywords;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL)
    private PrivateInfo privateInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<Keyword> keywords) {
        this.keywords = keywords;
    }

    public PrivateInfo getPrivateInfo() {
        return privateInfo;
    }

    public void setPrivateInfo(PrivateInfo privateInfo) {
        this.privateInfo = privateInfo;
    }
}
