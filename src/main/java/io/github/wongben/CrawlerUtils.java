package io.github.wongben;

import com.alibaba.fastjson.JSON;
import io.github.wongben.model.City;
import io.github.wongben.model.Location;
import io.github.wongben.model.Result;
import io.github.wongben.model.Shop;
import io.github.wongben.utils.ExcelUtil;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by WongBen on 2017/03/31 0031.
 */
public class CrawlerUtils {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerUtils.class);

    private static final OkHttpClient client = new OkHttpClient();
    private static ExecutorService threadPool = Executors.newSingleThreadExecutor(); //可以改为多线程
    private static String categoryId = "147";//这里定位是10-美食,147健身
    private static List<City> cityList = new ArrayList<City>(){{
        add(new City("1","上海"));
        add(new City("2","北京"));
        add(new City("3","杭州"));
        add(new City("4","广州"));
        add(new City("5","南京"));
        add(new City("6","苏州"));
        add(new City("7","深圳"));
        add(new City("8","成都"));
        add(new City("9","重庆"));
//        add(new City("10","天津"));
//        add(new City("17","西安"));
        //add(new City("16","武汉"));
    }};
    //整理请求链接
    static {
        initCityList(cityList);
    }

    static void run() {
        for (City city : cityList) {
            threadPool.execute(new Job(city) );
        }
        threadPool.shutdown();
    }

    static void initCityList(List<City> cityList) {
        for (City city : cityList) {
            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host("mapi.dianping.com")
                    .addPathSegment("searchshop.json")
                    .addQueryParameter("start", "0")
                    .addQueryParameter("regionid", "0")
                    .addQueryParameter("categoryid", categoryId)
                    .addQueryParameter("sortid", "0")
                    .addQueryParameter("locatecityid", city.getId())
                    .addQueryParameter("cityid", city.getId())//这里定位城市
                    .addQueryParameter("_", "1490956859724")
                    .addQueryParameter("callback", "Zepto1490956702678")
                    .build();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            logger.info("初始化 {}", url.toString() );
            try {
                Response response = client.newCall(request).execute();
                String resultData = response.body().string();
                resultData = resultData.substring(19, resultData.length() - 1);
                Result result = JSON.parseObject(resultData, Result.class);
                List<Location> regionList = result.getRegionNavs().stream().filter(
                        location ->
                                Integer.parseInt(location.getParentId()) > 0
                                        && Integer.parseInt(location.getId()) > Integer.parseInt(location.getParentId())
                                        //&& Integer.parseInt(location.getParentId()) == 2 //可以测试只导出某个区域
                ).collect(Collectors.toList());
                Map<String, List<Location>> locationMap = new HashMap<>();
                for (Location l : regionList) {
                    if (locationMap.get(l.getParentId()) == null) {
                        List<Location> list = new ArrayList<>();
                        list.add(l);
                        locationMap.put(l.getParentId(), list);
                    } else {
                        locationMap.get(l.getParentId()).add(l);
                    }
                }
                city.setLocationMap(locationMap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    static class Job implements Runnable {
        private City city;
        Job(City city){
            this.city = city;
        }
        public void run() {
            try {
                List<Shop> dataList = new ArrayList<>();
                for (Map.Entry<String, List<Location>> entry : city.getLocationMap().entrySet()) {
                    for (Location location : entry.getValue()) {
                        int pageIndex = 0;
                        String locationId = location.getId();
                        for (;;) {
                            String data = query(city.getId(), locationId, pageIndex);
                            Result result = formatShopData(data, city.getName(), location.getName());
                            dataList.addAll(result.getList());
                            pageIndex = pageIndex + 25;
                            Thread.sleep(500);//防止调用过快被封
                            if (pageIndex >= result.getRecordCount()) {
                                break;
                            }
                        }
                    }
                }
                //此城市数据收集完毕，导入至Excel
                exportExcel(city.getName(),dataList);
                logger.info("完成一个城市 {}", city.getName());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static Result formatShopData(String data, String cityName, String location){
        //Zepto1490956702678( --19字母
        Result result = null;
        try {
            String resultData = data.substring(19, data.length() - 1);
            result = JSON.parseObject(resultData, Result.class);
            for(Shop shop : result.getList()) {
                shop.setCityName(cityName);
                shop.setLocation(location);
            }
        }
        catch (StringIndexOutOfBoundsException e) {
            logger.info("调用频率过高");
        }
        return result;
    }

    static String query(String cityId, String regionId, int pageIndex) {
        String result = null;
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("mapi.dianping.com")
                .addPathSegment("searchshop.json")
                .addQueryParameter("start", String.valueOf(pageIndex))
                .addQueryParameter("regionid", regionId)
                .addQueryParameter("categoryid", categoryId)
                .addQueryParameter("sortid", "0")
                .addQueryParameter("locatecityid", cityId)
                .addQueryParameter("cityid", cityId)
                .addQueryParameter("_", "1490956859724")
                .addQueryParameter("callback", "Zepto1490956702678")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            logger.info("URL {}", url.toString() );
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void exportExcel(String fileName, List<Shop> dataList) {
        String[] headers = {"名称","分类","分店名称","regionName","价格","城市","商圈"};
        fileName = "d://" + fileName + ".xlsx";
        File file = new File(fileName);
        try {
            OutputStream out = new FileOutputStream(file);
            ExcelUtil.exportExcel(headers, dataList, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
