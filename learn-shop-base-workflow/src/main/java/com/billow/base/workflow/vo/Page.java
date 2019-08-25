package com.billow.base.workflow.vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页参数
 *
 * @author liuyongtao
 * @create 2019-08-25 12:12
 */
public class Page<T> {

    private static final Integer PAGE_SIZE = 10; // 每页要显示的记录数
    private static final Integer PAGE_NO = 1; // 当前页号
    private static final Integer RECORD_COUNT = 0; // 总记录数

    // 每页要显示的记录数
    private Integer pageSize = PAGE_SIZE;
    // 每页要显示的记录数
    private Integer pageNo = PAGE_NO;
    // 总记录数
    private long totalElements = RECORD_COUNT;
    // 总页数据
    private long totalPages = RECORD_COUNT;
    // 分页数据
    private List<T> content = new ArrayList<>();

    public Page() {
    }

    /**
     * 分页构造
     *
     * @param pageSize      页面大小
     * @param totalElements 总记录数
     * @param content       查询结果
     * @author billow
     * @date 2019/8/25 12:29
     */
    public Page(Integer pageSize, long totalElements, List<T> content) {
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.content = content;
        // 计算分页
        this.totalPages = pageSize == null ? 0 : (int) Math.ceil((double) totalElements / (double) pageSize);
    }

    public long getTotalElements() {
        return totalElements;
    }

    public Page<T> setTotalElements(long totalElements) {
        this.totalElements = totalElements;
        return this;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public Page<T> setTotalPages(long totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    public List<T> getContent() {
        return content;
    }

    public Page<T> setContent(List<T> content) {
        this.content = content;
        return this;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Page<T> setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public Page<T> setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
        return this;
    }
}
