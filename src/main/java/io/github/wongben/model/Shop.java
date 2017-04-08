package io.github.wongben.model;

import io.github.wongben.utils.ExcelCell;

/**
 * Created by WongBen on 2017/03/31 0031.
 */
public class Shop {
    @ExcelCell(index = 0)
    private String name;
    @ExcelCell(index = 1)
    private String categoryName; //商店类型
    @ExcelCell(index = 2)
    private String branchName;//分店
    @ExcelCell(index = 3)
    private String regionName;//区域
    @ExcelCell(index = 4)
    private String priceText;// 价格
    @ExcelCell(index = 5)
    private String scoreText;// 评分
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getPriceText() {
        return priceText;
    }

    public void setPriceText(String priceText) {
        this.priceText = priceText;
    }

    public String getScoreText() {
        return scoreText;
    }

    public void setScoreText(String scoreText) {
        this.scoreText = scoreText;
    }
}
