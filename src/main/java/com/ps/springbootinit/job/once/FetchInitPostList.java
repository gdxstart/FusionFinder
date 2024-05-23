package com.ps.springbootinit.job.once;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ps.springbootinit.esdao.PostEsDao;
import com.ps.springbootinit.model.dto.post.PostEsDTO;
import com.ps.springbootinit.model.entity.Post;
import com.ps.springbootinit.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全量同步帖子
 */
// todo 取消注释后，每次启动springboot项目会启动run方法
@Component
@Slf4j
public class FetchInitPostList implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Override
    public void run(String... args) {
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
        if (b) {
            log.info("获取到初始化帖子列表成功，条数={}", postList.size());
        } else {
            log.error("获取到初始化帖子列表失败");
        }
    }
}
