package org.example.controller.mongodb;

import org.example.dao.MongoTestRepository;
import org.example.domain.MongoTest;
import org.example.core.result.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
*
*@author wcx
*@date 2022/10/23 19:03
*/
@RestController
@RequestMapping("/mongo")
public class MongodbTestController {

    @Resource
    private MongoTestRepository mongoTestRepository;

    /**
    *新增
    */
    @PostMapping
    public R add(@RequestBody MongoTest mongoTest) {
        mongoTestRepository.save(mongoTest);
        return R.ok(mongoTest.getId());
    }
    /**
    *更新
    */
    @PutMapping
    public R update(@RequestBody MongoTest mongoTest) {
        mongoTestRepository.updateById(mongoTest.getId(),mongoTest);
        return R.ok();
    }
    /**
    *删除
    */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable(value = "id") String id) {
        MongoTest mongoTest = new MongoTest();
        mongoTest.setId(id);
        mongoTestRepository.deleteById(mongoTest);
        return R.ok();
    }
    /**
    *查询
    */
    @GetMapping
    public R<List<MongoTest>> get(String id) {
        MongoTest mongoTest = new MongoTest();
        mongoTest.setId(id);
        return R.ok(mongoTestRepository.findByCondition(mongoTest));
    }
}
