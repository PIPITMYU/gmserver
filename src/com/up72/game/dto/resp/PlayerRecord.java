package com.up72.game.dto.resp;

/**
 * Created by Administrator on 2017/7/8.
 */
public class PlayerRecord {
    private Integer id;
    private Integer roomId;
    private String startTime;
    private String endTime;
    private String eastUserId;
    private String eastUserName;
    private Integer eastUserMoneyRecord;
    private Integer eastUserMoneyRemain;


    private String southUserId;
    private String southUserName;
    private Integer southUserMoneyRecord;
    private Integer southUserMoneyRemain;


    private String westUserId;
    private String westUserName;
    private Integer westUserMoneyRecord;
    private Integer westUserMoneyRemain;


    private String northUserId;
    private String northUserName;
    private Integer northUserMoneyRecord;
    private Integer northUserMoneyRemain;
    private Integer clubId;// 俱乐部id

    public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRoomId() {
        return roomId;
    }

    public void setRoomId(Integer roomId) {
        this.roomId = roomId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEastUserName() {
        return eastUserName;
    }

    public void setEastUserName(String eastUserName) {
        this.eastUserName = eastUserName;
    }

    public Integer getEastUserMoneyRecord() {
        return eastUserMoneyRecord;
    }

    public void setEastUserMoneyRecord(Integer eastUserMoneyRecord) {
        this.eastUserMoneyRecord = eastUserMoneyRecord;
    }

    public Integer getEastUserMoneyRemain() {
        return eastUserMoneyRemain;
    }

    public void setEastUserMoneyRemain(Integer eastUserMoneyRemain) {
        this.eastUserMoneyRemain = eastUserMoneyRemain;
    }

    public String getSouthUserName() {
        return southUserName;
    }

    public void setSouthUserName(String southUserName) {
        this.southUserName = southUserName;
    }

    public Integer getSouthUserMoneyRecord() {
        return southUserMoneyRecord;
    }

    public void setSouthUserMoneyRecord(Integer southUserMoneyRecord) {
        this.southUserMoneyRecord = southUserMoneyRecord;
    }

    public Integer getSouthUserMoneyRemain() {
        return southUserMoneyRemain;
    }

    public void setSouthUserMoneyRemain(Integer southUserMoneyRemain) {
        this.southUserMoneyRemain = southUserMoneyRemain;
    }

    public String getWestUserName() {
        return westUserName;
    }

    public void setWestUserName(String westUserName) {
        this.westUserName = westUserName;
    }

    public Integer getWestUserMoneyRecord() {
        return westUserMoneyRecord;
    }

    public void setWestUserMoneyRecord(Integer westUserMoneyRecord) {
        this.westUserMoneyRecord = westUserMoneyRecord;
    }

    public Integer getWestUserMoneyRemain() {
        return westUserMoneyRemain;
    }

    public void setWestUserMoneyRemain(Integer westUserMoneyRemain) {
        this.westUserMoneyRemain = westUserMoneyRemain;
    }

    public String getNorthUserName() {
        return northUserName;
    }

    public void setNorthUserName(String northUserName) {
        this.northUserName = northUserName;
    }

    public Integer getNorthUserMoneyRecord() {
        return northUserMoneyRecord;
    }

    public void setNorthUserMoneyRecord(Integer northUserMoneyRecord) {
        this.northUserMoneyRecord = northUserMoneyRecord;
    }

    public Integer getNorthUserMoneyRemain() {
        return northUserMoneyRemain;
    }

    public void setNorthUserMoneyRemain(Integer northUserMoneyRemain) {
        this.northUserMoneyRemain = northUserMoneyRemain;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEastUserId() {
        return eastUserId;
    }

    public void setEastUserId(String eastUserId) {
        this.eastUserId = eastUserId;
    }

    public String getSouthUserId() {
        return southUserId;
    }

    public void setSouthUserId(String southUserId) {
        this.southUserId = southUserId;
    }

    public String getWestUserId() {
        return westUserId;
    }

    public void setWestUserId(String westUserId) {
        this.westUserId = westUserId;
    }

    public String getNorthUserId() {
        return northUserId;
    }

    public void setNorthUserId(String northUserId) {
        this.northUserId = northUserId;
    }
}
