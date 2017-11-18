package de.greenrobot.testdao;


// THIS CODE IS GENERATED, DO NOT EDIT.
/**
 * Entity mapped to table TEST_ENTITY (schema version 1).
*/
public class TestEntity {

    private Long id;
    private int simpleInt;
    private Integer simpleInteger;
    private String simpleStringNotNull;
    private String simpleString;
    private String indexedString;
    private String indexedStringAscUnique;

    public TestEntity() {
    }

    public TestEntity(Long id) {
        this.id = id;
    }

    public TestEntity(Long id, int simpleInt, Integer simpleInteger, String simpleStringNotNull, String simpleString, String indexedString, String indexedStringAscUnique) {
        this.id = id;
        this.simpleInt = simpleInt;
        this.simpleInteger = simpleInteger;
        this.simpleStringNotNull = simpleStringNotNull;
        this.simpleString = simpleString;
        this.indexedString = indexedString;
        this.indexedStringAscUnique = indexedStringAscUnique;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSimpleInt() {
        return simpleInt;
    }

    public void setSimpleInt(int simpleInt) {
        this.simpleInt = simpleInt;
    }

    public Integer getSimpleInteger() {
        return simpleInteger;
    }

    public void setSimpleInteger(Integer simpleInteger) {
        this.simpleInteger = simpleInteger;
    }

    public String getSimpleStringNotNull() {
        return simpleStringNotNull;
    }

    public void setSimpleStringNotNull(String simpleStringNotNull) {
        this.simpleStringNotNull = simpleStringNotNull;
    }

    public String getSimpleString() {
        return simpleString;
    }

    public void setSimpleString(String simpleString) {
        this.simpleString = simpleString;
    }

    public String getIndexedString() {
        return indexedString;
    }

    public void setIndexedString(String indexedString) {
        this.indexedString = indexedString;
    }

    public String getIndexedStringAscUnique() {
        return indexedStringAscUnique;
    }

    public void setIndexedStringAscUnique(String indexedStringAscUnique) {
        this.indexedStringAscUnique = indexedStringAscUnique;
    }

}