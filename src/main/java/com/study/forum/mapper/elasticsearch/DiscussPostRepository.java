package com.study.forum.mapper.elasticsearch;

import com.study.forum.pojo.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * 继承接口并声明实体类型和主键类型
 * @author wy
 * @version 1.0
 */

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
