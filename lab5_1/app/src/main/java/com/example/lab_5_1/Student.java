package com.example.lab_5_1;

import java.util.Date;

public class Student {

    String ID;
    String Name;
    String DoB;
    String Email;
    String phoneNumber;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    @Override
    public String toString() {
        return "Student{" +
                "ID='" + ID + '\'' +
                ", Name='" + Name + '\'' +
                ", DoB='" + DoB + '\'' +
                ", Email='" + Email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    public Student() {

    }

    public Student(String ID, String Name, String DoB, String Email, String phoneNumber) {
        this.ID = ID;
        this.Name = Name;
        this.Email = Email;
        this.phoneNumber = phoneNumber;
        this.DoB = DoB;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDoB() {
        return DoB;
    }

    public void setDoB(String doB) {
        DoB = doB;
    }

}
