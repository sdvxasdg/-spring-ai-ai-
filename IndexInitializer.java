package com.atguigo.springaiproject_1.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * 启动时幂等创建 MySQL 索引。
 * MySQL 不支持 CREATE INDEX IF NOT EXISTS，所以在 Java 里判断。
 */
@Component
@RequiredArgsConstructor
public class IndexInitializer implements CommandLineRunner {

    private final DataSource dataSource;

    @Override
    public void run(String... args) {
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        createIndexIfAbsent(jdbc, "candidate",           "idx_candidate_name",      "name");
        createIndexIfAbsent(jdbc, "application_record",  "idx_application_candidate","candidate_id");
        createIndexIfAbsent(jdbc, "interview",           "idx_interview_candidate", "candidate_id");
        createIndexIfAbsent(jdbc, "job",                 "idx_job_title",           "title");
    }

    private void createIndexIfAbsent(JdbcTemplate jdbc, String table,
                                     String indexName, String column) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.statistics " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND index_name = ?",
                Integer.class, table, indexName);

        if (count != null && count > 0) {
            System.out.println(">>> [Index] " + indexName + " 已存在，跳过");
            return;
        }

        jdbc.execute("CREATE INDEX " + indexName + " ON " + table + "(" + column + ")");
        System.out.println(">>> [Index] 已创建 " + indexName + " ON " + table + "(" + column + ")");
    }
}