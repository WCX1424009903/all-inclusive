package elasticsearch.demo.controller;

import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.example.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * es基本操作
 * @author wcx
 * @date 2021/8/29 17:44
 */
@RestController
public class BaseElasticSearchController {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @PostMapping("/insertValueIndex")
    public void insertIndex() {
        IndexRequest indexRequest = new IndexRequest("test_index");
        Map<String,Object> map = new HashMap<>(3);
        map.put("user_id","456");
        map.put("user_name","拉力");
        map.put("user_password","678");
        indexRequest.source(map);
        try {
            restHighLevelClient.index(indexRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
