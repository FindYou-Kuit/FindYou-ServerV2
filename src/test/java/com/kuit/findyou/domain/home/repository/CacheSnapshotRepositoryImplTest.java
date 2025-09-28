package com.kuit.findyou.domain.home.repository;

import com.kuit.findyou.global.config.TestDatabaseConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({CacheSnapshotRepositoryImpl.class, TestDatabaseConfig.class})
@Sql(statements = {
        """
        CREATE TABLE IF NOT EXISTS cache_snapshot (
            cache_key VARCHAR(255) PRIMARY KEY,
            content   TEXT NOT NULL
        );
        """
})
class CacheSnapshotRepositoryImplTest {
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    CacheSnapshotRepository repository;

    @BeforeEach
    void clean() {
        jdbcTemplate.update("DELETE FROM cache_snapshot", new MapSqlParameterSource());
    }

    @DisplayName("존재하지 않는 키는 Optional.empty()를 반환한다")
    @Test
    void should_FindReturnsEmpty_When_KeyNotExists() {
        Optional<String> result = repository.find("no-such-key");
        assertThat(result).isEmpty();
    }

    @DisplayName("insert 후 find 하면 content 를 얻을 수 있다")
    @Test
    void should_Succeed_When_FindAfterInserting() {
        repository.insert("home:statistics", "{\"foo\":1}");

        Optional<String> result = repository.find("home:statistics");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("{\"foo\":1}");
    }

    @DisplayName("delete 하면 해당 키는 조회되지 않는다")
    @Test
    void should_FindReturnsEmpty_When_Delete() {
        repository.insert("k1", "v1");

        repository.delete("k1");

        assertThat(repository.find("k1")).isEmpty();
    }

    @DisplayName("같은 키로 다시 insert 하면 PK 제약으로 예외가 난다")
    @Test
    void should_ThrowException_When_InsertDuplicateKey() {
        repository.insert("dup", "v1");

        assertThrows(Exception.class,
                () -> repository.insert("dup", "v2"));
    }
}
