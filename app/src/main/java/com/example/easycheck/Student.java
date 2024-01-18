package com.example.easycheck; // Replace with your package name

public class Student {
    private String name;
    private String email;
    private String mobile;
    private String rollno;
    private String batch;

    public Student(String name,String email, String mobile, String rollno, String batch) {
        this.name = name;
        this.email=email;
        this.mobile = mobile;
        this.rollno = rollno;
        this.batch = batch;
    }

    // Getters and setters for each field (optional for now)
    public String getName() {
        return name;
    }
    public String getEmail(){
        return email;
    }
    public String getMobile() {
        return mobile;
    }

    public String getRollno() {
        return rollno;
    }

    public String getBatch() {
        return batch;
    }


    // Additional methods for validation, data manipulation, etc. (optional)
}
