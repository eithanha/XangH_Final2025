package com.example.xangh_final2025.models;

public class Category {
    private int id;
    private String name;

    // Default constructor
    public Category() {
    }

    // Constructor with all fields
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor without ID (for new categories)
    public Category(String name) {
        this.name = name;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
