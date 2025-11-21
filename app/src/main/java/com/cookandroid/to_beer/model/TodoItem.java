package com.cookandroid.to_beer.model;

public class TodoItem {
    private int id;
    private String date;      // "YYYY-MM-DD" 형식 권장
    private String title;
    private int isComplete;   // 0 or 1
    private int weight;       // 난이도, 1..5 권장

    public TodoItem(int id, String date, String title, int isComplete, int weight) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.isComplete = isComplete;
        this.weight = weight;
    }

    // 생성자 (id 미정 상태로 생성할 때)
    public TodoItem(String date, String title, int weight) {
        this(-1, date, title, 0, weight);
    }

    // getter / setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getIsComplete() { return isComplete; }
    public void setIsComplete(int isComplete) { this.isComplete = isComplete; }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
}
