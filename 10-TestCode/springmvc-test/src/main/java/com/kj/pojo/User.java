package com.kj.pojo;

import java.util.*;

public class User {
    private int id;
    private String username;
    private Date birthday;
    private String sex;

    // 演示包装POJO参数绑定
    private Address address;
    // 演示批量简单类型参数接收
    private List<Integer> uid = new ArrayList<>();
    //将request请求参数，绑定到[元素是POJO类型的List集合]参数
    private List<Item> itemList = new ArrayList<>();
    // 将request请求参数，绑定到[元素是POJO类型的Map集合]参数
    private Map<String, Item> itemMap = new HashMap<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Integer> getUid() {
        return uid;
    }

    public void setUid(List<Integer> uid) {
        this.uid = uid;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    public Map<String, Item> getItemMap() {
        return itemMap;
    }

    public void setItemMap(Map<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", birthday=" + birthday +
                ", sex='" + sex + '\'' +
                ", address=" + address +
                ", uid=" + uid +
                ", itemList=" + itemList +
                ", itemMap=" + itemMap +
                '}';
    }
}
