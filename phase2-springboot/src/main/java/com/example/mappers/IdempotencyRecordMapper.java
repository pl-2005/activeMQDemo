package com.example.mappers;

import com.example.pojos.entitis.IdempotencyRecord;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 幂等记录映射器
 */
@Mapper
public interface IdempotencyRecordMapper extends BaseMapper<IdempotencyRecord> {

    @Select("""
        SELECT COALESCE(MAX(replay_no), 0)
        FROM idempotency_record
        WHERE source_key = #{sourceKey}
        """)
    Integer selectMaxReplayNoBySourceKey(@Param("sourceKey") String sourceKey);

    @Select("""
        SELECT *
        FROM idempotency_record
        WHERE source_key = #{sourceKey}
        ORDER BY replay_no, id
        """)
    List<IdempotencyRecord> selectReplayHistoryBySourceKey(@Param("sourceKey") String sourceKey);
}
