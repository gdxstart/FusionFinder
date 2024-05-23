package com.ps.springbootinit;

import java.util.ArrayList;
import java.util.Date;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ps.springbootinit.model.entity.Post;
import com.ps.springbootinit.service.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class CrawlerTest {

    @Autowired
    private PostService postService;
    @Test
    public void testFetchPassage() {
        // 1.获取数据
        String json = "{\"current\":1,\"pageSize\":8,\"sortField\":\"createTime\",\"sortOrder\":\"descend\",\"category\":\"文章\",\"tags\":[],\"reviewStatus\":1}";
        String url = "https://api.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest.post(url)
                .body(json)
                .execute().body();
//        System.out.println(result);

        // 2.json 转对象
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject recordTemp = (JSONObject) record;
            Post post = new Post();
            if (recordTemp.getStr("title") != null) {
                post.setTitle(recordTemp.getStr("title"));
            }

            if (recordTemp.getStr("content") != null) {
                post.setContent(recordTemp.getStr("content"));
            }

            if (recordTemp.get("tags") != null) {
                JSONArray tags = (JSONArray) recordTemp.get("tags");
                List<String> tagList = tags.toList(String.class);
                post.setTags(JSONUtil.toJsonStr(tagList));
            }
            post.setUserId(1L);
            postList.add(post);
        }

        // 3.数据入库
        boolean b = postService.saveBatch(postList);
        Assertions.assertTrue(b);
    }
}
