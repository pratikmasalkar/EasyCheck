package com.example.easycheck; // Replace with your package name
public class Attendance {
    private String roll;
    private String studentName;
    private String status;
    private String batchName;
    private String subject;
    private String date;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    private String flag;
    private String code;

    // Default constructor
    public Attendance() {
    }

    // Parameterized constructor
    public Attendance(String roll, String studentName, String status, String batchName,String flag, String subject, String date,String code) {
        this.roll = roll;
        this.studentName = studentName;
        this.status = status;
        this.batchName = batchName;
        this.subject = subject;
        this.date = date;
        this.flag=flag;
        this.code=code;
    }

    // Getters and setters
    public String getRoll() {
        return roll;
    }

    public void setRoll(String roll) {
        this.roll = roll;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
