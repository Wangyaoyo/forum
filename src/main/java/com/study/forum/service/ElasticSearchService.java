package com.study.forum.service;

import com.study.forum.mapper.DiscussPostMapper;
import com.study.forum.mapper.elasticsearch.DiscussPostRepository;
import com.study.forum.pojo.DiscussPost;
import com.study.forum.util.CommunityConstant;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author wy
 * @version 1.0
 */
@Service
public class ElasticSearchService implements CommunityConstant {

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;


    /**
     * 更新也用此方法
     * @param post
     */
    public void addPost(DiscussPost post) {
        discussRepository.save(post);
    }

    public void deletePost(Integer id) {
        discussRepository.deleteById(id);
    }

    public Page<DiscussPost> searchPost(String key, Integer current, Integer limit){
        if(current == null){
            current = 0;
        }
        if(limit == null){
            limit = PAGE_LIMIT;
        }
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                // 查询条件
                .withQuery(QueryBuilders.multiMatchQuery(key, "title", "content"))
                // 搜索条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                // 分页
                .withPageable(PageRequest.of(current, limit))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        return elasticTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                SearchHits hits = response.getHits();
                if (hits.getTotalHits() <= 0) {
                    return null;
                }

                // 构造新的结果集(可高亮显示的)
                List<DiscussPost> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    // 将结果Map中的内容重新封装
                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 获得高亮显示的内容
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        // 覆盖到新的结果中
                        // 取匹配到的第一段即可
                        post.setTitle(titleField.getFragments()[0].toString());
                    }

                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    list.add(post);
                }

                return new AggregatedPageImpl(list, pageable,
                        hits.getTotalHits(), response.getAggregations(), response.getScrollId(), hits.getMaxScore());
            }
        });
    }
}
