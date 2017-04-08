package io.github.wongben;

import com.alibaba.fastjson.JSON;
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
    private static Map<String, List<Location>> locationMap = new HashMap<>();

    //整理请求链接
    static {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("mapi.dianping.com")
                .addPathSegment("searchshop.json")
                .addQueryParameter("start", "0")
                .addQueryParameter("regionid", "2")
                .addQueryParameter("categoryid", "10")//这里定位是美食
                .addQueryParameter("sortid", "0")
                .addQueryParameter("locatecityid", "1")
                .addQueryParameter("cityid", "1")//这里定位城市
                .addQueryParameter("_", "1490956859724")
                .addQueryParameter("callback", "Zepto1490956702678")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
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

            for(Location l : regionList) {
                if (locationMap.get(l.getParentId()) == null) {
                    List<Location> list = new ArrayList<>();
                    list.add(l);
                    locationMap.put(l.getParentId(),list);
                } else {
                    locationMap.get(l.getParentId()).add(l);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void run() {
        for (Map.Entry<String, List<Location>> entry : locationMap.entrySet()) {
            threadPool.execute(new Job(entry.getKey(),entry.getValue()));
        }
        threadPool.shutdown();
    }

    static class Job implements Runnable {
        private String parentId;
        private List<Location> regionList;
        Job(String parentId, List<Location> regionList){
            this.parentId = parentId;
            this.regionList = regionList;
        }

        public void run() {
            try {
                List<Shop> dataList = new ArrayList<>();
                for (Location location : regionList) {
                    int pageIndex = 0;
                    String locationId = location.getId();
                    for(;;) {
                        String data = query(locationId, pageIndex);
                        //Zepto1490956702678( --19字母
                        String resultData = data.substring(19, data.length() - 1);
                        Result result = JSON.parseObject(resultData, Result.class);
                        dataList.addAll(result.getList());
                        pageIndex = pageIndex + 25;
                        Thread.sleep(500);//防止调用过快被封
                        if (pageIndex >= result.getRecordCount()) {
                            break;
                        }
                    }
                }
                //此大区数据收集完毕，导入至Excel
                exportExcel(parentId,dataList);
                logger.info("完成一个大区 {}", parentId);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (StringIndexOutOfBoundsException e) {
                logger.info("调用频率过高");
            }
        }
    }

    static String query(String regionId, int pageIndex) {
        String result = null;
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("mapi.dianping.com")
                .addPathSegment("searchshop.json")
                .addQueryParameter("start", String.valueOf(pageIndex))
                .addQueryParameter("regionid", regionId)
                .addQueryParameter("categoryid", "10")
                .addQueryParameter("sortid", "0")
                .addQueryParameter("locatecityid", "1")
                .addQueryParameter("cityid", "1")
                .addQueryParameter("_", "1490956859724")
                .addQueryParameter("callback", "Zepto1490956702678")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            result = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    static void exportExcel(String fileName, List<Shop> dataList) {
        String[] headers = {"name","categoryName","branchName","regionName","priceText"};
        fileName = "d://" + fileName + ".xls";
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
