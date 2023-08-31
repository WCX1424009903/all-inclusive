package org.example.mongodb.service;

import org.example.mongodb.vo.PageMongo;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
/**
*MongoDB通用Dao
*@author wcx
*@date 2022/10/23 20:26
*/
public interface BaseMongoDAO<T> {

    /**
     * 保存一个对象到mongodb
     *
     * @param entity
     * @return
     */
    T save(T entity);

    /**
     * 根据id删除对象
     *
     * @param t
     */
    void deleteById(T t);

    /**
     * 根据对象的属性删除
     *
     * @param t
     */
    void deleteByCondition(T t);


    /**
     * 根据id进行更新
     *
     * @param id
     * @param t
     */
    void updateById(String id, T t);


    /**
     * 根据对象的属性查询
     *
     * @param t
     * @return
     */
    List<T> findByCondition(T t);


    /**
     * 通过条件查询实体(集合)
     *
     * @param query
     */
    List<T> find(Query query);

    /**
     * 通过一定的条件查询一个实体
     *
     * @param query
     * @return
     */
    T findOne(Query query);

    /**
     * 通过条件查询更新数据
     *
     * @param query
     * @param update
     * @return
     */
    void update(Query query, Update update);

    /**
     * 通过ID获取记录
     *
     * @param id
     * @return
     */
    T findById(String id);

    /**
     * 通过ID获取记录,并且指定了集合名(表的意思)
     *
     * @param id
     * @param collectionName 集合名
     * @return
     */
    T findById(String id, String collectionName);

    /**
     * 通过条件查询,查询分页结果
     *
     * @param page
     * @param query
     * @return
     */
    PageMongo<T> findPage(PageMongo<T> page, Query query);

    /**
     * 求数据总和
     *
     * @param query
     * @return
     */
    long count(Query query);


    /**
     * 获取MongoDB模板操作
     *
     * @return
     */
    MongoTemplate getMongoTemplate();
}
