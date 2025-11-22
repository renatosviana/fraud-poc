CREATE TABLE IF NOT EXISTS alerts (
  id SERIAL PRIMARY KEY,
  txn_id TEXT,
  user_id TEXT,
  reasons TEXT,
  score DOUBLE PRECISION,
  created_at TIMESTAMPTZ DEFAULT now()
);