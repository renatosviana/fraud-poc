package com.example.poc.store;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepo {
    private final JdbcTemplate jdbc;
    public AlertRepo(JdbcTemplate jdbc){ this.jdbc = jdbc; }

    public void save(String txnId, String userId, String reasons, double score){
        jdbc.update("INSERT INTO alerts(txn_id,user_id,reasons,score) VALUES (?,?,?,?)",
            txnId, userId, reasons, score);
    }
}
