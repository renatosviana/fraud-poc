package com.viana.poc.model;

public record Txn(
  String txnId, String userId, double amount, String currency,
  long ts, String merchantId, String country, String deviceId
) {}
