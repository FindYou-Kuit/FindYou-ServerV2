package com.kuit.findyou.domain.home.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CacheSnapshotRepositoryImpl implements CacheSnapshotRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Optional<String> find(String cacheKey) {
        String sql = "SELECT content FROM cache_snapshot WHERE cache_key = :cacheKey;";

        MapSqlParameterSource ps = new MapSqlParameterSource("cacheKey", cacheKey);

        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, ps, String.class));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void insert(String cacheKey, String content) {
        String sql = "INSERT INTO cache_snapshot(cache_key, content) VALUES(:cacheKey, :content);";

        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("cacheKey", cacheKey)
                .addValue("content", content);
        jdbcTemplate.update(sql, ps);
    }

    @Override
    public void delete(String cacheKey) {
        String sql = "DELETE FROM cache_snapshot WHERE cache_key = :cacheKey;";

        MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("cacheKey", cacheKey);
        jdbcTemplate.update(sql, ps);
    }


}
