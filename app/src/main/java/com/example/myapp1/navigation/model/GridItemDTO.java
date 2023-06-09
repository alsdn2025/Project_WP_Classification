package com.example.myapp1.navigation.model;

import android.util.Log;

/**
 * DTO for GridView
 * @author MW
 */
public class GridItemDTO {
    public int gridIndex;
    private int image;
    private String className;
    private int classId;
    private boolean isCollected;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public GridItemDTO(int index, int image, String className, int classId, boolean isCollected) {
        this.gridIndex = index;
        this.image = image;
        this.className = className;
        this.classId = classId;

        this.isCollected = isCollected;
        Log.i("GridItem Created", "|name " + className + " | classId : " + classId);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getClassId() {
        return classId;
    }

    public void setClassId(int classId) {
        this.classId = classId;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public void setCollected(boolean collected) {
        isCollected = collected;
    }
}
