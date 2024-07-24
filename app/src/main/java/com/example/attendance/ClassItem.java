package com.example.attendance;



public class ClassItem {
    private String classname;
    private String subjectname;
    private long cid;

    public long getCid() {
        return cid;
    }

    public ClassItem(long cid, String classname, String subjectname) {
        this.classname = classname;
        this.subjectname = subjectname;
        this.cid = cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public ClassItem(String classname, String subjectname) {
        this.classname = classname;
        this.subjectname = subjectname;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getSubjectname() {
        return subjectname;
    }

    public void setSubjectname(String subjectname) {
        this.subjectname = subjectname;
    }
}
